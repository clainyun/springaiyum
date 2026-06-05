import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/home',
      redirect: { name: 'home' },
    },
    {
      path: '/auth/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
    },
    {
      path: '/auth/signup',
      name: 'signup',
      component: () => import('@/views/auth/SignupView.vue'),
    },
    {
      path: '/meals',
      name: 'meal-list',
      component: () => import('@/views/meal/MealListView.vue'),
    },
    {
      path: '/meals/detail',
      name: 'meal-detail',
      component: () => import('@/views/meal/MealDetailView.vue'),
    },
    {
      path: '/meals/new',
      name: 'meal-new',
      component: () => import('@/views/meal/MealFormView.vue'),
    },
    {
      path: '/meals/edit',
      name: 'meal-edit',
      component: () => import('@/views/meal/MealFormView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/ProfileView.vue'),
    },
    {
      path: '/coach',
      name: 'coach',
      component: () => import('@/views/coach/CoachView.vue'),
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('@/views/community/CommunityView.vue'),
    },
    {
      path: '/challenges',
      name: 'challenge',
      component: () => import('@/views/challenge/ChallengeView.vue'),
    },
    {
      path: '/social',
      name: 'social',
      component: () => import('@/views/social/SocialView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'error',
      component: () => import('@/views/ErrorView.vue'),
    },
  ],
})

export default router
