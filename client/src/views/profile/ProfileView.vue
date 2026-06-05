<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  useDeactivateMyAccountApi,
  useDeleteMyAccountApi,
  useMyProfileDashboardApi,
  useUpdateMyProfileApi,
} from '@/composables/useUserApis'
import { useSessionStore } from '@/stores/session'
import type { UpdateUserProfileRequest, UserProfile } from '@/types/api'
import { genderLabel, goalLabel } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const dashboardApi = useMyProfileDashboardApi()
const updateProfileApi = useUpdateMyProfileApi()
const deactivateApi = useDeactivateMyAccountApi()
const deleteApi = useDeleteMyAccountApi()

const form = ref<UpdateUserProfileRequest>({
  email: '',
  nickname: '',
  password: '',
  gender: 'male',
  birthYear: null,
  height: null,
  weight: null,
  goal: 'health',
  healthNote: '',
})

const profileUser = computed(() => dashboardApi.data.value?.user || null)
const dailyGoal = computed(() => dashboardApi.data.value?.dailyGoal || null)
const avatarInitial = computed(() => profileUser.value?.nickname.slice(0, 1) || '?')

function applyProfileToForm(user: UserProfile) {
  form.value = {
    email: user.email,
    nickname: user.nickname,
    password: '',
    gender: user.gender,
    birthYear: user.birthYear,
    height: user.height,
    weight: user.weight,
    goal: user.goal,
    healthNote: user.healthNote,
  }
}

async function loadDashboard() {
  try {
    const dashboard = await dashboardApi.execute()
    applyProfileToForm(dashboard.user)
    sessionStore.setLoginUser(dashboard.user)
  } catch {
    sessionStore.setAlert(dashboardApi.error.value || '프로필을 불러오지 못했습니다.', 'danger')
  }
}

async function submitProfile() {
  try {
    const updated = await updateProfileApi.execute(form.value)
    applyProfileToForm(updated)
    sessionStore.setLoginUser(updated)
    sessionStore.setAlert('프로필을 수정했습니다.', 'success')
    await loadDashboard()
  } catch {
    sessionStore.setAlert(updateProfileApi.error.value || '프로필을 수정하지 못했습니다.', 'danger')
  }
}

async function deactivateAccount() {
  if (!window.confirm('계정을 비활성화하시겠습니까?')) {
    return
  }

  try {
    const response = await deactivateApi.execute()
    sessionStore.clearLoginUser()
    sessionStore.setAlert(response.message, 'info')
    await router.push({ name: 'login' })
  } catch {
    sessionStore.setAlert(deactivateApi.error.value || '계정을 비활성화하지 못했습니다.', 'danger')
  }
}

async function deleteAccount() {
  if (!window.confirm('계정을 영구 삭제하시겠습니까?')) {
    return
  }

  try {
    const response = await deleteApi.execute()
    sessionStore.clearLoginUser()
    sessionStore.setAlert(response.message, 'info')
    await router.push({ name: 'login' })
  } catch {
    sessionStore.setAlert(deleteApi.error.value || '계정을 삭제하지 못했습니다.', 'danger')
  }
}

void loadDashboard()
</script>

