# 智能医助 (Chronic Disease Agent) 设计文档

## 一、项目定位

智能医助不是一个独立的 AI 聊天机器人，而是深度嵌入现有慢病管理业务流程的 AI 能力层。医生和患者在各自的工作流中按需调用 AI，而非切换到另一个"AI 对话页"。

### 核心原则

- **嵌入式**：AI 入口散布在 AdminData（健康监测）、DoctorChat（医患沟通）、异常预警等现有页面中
- **有上下文**：自动注入患者档案、指标、用药等信息，用户无需手动提供
- **单一 Agent**：不做多 Agent 编排，通过 System Prompt 工厂 + 场景切换实现不同能力
- **安全合规**：所有输出自动标注免责声明，识别紧急情况阻断并引导就医

---

## 二、整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (已有)                              │
│  AdminData(健康监测)    DoctorChat(沟通)    异常预警           │
│       │                    │                  │               │
│       │  按需调用 AI        │  内嵌 AI 面板    │               │
└───────┼────────────────────┼──────────────────┼───────────────┘
        │                    │                  │
        │   POST /agent/analyze           POST /agent/chat
        │   (场景化：传入患者 + 数据)       (自由对话)
        │                    │                  │
┌───────┴────────────────────┴──────────────────┴───────────────┐
│                    Agent Service (9005)                        │
│                                                               │
│  ┌─────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ /agent/chat │  │ /agent/analyze   │  │ /agent/summarize│ │
│  │  自由对话    │  │ 场景分析(传数据)  │  │ 患者健康小结     │ │
│  └──────┬──────┘  └────────┬─────────┘  └────────┬────────┘ │
│         │                  │                      │          │
│  ┌──────┴──────────────────┴──────────────────────┴────────┐ │
│  │                   Agent Core                             │ │
│  │  ┌──────────┐  ┌────────────┐  ┌────────────────────┐  │ │
│  │  │ 上下文    │  │  Tool      │  │  System Prompt     │  │ │
│  │  │ 构建器   │  │  调度器    │  │  工厂              │  │ │
│  │  └──────────┘  └────────────┘  └────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
│                              │                                │
│  ┌───────────────────────────┴──────────────────────────────┐│
│  │                    Feign 适配层                            ││
│  │  HealthIndexService   MedicationService                   ││
│  │  UserProfileService   DoctorPatientService                ││
│  └───────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────┘
```

---

## 三、技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.1.0 | 基础框架 |
| Spring AI | 2.0.0 | AI 抽象层 |
| DeepSeek API | v4-flash | LLM 模型（OpenAI 兼容） |
| chronic-disease-common | 1.0-SNAPSHOT | 公共模块（Result、JwtTool 等） |
| Redis | — | 会话记忆（短期） |
| MySQL | — | 会话摘要持久化（中期） |

---

## 四、三种交互模式

### 模式 1：自由对话 `POST /agent/chat`

医生在聊天框自由提问，AI 自动注入当前选中患者的上下文。

**请求：**
```json
{
  "sessionId": "uuid",
  "patientId": 1001,
  "message": "张三最近的血糖怎么样"
}
```

**AI 自动行为：**
1. 根据 `patientId` 构建上下文（患者画像 + 最近指标 + 用药）
2. 拼入 System Prompt + 用户消息
3. 调用 LLM 流式返回

### 模式 2：场景化分析 `POST /agent/analyze`

嵌入在健康监测页面，医生点击"AI 分析"按钮时触发。前端直接传当前页面已加载的数据，**省掉 Feign 调用**。

**请求：**
```json
{
  "sessionId": "uuid",
  "patientId": 1001,
  "patientName": "张三",
  "contextType": "INDEX_REVIEW",
  "data": {
    "recentIndices": [
      { "indexCode": "systolic", "indexName": "收缩压", "value": 155, "unit": "mmHg", "time": "2024-01-15" }
    ],
    "medications": [
      { "drugName": "硝苯地平缓释片", "dosage": "30mg qd" }
    ]
  },
  "question": "分析近两周血压趋势"
}
```

### 模式 3：患者健康小结 `GET /agent/summarize`

生成患者档案的 AI 摘要，可嵌入患者卡片或报告导出。

**请求：**
```
GET /agent/summarize?patientId=1001
```

**响应示例：**
```
张三，58岁男性，2型糖尿病3年。
近3月空腹血糖均值7.8mmol/L，达标率62%，较上月改善5%。
当前方案：二甲双胍0.5g tid + 格列美脲2mg qd。
最近糖化血红蛋白已逾期未查，建议近期复查。
⚠ 自动生成，仅供参考
```

---

## 五、核心组件设计

### 5.1 System Prompt 工厂

按角色 + 场景动态组装 System Prompt：

```
┌────────────────────────────────────────────────────┐
│ Role   │ DOCTOR → 辅助诊疗分析                      │
│        │ PATIENT → 健康数据解读                      │
│ Scene  │ CHAT → 对话模式（自然语言）                  │
│        │ ANALYZE → 结构化分析报告模式                 │
│        │ SUMMARIZE → 摘要模式（简洁）                 │
│ Safety │ 所有输出 + 免责声明 + 紧急词阻断             │
└────────────────────────────────────────────────────┘
```

### 5.2 上下文构建器 (ContextBuilder)

每次请求自动查询并注入患者上下文：

```
优先级分层（按 Token 预算截断）：
├── Layer 1（必选）：患者姓名、年龄、性别、慢病病种
├── Layer 2（优选）：最近3次异常指标、当前用药方案、最近复查日期
└── Layer 3（按需）：历史趋势快照、药物调整历史
```

### 5.3 Tool 调度器

注册为 Spring AI `@Tool`，AI 可在对话中自动调用：

| Tool | 数据来源 | 说明 |
|------|---------|------|
| `queryHealthTrend` | health-record-service | 指定时间段指标趋势 |
| `getCurrentMedication` | health-record-service | 当前用药方案 |
| `getRecentRechecks` | health-record-service | 最近复查记录 |
| `searchMedicalGuideline` | 知识库 | 慢病医学指南检索 |

### 5.4 安全合规

| 机制 | 说明 |
|------|------|
| 免责标注 | 所有 AI 回复末尾追加 `⚠ 以上内容由 AI 生成，仅供参考，不构成诊疗建议` |
| 紧急阻断 | 检测到"胸痛""呼吸困难""晕厥"等关键词 → 拒绝 AI 回答，提示就医 |
| 越权拦截 | 患者端尝试查询其他患者 → 拒绝 + 审计日志 |
| 敏感词过滤 | 非医疗相关敏感词自动拦截 |

### 5.5 会话管理 (SessionManager)

```
SessionContext {
    sessionId        // 会话 ID
    userId           // 当前用户
    role             // DOCTOR / PATIENT
    currentPatientId // 当前选中的患者（医生端）
    messages[]       // 对话历史 (Redis, TTL 30min)
    contextCache     // 患者上下文缓存
}
```

医生切换患者时更新 `currentPatientId`，后续对话自动注入新患者的上下文，无需重复告知 AI。

---

## 六、项目结构

```
chronic-disease-agent/
├── src/main/java/com/assistant/chronicdiseaseagent/
│   ├── ChronicDiseaseAgentApplication.java
│   ├── config/
│   │   └── AgentConfig.java               // ChatClient、ObjectMapper 等 Bean 配置
│   ├── controller/
│   │   ├── AgentChatController.java        // /agent/chat 自由对话
│   │   ├── AgentAnalyzeController.java     // /agent/analyze 场景分析
│   │   └── AgentSummaryController.java     // /agent/summarize 健康小结
│   ├── core/
│   │   ├── AgentCore.java                  // 核心：组装 prompt + context + tools
│   │   ├── context/
│   │   │   ├── ContextBuilder.java         // 上下文构建器
│   │   │   ├── PatientProfileResolver.java // 患者画像解析
│   │   │   ├── ClinicalSnapshotResolver.java // 临床快照
│   │   │   └── TokenBudgetManager.java     // Token 预算管理
│   │   ├── prompt/
│   │   │   ├── SystemPromptFactory.java    // 按角色 + 场景生成 Prompt
│   │   │   └── PromptTemplates.java        // 模板常量
│   │   ├── tool/
│   │   │   ├── HealthIndexTool.java        // 指标查询工具
│   │   │   ├── MedicationTool.java         // 用药查询工具
│   │   │   ├── RecheckTool.java            // 复查查询工具
│   │   │   └── GuidelineSearchTool.java    // 指南检索工具
│   │   ├── safety/
│   │   │   ├── OutputAnnotator.java        // 免责声明追加
│   │   │   └── EmergencyDetector.java      // 紧急情况识别
│   │   └── session/
│   │       ├── SessionManager.java         // 会话管理
│   │       └── SessionContext.java         // 会话上下文模型
│   ├── feign/
│   │   ├── RecordServiceFeign.java         // health-record-service
│   │   └── UserServiceFeign.java           // health-user-service
│   ├── model/
│   │   ├── request/
│   │   │   ├── ChatRequest.java
│   │   │   └── AnalyzeRequest.java
│   │   └── response/
│   │       └── AgentResponse.java
│   └── enums/
│       ├── ContextType.java                // INDEX_REVIEW / MEDICATION_REVIEW / GENERAL
│       └── UserRole.java                   // DOCTOR / PATIENT
├── src/main/resources/
│   └── application.yaml
├── DESIGN.md                               // 本文档
└── pom.xml
```

---

## 七、与前端交互设计

| 前端位置 | AI 入口 | 后端接口 | 说明 |
|---------|--------|---------|------|
| AdminData → 趋势图表 | "AI 分析"按钮 | POST /agent/analyze | 传入患者 + 当前指标数据，返回分析报告 |
| AdminData → 异常预警 | 行内"AI 解读" | POST /agent/analyze | 解释该异常指标的临床意义 |
| DoctorChat | 聊天框 | POST /agent/chat | 自由对话，自动注入当前患者上下文 |
| DoctorChat 侧边 | 快捷指令 | POST /agent/summarize | "生成患者小结""指标对比"等 |

---

## 八、关键约束与规则

1. **Token 预算管理**：单次上下文不超过模型上限的 60%，超出则截断低优先级数据
2. **对话记忆**：Redis 短期记忆 30min TTL，MySQL 中期记忆 90 天 TTL，每 10 轮自动摘要压缩
3. **数据权限**：医生只能查询自己绑定患者的档案，患者只能查自己的
4. **流式输出**：所有接口均支持 SSE 流式返回，前端实时渲染
5. **超时与降级**：Feign 调用超时 3s → 降级为不包含该数据的回答；LLM 超时 30s → 返回错误提示
6. **成本控制**：统计每次请求的 token 消耗，按用户/日期维度聚合，设置日限额告警
