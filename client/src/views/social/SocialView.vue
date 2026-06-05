<script setup lang="ts">
import { useFollowUserApi, useSocialDashboardApi, useUnfollowUserApi } from '@/composables/useSocialApis'
import { useSessionStore } from '@/stores/session'

const sessionStore = useSessionStore()
const socialDashboardApi = useSocialDashboardApi()
const followUserApi = useFollowUserApi()
const unfollowUserApi = useUnfollowUserApi()

async function loadSocial() {
  try {
    await socialDashboardApi.execute()
  } catch {
    sessionStore.setAlert(socialDashboardApi.error.value || '소셜 정보를 불러오지 못했습니다.', 'danger')
  }
}

async function follow(targetUserId: string) {
  try {
    const response = await followUserApi.execute(targetUserId)
    sessionStore.setAlert(response.message, 'success')
    await loadSocial()
  } catch {
    sessionStore.setAlert(followUserApi.error.value || '팔로우하지 못했습니다.', 'danger')
  }
}

async function unfollow(targetUserId: string) {
  try {
    const response = await unfollowUserApi.execute(targetUserId)
    sessionStore.setAlert(response.message, 'info')
    await loadSocial()
  } catch {
    sessionStore.setAlert(unfollowUserApi.error.value || '언팔로우하지 못했습니다.', 'danger')
  }
}

void loadSocial()
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="page-heading">
        <div>
          <h2>소셜</h2>
          <div class="subtle-text mt-2">팔로우 관계 추가/삭제와 목록 조회를 MVC 구조로 처리합니다.</div>
        </div>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>추천 사용자</h5>
                <div class="mini-note">팔로우 추천 목록입니다.</div>
              </div>
            </div>
            <div v-if="!socialDashboardApi.data.value?.suggestions.length" class="empty-state">
              <i class="bi bi-people"></i>
              <p class="mb-0">추천 사용자가 없습니다.</p>
            </div>
            <div
              v-for="target in socialDashboardApi.data.value?.suggestions || []"
              v-else
              :key="target.id"
              class="social-card mb-3"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">{{ target.nickname }}</div>
                  <div class="mini-note">{{ target.goalLabel }} | 팔로워 {{ target.followerCount }}명</div>
                </div>
                <button class="btn btn-outline-success btn-sm" type="button" @click="follow(target.id)">
                  팔로우
                </button>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>팔로잉</h5>
                <div class="mini-note">내가 팔로우 중인 사용자입니다.</div>
              </div>
            </div>
            <div v-if="!socialDashboardApi.data.value?.following.length" class="empty-state">
              <i class="bi bi-person-x"></i>
              <p class="mb-0">팔로우 중인 사용자가 없습니다.</p>
            </div>
            <div
              v-for="target in socialDashboardApi.data.value?.following || []"
              v-else
              :key="target.id"
              class="social-card mb-3"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">{{ target.nickname }}</div>
                  <div class="mini-note">{{ target.email }}</div>
                </div>
                <button class="btn btn-outline-danger btn-sm" type="button" @click="unfollow(target.id)">
                  언팔로우
                </button>
              </div>
            </div>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>팔로워</h5>
                <div class="mini-note">나를 팔로우하는 사용자입니다.</div>
              </div>
            </div>
            <div v-if="!socialDashboardApi.data.value?.followers.length" class="empty-state">
              <i class="bi bi-person-plus"></i>
              <p class="mb-0">아직 팔로워가 없습니다.</p>
            </div>
            <div
              v-for="target in socialDashboardApi.data.value?.followers || []"
              v-else
              :key="target.id"
              class="social-card mb-3"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">{{ target.nickname }}</div>
                  <div class="mini-note">{{ target.goalShortLabel }}</div>
                </div>
                <button
                  v-if="target.following"
                  class="btn btn-outline-danger btn-sm"
                  type="button"
                  @click="unfollow(target.id)"
                >
                  언팔로우
                </button>
                <button v-else class="btn btn-outline-success btn-sm" type="button" @click="follow(target.id)">
                  맞팔로우
                </button>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>리더보드</h5>
                <div class="mini-note">팔로워 수 기준 상위 사용자입니다.</div>
              </div>
            </div>
            <div v-if="!socialDashboardApi.data.value?.leaderboard.length" class="empty-state">
              <i class="bi bi-bar-chart"></i>
              <p class="mb-0">리더보드 데이터가 없습니다.</p>
            </div>
            <div
              v-for="(target, index) in socialDashboardApi.data.value?.leaderboard || []"
              v-else
              :key="target.id"
              class="social-card mb-3"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">#{{ index + 1 }} {{ target.nickname }}</div>
                  <div class="mini-note">{{ target.goalShortLabel }} | 팔로워 {{ target.followerCount }}명</div>
                </div>
                <span class="tag">{{ target.following ? '팔로잉' : '추천' }}</span>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
