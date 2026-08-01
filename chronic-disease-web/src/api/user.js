import request from './request.js'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
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

// 患者端趋势聚合（日/周/月粒度）
export function getHealthIndexTrendV2(params) {
  return request.get('/index/chart/trend', { params })
}

// 管理端趋势聚合（查看指定患者）
export function getAdminIndexTrend(params) {
  return request.get('/index/admin/trend', { params })
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
// 患者端分页查询我的收藏
export function getFavoritesPage(data) {
  return request.post('/article/favorites/page', data)
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
// 收藏排行 Top N
export function getTopArticles(limit = 6) {
  return request.get('/article/top', { params: { limit } })
}
// 管理端查看某日排行（含收藏数）
export function getAdminRank(date, limit = 20) {
  return request.get('/article/admin/rank', { params: { date, limit } })
}
// 管理端获取患者健康指标记录
export function getUserHealthRecords(params) {
  return request.get('/index/admin/records', { params })
}
// 管理端获取患者用药记录
export function getMedicineRecords(params) {
  return request.get('/medicine/admin/records', { params })
}
// 管理端获取患者复查记录
export function getRecheckRecords(params) {
  return request.get('/recheck/admin/records', { params })
}

// ====== 医生资历管理 ======
export function getDoctorProfilePage(params) {
  return request.post('/doctor/profile/page', params)
}

export function addDoctorProfile(data) {
  return request.post('/doctor/profile/add', data)
}

export function editDoctorProfile(data) {
  return request.post('/doctor/profile/edit', data)
}

export function getDoctorProfileById(id) {
  return request.get('/doctor/profile/queryById', { params: { id } })
}

export function deleteDoctorProfile(id) {
  return request.post('/doctor/profile/delete', null, { params: { id } })
}

export function getDoctorUserList() {
  return request.get('/doctor/user/list')
}

// ====== 患者端医生绑定 ======
export function getDoctorList() {
  return request.get('/doctor/profile/list')
}

export function getMyDoctor() {
  return request.get('/doctor/patient/my')
}

export function bindDoctor(doctorId) {
  return request.post('/doctor/patient/bind', null, { params: { doctorId } })
}

export function unbindDoctor() {
  return request.post('/doctor/patient/unbind')
}

export function getAllPatientBriefs(patientName) {
  return request.get('/archive/allPatientBriefs', { params: { patientName } })
}

// 医生端：获取自己绑定的患者列表
export function getMyPatients(doctorId) {
  return request.get('/doctor/patient/myPatients', { params: { doctorId } })
}
