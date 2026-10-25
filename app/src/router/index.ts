import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useSecretUnlock } from '@/composables/useSecretUnlock'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/charts',
      name: 'charts',
      component: () => import('../views/DataCharts.vue')
    },
    {
      path: '/players/:id',
      name: 'player-details',
      component: () => import('../views/PlayerDetails.vue')
    },
    {
      // Hidden until the passphrase is entered (see useSecretUnlock)
      path: '/inventory',
      name: 'inventory',
      component: () => import('../views/InventorySearchView.vue'),
      meta: { requiresUnlock: true }
    },
    {
      path: '/rema',
      name: 'rema',
      component: () => import('../views/RemaWeaponsView.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      savedPosition.behavior = 'instant'
      return savedPosition
    } else {
      return {
        top: 0,
        left: 0,
        behavior: 'instant'
      }
    }
  }
})

router.beforeEach((to) => {
  if (to.meta.requiresUnlock && !useSecretUnlock().unlocked.value) {
    return '/'
  }
})

router.afterEach((to, from) => {
  window.scrollTo({
    top: 0,
    left: 0,
    behavior: 'instant'
  });
});

export default router
