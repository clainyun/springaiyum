<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useLoginApi } from '@/composables/useAuthApis'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const sessionStore = useSessionStore()
const loginApi = useLoginApi()

const email = ref('demo@yamyam.com')
const password = ref('Demo1234!')

async function submitLogin() {
  try {
    const response = await loginApi.execute({
      email: email.value,
      password: password.value,
    })
    sessionStore.setLoginUser(response.user)
    sessionStore.setAlert(response.message, 'success')
    await router.push({ name: 'home' })
  } catch {
    sessionStore.setAlert(loginApi.error.value || '로그인에 실패했습니다.', 'danger')
  }
}
</script>

<template>
  <main class="auth-layout">
    <div class="auth-shell">
      <section class="auth-brand-panel">
        <h1>로그인</h1>
        <p class="summary mt-3">정적 프론트 프로젝트를 JSP와 Servlet MVC 구조로 전환한 버전입니다.</p>
        <div class="mt-4">
          <div class="pill mb-2"><i class="bi bi-diagram-3"></i>Controller - Service - Repository</div>
          <div class="pill"><i class="bi bi-kanban"></i>JSP 기반 화면 렌더링</div>
        </div>
      </section>

      <section class="auth-form-panel">
        <h2 class="fw-bold mb-3">로그인</h2>
        <p class="subtle-text">데모 계정으로 바로 확인할 수 있습니다.</p>
        <form class="mt-4" @submit.prevent="submitLogin">
          <div class="mb-3">
            <label class="form-label fw-semibold" for="email">이메일</label>
            <input
              id="email"
              v-model="email"
              class="form-control"
              type="email"
              required
              :disabled="loginApi.isLoading.value"
            />
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold" for="password">비밀번호</label>
            <input
              id="password"
              v-model="password"
              class="form-control"
              type="password"
              required
              :disabled="loginApi.isLoading.value"
            />
          </div>
          <button class="btn btn-success w-100" type="submit" :disabled="loginApi.isLoading.value">
            로그인
          </button>
        </form>
        <div v-if="loginApi.error.value" class="alert alert-danger mt-3">
          {{ loginApi.error.value }}
        </div>
        <div class="mt-3 subtle-text">
          계정이 없다면
          <RouterLink class="link-button" :to="{ name: 'signup' }">회원가입</RouterLink>으로
          이동하세요.
        </div>
      </section>
    </div>
  </main>
</template>
