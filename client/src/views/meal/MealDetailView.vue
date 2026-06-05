<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDeleteMealApi, useMealApi } from '@/composables/useMealApis'
import { useSessionStore } from '@/stores/session'
import { formatDate, intValue, mealTypeLabel, oneDecimal } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const mealApi = useMealApi()
const deleteMealApi = useDeleteMealApi()

const mealId = computed(() => (typeof route.query.mealId === 'string' ? route.query.mealId : ''))

async function loadMeal() {
  if (!mealId.value) {
    return
  }

  try {
    await mealApi.execute(mealId.value)
  } catch {
    sessionStore.setAlert(mealApi.error.value || '식단 상세를 불러오지 못했습니다.', 'danger')
  }
}

async function removeMeal() {
  if (!mealId.value || !window.confirm('식단을 삭제하시겠습니까?')) {
    return
  }

  try {
    const response = await deleteMealApi.execute(mealId.value)
    sessionStore.setAlert(response.message, 'success')
    await router.push({ name: 'meal-list' })
  } catch {
    sessionStore.setAlert(deleteMealApi.error.value || '식단을 삭제하지 못했습니다.', 'danger')
  }
}

watch(
  () => route.query.mealId,
  () => {
    void loadMeal()
  },
  { immediate: true },
)
</script>

<template>
  <main class="page-shell">
    <div class="container" style="max-width: 1180px">
      <section v-if="mealApi.isLoading.value" class="surface-card">
        <div class="empty-state">
          <i class="bi bi-hourglass-split"></i>
          <p class="mb-0">식단 상세를 불러오는 중입니다.</p>
        </div>
      </section>

      <section v-else-if="!mealApi.data.value" class="surface-card">
        <div class="empty-state">
          <i class="bi bi-journal-x"></i>
          <p class="mb-3">식단을 찾을 수 없습니다.</p>
          <RouterLink class="btn btn-outline-secondary" :to="{ name: 'meal-list' }">목록으로</RouterLink>
        </div>
      </section>

      <template v-else>
        <section class="page-heading">
          <div>
            <h2>{{ mealTypeLabel(mealApi.data.value.mealType) }} 식단 상세</h2>
            <div class="subtle-text mt-2">
              {{ formatDate(mealApi.data.value.mealDate) }} 기록 | AI 분석과 영양 요약을 함께
              보여줍니다.
            </div>
          </div>
          <div class="d-flex gap-2">
            <RouterLink class="btn btn-outline-success" :to="{ name: 'community' }">커뮤니티 공유</RouterLink>
            <RouterLink
              class="btn btn-outline-primary"
              :to="{ name: 'meal-edit', query: { mealId: mealApi.data.value.id } }"
            >
              수정
            </RouterLink>
            <button class="btn btn-outline-danger" type="button" @click="removeMeal">삭제</button>
          </div>
        </section>

        <div class="split-grid">
          <div class="stack-grid">
            <section class="surface-card">
              <div class="section-title">
                <div>
                  <h5>식단 정보</h5>
                  <div class="mini-note">{{ mealApi.data.value.memo }}</div>
                </div>
                <span class="soft-pill">{{ mealTypeLabel(mealApi.data.value.mealType) }}</span>
              </div>
              <div class="table-responsive">
                <table class="table align-middle">
                  <thead>
                    <tr>
                      <th>음식</th>
                      <th>분류</th>
                      <th>중량</th>
                      <th>칼로리</th>
                      <th>탄수화물</th>
                      <th>단백질</th>
                      <th>지방</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="food in mealApi.data.value.foods" :key="food.code">
                      <td>{{ food.name }}</td>
                      <td>{{ food.category }}</td>
                      <td>{{ intValue(food.grams) }}g</td>
                      <td>{{ intValue(food.energy) }} kcal</td>
                      <td>{{ oneDecimal(food.carbs) }}g</td>
                      <td>{{ oneDecimal(food.protein) }}g</td>
                      <td>{{ oneDecimal(food.fat) }}g</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          <div class="stack-grid">
            <section class="surface-card">
              <div class="section-title">
                <div>
                  <h5>영양 분석</h5>
                  <div class="mini-note">칼로리와 탄단지 비율을 기준으로 점수를 계산합니다.</div>
                </div>
              </div>
              <div class="mini-grid">
                <div class="metric-card">
                  <div class="label">칼로리</div>
                  <div class="value">{{ intValue(mealApi.data.value.nutrition.calories) }}</div>
                  <div class="meta">
                    하루 목표 {{ mealApi.data.value.dailyGoal?.calories || '-' }} kcal
                  </div>
                </div>
                <div class="metric-card">
                  <div class="label">점수</div>
                  <div class="value">{{ mealApi.data.value.analysis?.grade || '-' }}</div>
                  <div class="meta">{{ mealApi.data.value.analysis?.score || '-' }}점</div>
                </div>
              </div>
              <div class="mt-3">
                <div class="mini-note">
                  탄수화물 <strong>{{ mealApi.data.value.nutrition.carbsPct }}%</strong> | 단백질
                  <strong>{{ mealApi.data.value.nutrition.proteinPct }}%</strong> | 지방
                  <strong>{{ mealApi.data.value.nutrition.fatPct }}%</strong>
                </div>
              </div>
            </section>

            <section class="surface-card">
              <div class="section-title">
                <div>
                  <h5>AI 식단 코멘트</h5>
                  <div class="mini-note">서비스 계층에서 생성한 식단 분석 결과입니다.</div>
                </div>
              </div>
              <div class="ai-callout">
                <div class="fw-semibold">{{ mealApi.data.value.analysis?.headline || '-' }}</div>
                <div class="mini-note mt-2">{{ mealApi.data.value.analysis?.nextAction || '-' }}</div>
              </div>
              <ul class="mt-3 mb-0 ps-3">
                <li
                  v-for="insight in mealApi.data.value.analysis?.insights || []"
                  :key="insight"
                  class="mb-2"
                >
                  {{ insight }}
                </li>
              </ul>
            </section>
          </div>
        </div>
      </template>
    </div>
  </main>
</template>
