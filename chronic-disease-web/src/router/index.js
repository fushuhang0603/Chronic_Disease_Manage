import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Layout from '../views/Layout.vue'
import LayoutPatient from '../views/LayoutPatient.vue'
import UserManage from '../views/UserManage.vue'
import IndexDictManage from '../views/IndexDictManage.vue'
import PatientHome from '../views/PatientHome.vue'
import ArchivePatient from '../views/ArchivePatient.vue'
import ArchiveManage from '../views/ArchiveManage.vue'
import PatientData from '../views/PatientData.vue'
import PatientRemind from '../views/PatientRemind.vue'
import PatientArticle from '../views/PatientArticle.vue'
import PatientDoctors from '../views/PatientDoctors.vue'
import AdminRemind from '../views/AdminRemind.vue'
import AdminArticle from '../views/AdminArticle.vue'
import AdminData from '../views/AdminData.vue'
import DoctorManage from '../views/DoctorManage.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/home', name: 'Home', component: Home },
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/users',
    children: [
      { path: 'users', name: 'UserManage', component: UserManage },
      { path: 'dicts', name: 'IndexDictManage', component: IndexDictManage },
      { path: 'archives', name: 'ArchiveManage', component: ArchiveManage },
      { path: 'remind', name: 'AdminRemind', component: AdminRemind },
      { path: 'article', name: 'AdminArticle', component: AdminArticle },
      { path: 'data', name: 'AdminData', component: AdminData },
      { path: 'doctors', name: 'DoctorManage', component: DoctorManage },
    ],
  },
  {
    path: '/patient',
    component: LayoutPatient,
    redirect: '/patient/home',
    children: [
      { path: 'home', name: 'PatientHome', component: PatientHome },
      { path: 'archive', name: 'ArchivePatient', component: ArchivePatient },
      { path: 'data', name: 'PatientData', component: PatientData },
      { path: 'remind', name: 'PatientRemind', component: PatientRemind },
      { path: 'article', name: 'PatientArticle', component: PatientArticle },
      { path: 'doctors', name: 'PatientDoctors', component: PatientDoctors },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