<template>
  <main class="page-shell">
    <div class="container" style="max-width: 1120px">
      <section class="page-heading">
        <div>
          <h2>프로필</h2>
          <div class="subtle-text mt-2">회원 정보 조회/수정과 계정 관리 기능을 Vue 폼으로 제공합니다.</div>
        </div>
      </section>

      <section v-if="dashboardApi.isLoading.value && !dashboardApi.data.value" class="surface-card">
        <div class="empty-state">
          <i class="bi bi-hourglass-split"></i>
          <p class="mb-0">프로필을 불러오는 중입니다.</p>
        </div>
      </section>

      <div v-else class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div v-if="profileUser" class="d-flex align-items-center gap-3">
              <div class="avatar-badge">{{ avatarInitial }}</div>
              <div>
                <h4 class="mb-1">{{ profileUser.nickname }}</h4>
                <div class="subtle-text">{{ profileUser.email }}</div>
                <div class="d-flex gap-2 mt-2">
                  <span class="tag">{{ goalLabel(profileUser.goal) }}</span>
                  <span class="tag">{{ genderLabel(profileUser.gender) }}</span>
                </div>
              </div>
            </div>
            <div v-if="profileUser" class="detail-grid mt-4">
              <div class="recommend-card">
                <div class="mini-note">출생 연도</div>
                <div class="fw-semibold mt-2">{{ profileUser.birthYear }}</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">키</div>
                <div class="fw-semibold mt-2">{{ profileUser.height }} cm</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">몸무게</div>
                <div class="fw-semibold mt-2">{{ profileUser.weight }} kg</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">건강 메모</div>
                <div class="fw-semibold mt-2">{{ profileUser.healthNote || '없음' }}</div>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>프로필 수정</h5>
                <div class="mini-note">회원 수정 F108 및 계정 관리 기능입니다.</div>
              </div>
            </div>
            <form @submit.prevent="submitProfile">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold">이메일</label>
                  <input v-model="form.email" class="form-control" type="email" />
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold">닉네임</label>
                  <input v-model="form.nickname" class="form-control" type="text" />
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold">새 비밀번호</label>
                  <input
                    v-model="form.password"
                    class="form-control"
                    type="password"
                    placeholder="비워두면 유지됩니다."
                  />
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold">목표</label>
                  <select v-model="form.goal" class="form-select">
                    <option value="health">건강 유지</option>
                    <option value="diet">체중 감량</option>
                    <option value="muscle">근육 증가</option>
                  </select>
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">성별</label>
                  <select v-model="form.gender" class="form-select">
                    <option value="male">남성</option>
                    <option value="female">여성</option>
                  </select>
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">출생 연도</label>
                  <input v-model.number="form.birthYear" class="form-control" type="number" />
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">키</label>
                  <input v-model.number="form.height" class="form-control" type="number" />
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">몸무게</label>
                  <input v-model.number="form.weight" class="form-control" type="number" />
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold">건강 메모</label>
                  <input v-model="form.healthNote" class="form-control" type="text" />
                </div>
              </div>
              <button class="btn btn-success w-100 mt-4" type="submit" :disabled="updateProfileApi.isLoading.value">
                프로필 저장
              </button>
            </form>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>일일 목표</h5>
                <div class="mini-note">Service 계층에서 Harris-Benedict 방식으로 계산한 목표입니다.</div>
              </div>
            </div>
            <div class="mini-grid">
              <div class="metric-card">
                <div class="label">칼로리</div>
                <div class="value">{{ dailyGoal?.calories || '-' }}</div>
                <div class="meta">kcal</div>
              </div>
              <div class="metric-card">
                <div class="label">탄수화물</div>
                <div class="value">{{ dailyGoal?.carbs || '-' }}g</div>
                <div class="meta">하루 목표</div>
              </div>
              <div class="metric-card">
                <div class="label">단백질</div>
                <div class="value">{{ dailyGoal?.protein || '-' }}g</div>
                <div class="meta">하루 목표</div>
              </div>
              <div class="metric-card">
                <div class="label">지방</div>
                <div class="value">{{ dailyGoal?.fat || '-' }}g</div>
                <div class="meta">하루 목표</div>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>활동 요약</h5>
                <div class="mini-note">식단, 소셜, 챌린지 활동 집계입니다.</div>
              </div>
            </div>
            <div class="detail-grid">
              <div class="recommend-card">
                <div class="mini-note">식단 기록</div>
                <div class="fw-bold fs-4 mt-2">{{ dashboardApi.data.value?.mealCount || 0 }}</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">팔로잉</div>
                <div class="fw-bold fs-4 mt-2">{{ dashboardApi.data.value?.followingCount || 0 }}</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">팔로워</div>
                <div class="fw-bold fs-4 mt-2">{{ dashboardApi.data.value?.followerCount || 0 }}</div>
              </div>
              <div class="recommend-card">
                <div class="mini-note">참여 챌린지</div>
                <div class="fw-bold fs-4 mt-2">{{ dashboardApi.data.value?.joinedChallengeCount || 0 }}</div>
              </div>
            </div>
          </section>

          <section class="surface-card danger-panel">
            <div class="section-title">
              <div>
                <h5>계정 관리</h5>
                <div class="mini-note">비활성화와 영구 삭제를 지원합니다.</div>
              </div>
            </div>
            <button
              class="btn btn-outline-warning w-100 mb-2"
              type="button"
              :disabled="deactivateApi.isLoading.value"
              @click="deactivateAccount"
            >
              계정 비활성화
            </button>
            <button
              class="btn btn-outline-danger w-100"
              type="button"
              :disabled="deleteApi.isLoading.value"
              @click="deleteAccount"
            >
              계정 영구 삭제
            </button>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
