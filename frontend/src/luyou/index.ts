import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../shitu/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../shitu/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../shitu/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../shitu/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('../shitu/yibiaopan/Upload.vue')
      },
      {
        path: 'images',
        name: 'Images',
        component: () => import('../shitu/yibiaopan/Images.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../shitu/yibiaopan/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      },
      {
        path: 'test-headers',
        name: 'TestHeaders',
        component: () => import('../shitu/yibiaopan/TestHeaders.vue'),
        meta: { requiresAuth: true, title: '请求头测试' }
      },
      {
        path: 'test-token',
        name: 'TestToken',
        component: () => import('../shitu/yibiaopan/TestToken.vue'),
        meta: { requiresAuth: true, title: 'Token测试' }
      },
      // 标注项目相关路由
      {
        path: 'annotation',
        name: 'AnnotationProjects',
        component: () => import('../shitu/yibiaopan/biaozhu/Projects.vue'),
        meta: { requiresAuth: true, title: '标注项目' }
      },
      {
        path: 'annotation/project/:id',
        name: 'AnnotationProjectDetail',
        component: () => import('../shitu/yibiaopan/biaozhu/ProjectDetail.vue'),
        meta: { requiresAuth: true, title: '项目详情' }
      },
      {
        path: 'annotation/label/:projectId/:imageId',
        name: 'AnnotationLabel',
        component: () => import('../shitu/yibiaopan/biaozhu/Label.vue'),
        meta: { requiresAuth: true, title: '图片标注' }
      },
      // 管理员路由
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../shitu/yibiaopan/guanliyuan/Users.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' }
      },
      {
        path: 'admin/all-images',
        name: 'AdminAllImages',
        component: () => import('../shitu/yibiaopan/guanliyuan/AllImages.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '所有图片' }
      },
      {
        path: 'admin/stats',
        name: 'AdminStats',
        component: () => import('../shitu/yibiaopan/guanliyuan/Stats.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '系统统计' }
      },
      {
        path: 'admin/create-project',
        name: 'AdminCreateProject',
        component: () => import('../shitu/yibiaopan/guanliyuan/CreateProject.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '创建标注项目' }
      },
      {
        path: 'admin/projects',
        name: 'AdminProjects',
        component: () => import('../shitu/yibiaopan/guanliyuan/Projects.vue'),
        meta: { requiresAuth: true, requiresAdmin: true, title: '项目管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../shitu/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let user = null
  
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 需要登录的路由
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else if (to.matched.some(record => record.meta.requiresAdmin)) {
      // 需要管理员权限的路由
      if (user && user.roleType === 1) {
        next()
      } else {
        next({ path: '/dashboard' })
      }
    } else {
      next()
    }
  } else if (to.matched.some(record => record.meta.guest)) {
    // 游客路由
    if (token) {
      next({ path: '/dashboard' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router 