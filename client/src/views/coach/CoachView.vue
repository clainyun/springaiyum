<script setup lang="ts">
import { ref } from 'vue'
import { useCoachDashboardApi } from '@/composables/useDashboardApis'
import { useSessionStore } from '@/stores/session'
import { intValue } from '@/utils/format'
import { apiClient } from '@/composables/apiClient'

const sessionStore = useSessionStore()
const coachDashboardApi = useCoachDashboardApi()

async function loadCoachDashboard() {
  try {
    await coachDashboardApi.execute()
  } catch {
    sessionStore.setAlert(coachDashboardApi.error.value || '코치 데이터를 불러오지 못했습니다.', 'danger')
  }
}

void loadCoachDashboard()

// ── Spring AI 질문 영역 ──────────────────────────────────────
const aiQuestion = ref('')
const aiAnswer = ref('')
const aiLoading = ref(false)

async function askAiCoach() {
  if (!aiQuestion.value.trim()) return
  aiLoading.value = true
  aiAnswer.value = ''
  try {
    const res = await apiClient.get<{ answer: string }>('/api/v1/ai/coach', {
      params: { userId: 'user_demo', question: aiQuestion.value },
    })
    aiAnswer.value = res.data.answer
  } catch {
    aiAnswer.value = 'AI 코치 응답을 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    aiLoading.value = false
  }
}
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="hero-card mb-4">
        <div class="page-heading mb-0">
          <div>
            <div class="pill mb-3"><i class="bi bi-stars"></i>오늘의 코칭</div>
            <h1>AI 코치</h1>
            <p class="mb-0 mt-3 fs-5 text-white-50">
              {{ coachDashboardApi.data.value?.summary || '코칭 데이터를 불러오는 중입니다.' }}
            </p>
          </div>
        </div>
      </section>

      <section class="mini-grid mb-4">
        <article class="metric-card">
          <div class="label">오늘 칼로리</div>
          <div class="value">{{ intValue(coachDashboardApi.data.value?.todaySummary.calories) }}</div>
          <div class="meta">목표 {{ coachDashboardApi.data.value?.dailyGoal.calories || '-' }} kcal</div>
        </article>
        <article class="metric-card">
          <div class="label">오늘 목표 비율</div>
          <div class="value">{{ coachDashboardApi.data.value?.todayPct || 0 }}%</div>
          <div class="meta">칼로리 기준</div>
        </article>
        <article class="metric-card">
          <div class="label">단백질</div>
          <div class="value">{{ intValue(coachDashboardApi.data.value?.todaySummary.protein) }}g</div>
          <div class="meta">목표 {{ coachDashboardApi.data.value?.dailyGoal.protein || '-' }}g</div>
        </article>
        <article class="metric-card">
          <div class="label">회복 메모</div>
          <div class="value" style="font-size: 1rem">
            {{ coachDashboardApi.data.value?.recovery || '회복 코멘트를 불러오는 중입니다.' }}
          </div>
          <div class="meta">식단 기반 코칭</div>
        </article>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>운동 계획</h5>
                <div class="mini-note">서비스 계산 결과를 바탕으로 생성된 루틴입니다.</div>
              </div>
            </div>

            <div class="ai-callout mb-4">
              <div class="fw-semibold">
                {{ coachDashboardApi.data.value?.summary || '코칭 데이터를 불러오는 중입니다.' }}
              </div>
              <div class="mini-note mt-2">
                {{ coachDashboardApi.data.value?.recovery || '회복 메모를 불러오는 중입니다.' }}
              </div>
            </div>

            <div class="row g-3">
              <div v-if="!coachDashboardApi.data.value?.sessions.length" class="col-12">
                <div class="empty-state">
                  <i class="bi bi-heart-pulse"></i>
                  <p class="mb-0">추천 운동이 아직 없습니다.</p>
                </div>
              </div>
              <div v-for="session in coachDashboardApi.data.value?.sessions || []" v-else :key="session.title" class="col-md-6">
                <div class="recommend-card h-100">
                  <div class="fw-semibold">{{ session.title }}</div>
                  <div class="mini-note mt-2">{{ session.detail }}</div>
                  <span class="tag mt-3">{{ session.intensity }}</span>
                </div>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>다음 액션</h5>
                <div class="mini-note">식단과 챌린지 상태를 기준으로 제안합니다.</div>
              </div>
            </div>
            <div v-if="!coachDashboardApi.data.value?.nextActions.length" class="empty-state">
              <i class="bi bi-check2-circle"></i>
              <p class="mb-0">표시할 다음 액션이 없습니다.</p>
            </div>
            <ul v-else class="mb-0 ps-3">
              <li v-for="action in coachDashboardApi.data.value?.nextActions || []" :key="action" class="mb-2">
                {{ action }}
              </li>
            </ul>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>최근 식단 분석</h5>
                <div class="mini-note">최근 3개의 식단 분석 결과입니다.</div>
              </div>
            </div>
            <div v-if="!coachDashboardApi.data.value?.recentAnalyses.length" class="empty-state">
              <i class="bi bi-stars"></i>
              <p class="mb-0">최근 식단 데이터가 없습니다.</p>
            </div>
            <div
              v-for="analysis in coachDashboardApi.data.value?.recentAnalyses || []"
              v-else
              :key="analysis.headline"
              class="community-card mb-3"
            >
              <div class="fw-semibold">{{ analysis.headline }}</div>
              <div class="mini-note mt-2">{{ analysis.nextAction }}</div>
              <div class="tag mt-3">등급 {{ analysis.grade }} / {{ analysis.score }}점</div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>챌린지 연계</h5>
                <div class="mini-note">참여 중인 챌린지의 현재 진행도를 보여줍니다.</div>
              </div>
            </div>
            <div v-if="!coachDashboardApi.data.value?.challenges.length" class="empty-state">
              <i class="bi bi-trophy"></i>
              <p class="mb-0">참여 중인 챌린지가 없습니다.</p>
            </div>
            <div
              v-for="challenge in coachDashboardApi.data.value?.challenges || []"
              v-else
              :key="challenge.id"
              class="challenge-card mb-3"
            >
              <div class="fw-semibold">{{ challenge.title || '챌린지' }}</div>
              <div class="mini-note mt-2">{{ challenge.description }}</div>
              <div class="mini-note mt-2">
                진행률 {{ challenge.progress }} / {{ challenge.targetCount }}
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- ── Spring AI 질문 섹션 ───────────────────────────── -->
      <section class="surface-card mt-4">
        <div class="section-title">
          <div>
            <h5><i class="bi bi-stars me-2" style="color: var(--accent)"></i>AI 코치에게 질문하기</h5>
            <div class="mini-note">식단·영양 데이터를 기반으로 Gemini AI가 개인화된 분석을 제공합니다.</div>
          </div>
        </div>

        <div class="d-flex gap-2">
          <input
            v-model="aiQuestion"
            type="text"
            class="form-control"
            placeholder="오늘 내 식단을 분석해줘"
            :disabled="aiLoading"
            @keyup.enter="askAiCoach"
          />
          <button
            class="btn btn-success"
            style="white-space: nowrap"
            :disabled="aiLoading || !aiQuestion.trim()"
            @click="askAiCoach"
          >
            <span v-if="aiLoading">
              <span class="spinner-border spinner-border-sm me-1" role="status"></span>분석 중...
            </span>
            <span v-else><i class="bi bi-send me-1"></i>AI 코치에게 질문하기</span>
          </button>
        </div>

        <div v-if="aiAnswer" class="ai-callout mt-4">
          <div class="fw-semibold mb-3">
            <i class="bi bi-robot me-2" style="color: var(--brand)"></i>AI 코치 응답
          </div>
          <div style="white-space: pre-line; line-height: 1.75">{{ aiAnswer }}</div>
        </div>
      </section>
      <!-- ─────────────────────────────────────────────────── -->

    </div>
  </main>
</template>
