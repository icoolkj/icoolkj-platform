import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/pages/home/index.vue') },
    {
      path: '/login-entry',
      name: 'login-entry',
      component: () => import('@/pages/login-entry/index.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
