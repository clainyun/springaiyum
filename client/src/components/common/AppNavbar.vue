<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useLogoutApi } from '@/composables/useAuthApis'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const logoutApi = useLogoutApi()

const activeNav = computed(() => {
  const name = String(route.name || '')
  if (name === 'home') return 'home'
  if (name.startsWith('meal-')) return 'diet'
  if (name === 'coach') return 'coach'
  if (name === 'community') return 'community'
  if (name === 'challenge') return 'challenge'
  if (name === 'social') return 'social'
  if (name === 'profile') return 'profile'
  if (name === 'login') return 'login'
  if (name === 'signup') return 'signup'
  return ''
})

function navClass(key: string) {
  return ['nav-link', activeNav.value === key ? 'active fw-semibold' : '']
}

async function logout() {
  try {
    await logoutApi.execute()
  } finally {
    sessionStore.clearLoginUser()
    sessionStore.setAlert('로그아웃되었습니다.', 'success')
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-dark app-navbar">
    <div class="container">
      <RouterLink class="navbar-brand fw-bold" :to="{ name: 'home' }">
        <i class="bi bi-egg-fried me-2"></i>YumYum MVC
      </RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#appNavMenu"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="appNavMenu" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <template v-if="sessionStore.isLoggedIn">
            <li class="nav-item">
              <RouterLink :class="navClass('home')" :to="{ name: 'home' }">대시보드</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink :class="navClass('diet')" :to="{ name: 'meal-list' }">식단</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink :class="navClass('coach')" :to="{ name: 'coach' }">AI 코치</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink :class="navClass('community')" :to="{ name: 'community' }">커뮤니티</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink :class="navClass('challenge')" :to="{ name: 'challenge' }">챌린지</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink :class="navClass('social')" :to="{ name: 'social' }">소셜</RouterLink>
            </li>
          </template>
        </ul>

        <ul class="navbar-nav align-items-lg-center">
          <template v-if="sessionStore.isLoggedIn && sessionStore.loginUser">
            <li class="nav-item me-lg-2">
              <RouterLink :class="navClass('profile')" :to="{ name: 'profile' }">
                <i class="bi bi-person-circle me-1"></i>{{ sessionStore.loginUser.nickname }}
              </RouterLink>
            </li>
            <li class="nav-item">
              <button
                class="btn btn-outline-light btn-sm"
                type="button"
                :disabled="logoutApi.isLoading.value"
                @click="logout"
              >
                로그아웃
              </button>
            </li>
          </template>
          <template v-else>
            <li class="nav-item me-lg-2">
              <RouterLink :class="navClass('login')" :to="{ name: 'login' }">로그인</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink class="btn btn-outline-light btn-sm" :to="{ name: 'signup' }">
                회원가입
              </RouterLink>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>
