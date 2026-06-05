<script setup lang="ts">
import { useHomeDashboardApi } from '@/composables/useDashboardApis'
import { useSessionStore } from '@/stores/session'
import { formatDate, intValue, mealTypeLabel } from '@/utils/format'

const sessionStore = useSessionStore()
const homeDashboardApi = useHomeDashboardApi()

async function loadHomeDashboard() {
  try {
    await homeDashboardApi.execute()
  } catch {
    sessionStore.setAlert(homeDashboardApi.error.value || '대시보드를 불러오지 못했습니다.', 'danger')
  }
}

void loadHomeDashboard()
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="hero-card mb-4">
        <div class="page-heading mb-0">
          <div>
            <div class="pill mb-3"><i class="bi bi-diagram-3"></i>JSP + MVC + 알고리즘 적용</div>
            <h1>냠냠 프로젝트 대시보드</h1>
            <p class="mb-0 mt-3 fs-5 text-white-50">
              {{ homeDashboardApi.data.value?.coachAdvice.summary || '오늘의 식단과 활동을 한눈에 확인하세요.' }}
            </p>
          </div>
        </div>
      </section>

      <section class="mini-grid mb-4">
        <article class="metric-card">
          <div class="label">오늘 칼로리</div>
          <div class="value">{{ intValue(homeDashboardApi.data.value?.todaySummary.calories) }} kcal</div>
          <div class="meta">목표 {{ homeDashboardApi.data.value?.dailyGoal.calories || '-' }} kcal</div>
        </article>
        <article class="metric-card">
          <div class="label">탄단지 비율</div>
          <div class="value">
            {{ homeDashboardApi.data.value?.todaySummary.carbsPct || 0 }}/{{
              homeDashboardApi.data.value?.todaySummary.proteinPct || 0
            }}/{{ homeDashboardApi.data.value?.todaySummary.fatPct || 0 }}
          </div>
          <div class="meta">탄수화물 / 단백질 / 지방</div>
        </article>
        <article class="metric-card">
          <div class="label">팔로잉</div>
          <div class="value">{{ homeDashboardApi.data.value?.followingCount || 0 }}</div>
          <div class="meta">팔로워 {{ homeDashboardApi.data.value?.followerCount || 0 }}명</div>
        </article>
        <article class="metric-card">
          <div class="label">AI 코치</div>
          <div class="value">
            {{ homeDashboardApi.data.value?.coachAdvice.recentAnalyses[0]?.grade || '-' }}
          </div>
          <div class="meta">{{ homeDashboardApi.data.value?.coachAdvice.recovery || '규칙 기반 분석 준비 중' }}</div>
        </article>
      </section>

      <section class="quick-links mb-4">
        <RouterLink class="quick-link" :to="{ name: 'meal-list' }">
          <span class="icon"><i class="bi bi-journal-text"></i></span>
          <strong>식단 관리</strong>
          <span class="mini-note">목록, 등록, 수정, 삭제</span>
        </RouterLink>
        <RouterLink class="quick-link" :to="{ name: 'community' }">
          <span class="icon"><i class="bi bi-chat-left-dots"></i></span>
          <strong>커뮤니티</strong>
          <span class="mini-note">게시글과 댓글 CRUD</span>
        </RouterLink>
        <RouterLink class="quick-link" :to="{ name: 'coach' }">
          <span class="icon"><i class="bi bi-stars"></i></span>
          <strong>AI 코치</strong>
          <span class="mini-note">운동 및 식단 분석</span>
        </RouterLink>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>최근 식단</h5>
                <div class="mini-note">Quick Sort로 최신순 정렬된 식단 기록입니다.</div>
              </div>
              <RouterLink class="btn btn-outline-success btn-sm" :to="{ name: 'meal-new' }">
                식단 등록
              </RouterLink>
            </div>
            <div v-if="!homeDashboardApi.data.value?.recentMeals.length" class="empty-state">
              <i class="bi bi-journal-x"></i>
              <p class="mb-0">저장된 식단이 없습니다.</p>
            </div>
            <div v-for="meal in homeDashboardApi.data.value?.recentMeals || []" v-else :key="meal.id" class="meal-card">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <div class="tag mb-2">{{ mealTypeLabel(meal.mealType) }}</div>
                  <div class="fw-semibold">{{ formatDate(meal.mealDate) }}</div>
                  <div class="mini-note mt-2">{{ meal.memo }}</div>
                </div>
                <RouterLink
                  class="btn btn-outline-primary btn-sm"
                  :to="{ name: 'meal-detail', query: { mealId: meal.id } }"
                >
                  상세 보기
                </RouterLink>
              </div>
            </div>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>활성 챌린지</h5>
                <div class="mini-note">챌린지 참여 현황을 확인하세요.</div>
              </div>
            </div>
            <div v-if="!homeDashboardApi.data.value?.activeChallenges.length" class="empty-state">
              <i class="bi bi-trophy"></i>
              <p class="mb-0">활성 챌린지가 없습니다.</p>
            </div>
            <div
              v-for="challenge in homeDashboardApi.data.value?.activeChallenges || []"
              v-else
              :key="challenge.id"
              class="challenge-card mb-3"
            >
              <div class="fw-semibold">{{ challenge.title }}</div>
              <div class="mini-note mt-2">{{ challenge.description }}</div>
              <div class="mini-note mt-2">
                목표 {{ challenge.targetCount }}회 | 종료 {{ formatDate(challenge.endDate) }}
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
