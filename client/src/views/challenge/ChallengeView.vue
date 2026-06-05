<script setup lang="ts">
import { ref } from 'vue'
import {
  useChallengesApi,
  useCreateChallengeApi,
  useDeleteChallengeApi,
  useJoinChallengeApi,
  useLeaveChallengeApi,
  useUpdateChallengeProgressApi,
} from '@/composables/useChallengeApis'
import { useSessionStore } from '@/stores/session'
import type { ChallengeRequest } from '@/types/api'

const sessionStore = useSessionStore()
const challengesApi = useChallengesApi()
const createChallengeApi = useCreateChallengeApi()
const joinChallengeApi = useJoinChallengeApi()
const updateProgressApi = useUpdateChallengeProgressApi()
const leaveChallengeApi = useLeaveChallengeApi()
const deleteChallengeApi = useDeleteChallengeApi()
const progressForms = ref<Record<string, number>>({})
const form = ref<ChallengeRequest>({
  title: '',
  description: '',
  category: '습관',
  targetCount: 7,
  endDate: '',
})

async function loadChallenges() {
  try {
    const board = await challengesApi.execute()
    board.challenges.forEach((challenge) => {
      progressForms.value[challenge.id] = challenge.membership?.progress || 0
    })
  } catch {
    sessionStore.setAlert(challengesApi.error.value || '챌린지를 불러오지 못했습니다.', 'danger')
  }
}

async function submitChallenge() {
  try {
    const response = await createChallengeApi.execute(form.value)
    sessionStore.setAlert(response.message, 'success')
    form.value = { title: '', description: '', category: '습관', targetCount: 7, endDate: '' }
    await loadChallenges()
  } catch {
    sessionStore.setAlert(createChallengeApi.error.value || '챌린지를 생성하지 못했습니다.', 'danger')
  }
}

async function join(challengeId: string) {
  try {
    const response = await joinChallengeApi.execute(challengeId)
    sessionStore.setAlert(response.message, 'success')
    await loadChallenges()
  } catch {
    sessionStore.setAlert(joinChallengeApi.error.value || '챌린지에 참여하지 못했습니다.', 'danger')
  }
}

async function updateProgress(challengeId: string) {
  try {
    const response = await updateProgressApi.execute(challengeId, progressForms.value[challengeId] || 0)
    sessionStore.setAlert(response.message, 'success')
    await loadChallenges()
  } catch {
    sessionStore.setAlert(updateProgressApi.error.value || '진행률을 수정하지 못했습니다.', 'danger')
  }
}

async function leave(challengeId: string) {
  try {
    const response = await leaveChallengeApi.execute(challengeId)
    sessionStore.setAlert(response.message, 'info')
    await loadChallenges()
  } catch {
    sessionStore.setAlert(leaveChallengeApi.error.value || '챌린지에서 나가지 못했습니다.', 'danger')
  }
}

async function removeChallenge(challengeId: string) {
  if (!window.confirm('챌린지를 삭제하시겠습니까?')) return
  try {
    const response = await deleteChallengeApi.execute(challengeId)
    sessionStore.setAlert(response.message, 'success')
    await loadChallenges()
  } catch {
    sessionStore.setAlert(deleteChallengeApi.error.value || '챌린지를 삭제하지 못했습니다.', 'danger')
  }
}

