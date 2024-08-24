import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/users/:id',
      name: 'user-details',
      component: () => import('../views/UserDetails.vue')
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      savedPosition.behavior = 'instant';
      return savedPosition;
    } else {
      return { 
        top: 0,
        left: 0,
        behavior: 'instant'
      };
    }
  },
})

router.afterEach((to, from) => {
  window.scrollTo({
    top: 0,
    left: 0,
    behavior: 'instant'
  });
});

export default router
