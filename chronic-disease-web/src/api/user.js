import request from './request.js'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUserPage(params) {
  return request.post('/user/page', params)
}

export function addUser(data) {
  return request.post('/user/add', data)
}

export function editUser(data) {
  return request.post('/user/edit', data)
}

export function getUserById(id) {
  return request.get('/user/queryById', { params: { id } })
}

export function deleteUser(id) {
  return request.post('/user/delete', null, { params: { id } })
}

export function updateUserStatus(id, status) {
  return request.post('/user/updateStatus', null, { params: { id, status } })
}

// ====== 健康档案 ======
export function getMyArchive() {
  return request.get('/archive/my')
}

export function addArchive(data) {
  return request.post('/archive/add', data)
}

export function editArchive(data) {
  return request.post('/archive/edit', data)
}

export function getArchivePage(params) {
  return request.post('/archive/page', params)
}

export function deleteArchive(id) {
  return request.post('/archive/delete', null, { params: { id } })
}

// ====== 指标字典 ======
export function getDictPage(params) {
  return request.post('/dict/page', params)
}

export function addDict(data) {
  return request.post('/dict/add', data)
}

export function editDict(data) {
  return request.post('/dict/edit', data)
}

export function getDictById(id) {
  return request.get('/dict/queryById', { params: { id } })
}

export function deleteDict(id) {
  return request.post('/dict/delete', null, { params: { id } })
}

export function updateDictStatus(id, status) {
  return request.post('/dict/updateStatus', null, { params: { id, status } })
}

// ====== 健康指标记录 ======
export function addHealthIndex(data) {
  return request.post('/index/add', data)
}

export function getHealthIndexPage(params) {
  return request.post('/index/page', params)
}

export function getHealthIndexTrend(params) {
  return request.get('/index/chart', { params })
}

export function deleteHealthIndex(id) {
  return request.post('/index/delete', null, { params: { id } })
}

// ====== 用药记录 ======
export function addMedicine(data) {
  return request.post('/medicine/add', data)
}

export function getMedicinePage(data) {
  return request.post('/medicine/page', data)
}

export function deleteMedicine(id) {
  return request.post('/medicine/delete', null, { params: { id } })
}

// ====== 复查记录 ======
export function addRecheck(data) {
  return request.post('/recheck/add', data)
}

export function getRecheckPage(data) {
  return request.post('/recheck/page', data)
}

export function deleteRecheck(id) {
  return request.post('/recheck/delete', null, { params: { id } })
}

// ====== 健康提醒 ======
export function addRemind(data) {
  return request.post('/remind/add', data)
}

export function getRemindPage(params) {
  return request.post('/remind/page', params)
}

export function updateRemindStatus(id, status) {
  return request.post('/remind/updateStatus', null, { params: { id, status } })
}

export function deleteRemind(id) {
  return request.post('/remind/delete', null, { params: { id } })
}

// 管理员查看所有患者提醒
export function getAllRemindPage(params) {
  return request.post('/remind/pageAll', params)
}

// 管理员为患者创建提醒（复用 /remind/add）
export function addRemindForPatient(data) {
  return request.post('/remind/add', data)
}

// ==================== 健康资讯 ====================
// 患者端分页查询资讯
export function getArticlePage(data) {
  return request.post('/article/page', data)
}
// 管理端分页查询资讯
export function getArticlePageAdmin(data) {
  return request.post('/article/admin/page', data)
}
// 资讯详情
export function getArticleDetail(id) {
  return request.get('/article/detail', { params: { id } })
}
// 新增资讯
export function addArticle(data) {
  return request.post('/article/add', data)
}
// 编辑资讯
export function editArticle(data) {
  return request.post('/article/edit', data)
}
// 删除资讯
export function deleteArticle(id) {
  return request.post('/article/delete', null, { params: { id } })
}
// 上下架
export function updateArticleStatus(id, status) {
  return request.post('/article/updateStatus', null, { params: { id, status } })
}
// 收藏/取消收藏（status: 0收藏 1取消收藏）
export function updateFavoriteStatus(articleId, status) {
  return request.post('/article/favorite', null, { params: { articleId, status } })
}
// 记录阅读历史
export function recordReadHistory(data) {
  return request.post('/article/readHistory', data)
}
