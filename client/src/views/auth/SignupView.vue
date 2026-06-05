<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSignupApi } from '@/composables/useAuthApis'
import { useSessionStore } from '@/stores/session'
import type { SignupRequest } from '@/types/api'

const router = useRouter()
const sessionStore = useSessionStore()
const signupApi = useSignupApi()

const form = ref<SignupRequest>({
  email: '',
  password: '',
  nickname: '',
  gender: 'male',
  birthYear: 1998,
  height: 165,
  weight: 60,
  goal: 'health',
  healthNote: '',
})

async function submitSignup() {
  try {
    const response = await signupApi.execute(form.value)
    sessionStore.setLoginUser(response.user)
    sessionStore.setAlert(response.message, 'success')
    await router.push({ name: 'home' })
  } catch {
    sessionStore.setAlert(signupApi.error.value || '회원가입에 실패했습니다.', 'danger')
  }
}
</script>

<template>
  <main class="auth-layout">
    <div class="auth-shell">
      <section class="auth-brand-panel">
        <h1>회원가입</h1>
        <p class="summary mt-3">사용자 정보와 신체 정보를 저장하고, 이후 JSP 화면에서 개인화된 목표를 계산합니다.</p>
        <div class="mt-4">
          <div class="pill mb-2"><i class="bi bi-person-vcard"></i>회원 CRUD</div>
          <div class="pill"><i class="bi bi-heart-pulse"></i>일일 목표 자동 계산</div>
        </div>
      </section>

      <section class="auth-form-panel">
        <form @submit.prevent="submitSignup">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label fw-semibold">이메일</label>
              <input
                v-model="form.email"
                class="form-control"
                type="email"
                required
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">비밀번호</label>
              <input
                v-model="form.password"
                class="form-control"
                type="password"
                required
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">닉네임</label>
              <input
                v-model="form.nickname"
                class="form-control"
                type="text"
                required
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-md-6">
              <label class="form-label fw-semibold">목표</label>
              <select v-model="form.goal" class="form-select" :disabled="signupApi.isLoading.value">
                <option value="health">건강 유지</option>
                <option value="diet">체중 감량</option>
                <option value="muscle">근육 증가</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">성별</label>
              <select
                v-model="form.gender"
                class="form-select"
                :disabled="signupApi.isLoading.value"
              >
                <option value="male">남성</option>
                <option value="female">여성</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold">출생 연도</label>
              <input
                v-model.number="form.birthYear"
                class="form-control"
                type="number"
                min="1940"
                max="2010"
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-md-2">
              <label class="form-label fw-semibold">키</label>
              <input
                v-model.number="form.height"
                class="form-control"
                type="number"
                min="120"
                max="230"
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-md-2">
              <label class="form-label fw-semibold">몸무게</label>
              <input
                v-model.number="form.weight"
                class="form-control"
                type="number"
                min="30"
                max="250"
                :disabled="signupApi.isLoading.value"
              />
            </div>
            <div class="col-12">
              <label class="form-label fw-semibold">건강 메모</label>
              <input
                v-model="form.healthNote"
                class="form-control"
                type="text"
                placeholder="알레르기나 주의할 항목을 입력하세요."
                :disabled="signupApi.isLoading.value"
              />
            </div>
          </div>
          <button class="btn btn-success w-100 mt-4" type="submit" :disabled="signupApi.isLoading.value">
            회원가입
          </button>
        </form>
        <div v-if="signupApi.error.value" class="alert alert-danger mt-3">
          {{ signupApi.error.value }}
        </div>
      </section>
    </div>
  </main>
</template>