void loadChallenges()
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="page-heading">
        <div>
          <h2>챌린지</h2>
          <div class="subtle-text mt-2">챌린지 생성, 참여, 진행률 수정과 삭제를 한 화면에서 관리합니다.</div>
        </div>
      </section>

      <section class="mini-grid mb-4">
        <article class="metric-card">
          <div class="label">참여 중</div>
          <div class="value">{{ challengesApi.data.value?.joinedCount || 0 }}</div>
          <div class="meta">현재 참여 중인 챌린지</div>
        </article>
        <article class="metric-card">
          <div class="label">완료</div>
          <div class="value">{{ challengesApi.data.value?.completedCount || 0 }}</div>
          <div class="meta">완료한 챌린지</div>
        </article>
        <article class="metric-card">
          <div class="label">생성</div>
          <div class="value">{{ challengesApi.data.value?.createdCount || 0 }}</div>
          <div class="meta">내가 만든 챌린지</div>
        </article>
        <article class="metric-card">
          <div class="label">전체</div>
          <div class="value">{{ challengesApi.data.value?.challengeCount || 0 }}</div>
          <div class="meta">등록된 챌린지</div>
        </article>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>챌린지 생성</h5>
                <div class="mini-note">새 챌린지를 등록하고 다른 사용자와 함께 진행해 보세요.</div>
              </div>
            </div>
            <form @submit.prevent="submitChallenge">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold">제목</label>
                  <input v-model="form.title" class="form-control" type="text" />
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">목표 횟수</label>
                  <input v-model.number="form.targetCount" class="form-control" type="number" />
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold">종료일</label>
                  <input v-model="form.endDate" class="form-control" type="date" />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">분류</label>
                  <select v-model="form.category" class="form-select">
                    <option value="습관">습관</option>
                    <option value="영양 관리">영양 관리</option>
                    <option value="운동">운동</option>
                    <option value="수면 습관">수면 습관</option>
                  </select>
                </div>
                <div class="col-md-8">
                  <label class="form-label fw-semibold">설명</label>
                  <input v-model="form.description" class="form-control" type="text" />
                </div>
              </div>
              <button class="btn btn-success w-100 mt-4" type="submit">챌린지 생성</button>
            </form>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>챌린지 보드</h5>
                <div class="mini-note">참여 상태와 진행률을 한 번에 확인할 수 있습니다.</div>
              </div>
            </div>

            <div v-if="!challengesApi.data.value?.challenges.length" class="empty-state">
              <i class="bi bi-trophy"></i>
              <p class="mb-0">등록된 챌린지가 없습니다.</p>
            </div>
            <div v-for="challenge in challengesApi.data.value?.challenges || []" v-else :key="challenge.id" class="challenge-card mb-3">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <div class="d-flex align-items-center gap-2 mb-2">
                    <span class="tag">{{ challenge.category }}</span>
                    <span v-if="challenge.owned" class="tag">내가 생성</span>
                  </div>
                  <div class="fw-semibold fs-5">{{ challenge.title }}</div>
                  <div class="mini-note mt-2">{{ challenge.description }}</div>
                  <div class="mini-note mt-2">기간 {{ challenge.periodLabel }} | 목표 {{ challenge.targetCount }}회</div>
                </div>
                <span class="soft-pill">{{ challenge.statusLabel }}</span>
              </div>

              <div class="mt-3 mini-note">
                <div v-if="challenge.participants.length === 0">참여자가 없습니다.</div>
                <div v-for="participant in challenge.participants" v-else :key="participant.userId" class="d-flex justify-content-between">
                  <span>{{ participant.nickname }}</span>
                  <strong>{{ participant.progress }}</strong>
                </div>
              </div>

              <div class="d-flex flex-wrap gap-2 mt-4">
                <button
                  v-if="!challenge.membership"
                  class="btn btn-outline-success btn-sm"
                  type="button"
                  @click="join(challenge.id)"
                >
                  참여하기
                </button>
                <template v-else>
                  <div class="d-flex gap-2">
                    <input
                      v-model.number="progressForms[challenge.id]"
                      class="form-control form-control-sm"
                      style="width: 100px"
                      type="number"
                    />
                    <button class="btn btn-outline-primary btn-sm" type="button" @click="updateProgress(challenge.id)">
                      진행률 수정
                    </button>
                  </div>
                  <button class="btn btn-outline-danger btn-sm" type="button" @click="leave(challenge.id)">
                    나가기
                  </button>
                </template>
                <button
                  v-if="challenge.owned"
                  class="btn btn-outline-danger btn-sm"
                  type="button"
                  @click="removeChallenge(challenge.id)"
                >
                  삭제
                </button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
