<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDeleteMealApi, useMealsApi } from '@/composables/useMealApis'
import { useSessionStore } from '@/stores/session'
import type { MealSearchParams } from '@/types/api'
import { formatDate, intValue, mealTypeLabel } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const mealsApi = useMealsApi()
const deleteMealApi = useDeleteMealApi()

const filters = ref({
  startDate: '',
  endDate: '',
  mealType: '',
  sortKey: 'dateDesc',
})

function queryValue(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function syncFiltersFromQuery() {
  filters.value = {
    startDate: queryValue(route.query.startDate),
    endDate: queryValue(route.query.endDate),
    mealType: queryValue(route.query.mealType),
    sortKey: queryValue(route.query.sortKey) || 'dateDesc',
  }
}

function search() {
  const query: MealSearchParams = {}
  if (filters.value.startDate) query.startDate = filters.value.startDate
  if (filters.value.endDate) query.endDate = filters.value.endDate
  if (filters.value.mealType) query.mealType = filters.value.mealType
  if (filters.value.sortKey) query.sortKey = filters.value.sortKey
  void router.push({ name: 'meal-list', query })
}

async function loadMeals() {
  syncFiltersFromQuery()
  try {
    await mealsApi.execute(filters.value)
  } catch {
    sessionStore.setAlert(mealsApi.error.value || '식단 목록을 불러오지 못했습니다.', 'danger')
  }
}

async function removeMeal(mealId: string) {
  if (!window.confirm('식단을 삭제하시겠습니까?')) {
    return
  }

  try {
    const response = await deleteMealApi.execute(mealId)
    sessionStore.setAlert(response.message, 'success')
    await loadMeals()
  } catch {
    sessionStore.setAlert(deleteMealApi.error.value || '식단을 삭제하지 못했습니다.', 'danger')
  }
}

watch(
  () => route.query,
  () => {
    void loadMeals()
  },
  { immediate: true },
)
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="page-heading">
        <div>
          <h2>식단 목록</h2>
          <div class="subtle-text mt-2">JSP 화면에서 식단 목록 조회와 정렬 기능을 제공합니다.</div>
        </div>
        <RouterLink class="btn btn-success" :to="{ name: 'meal-new' }">
          <i class="bi bi-plus-lg me-1"></i>식단 등록
        </RouterLink>
      </section>

      <section class="surface-card mb-4">
        <form class="row g-3 align-items-end" @submit.prevent="search">
          <div class="col-md-3">
            <label class="form-label fw-semibold">시작일</label>
            <input v-model="filters.startDate" class="form-control" type="date" />
          </div>
          <div class="col-md-3">
            <label class="form-label fw-semibold">종료일</label>
            <input v-model="filters.endDate" class="form-control" type="date" />
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">식사 유형</label>
            <select v-model="filters.mealType" class="form-select">
              <option value="">전체</option>
              <option value="breakfast">아침</option>
              <option value="lunch">점심</option>
              <option value="dinner">저녁</option>
              <option value="snack">간식</option>
            </select>
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">정렬</label>
            <select v-model="filters.sortKey" class="form-select">
              <option value="dateDesc">최신순</option>
              <option value="dateAsc">오래된순</option>
              <option value="energyDesc">칼로리 높은순</option>
              <option value="scoreDesc">점수 높은순</option>
            </select>
          </div>
          <div class="col-md-2 d-grid">
            <button class="btn btn-outline-secondary" type="submit" :disabled="mealsApi.isLoading.value">
              조회
            </button>
          </div>
        </form>
      </section>

      <section class="surface-card">
        <div class="section-title">
          <div>
            <h5>식단 기록</h5>
            <div class="mini-note">Quick Sort 기반 정렬 결과입니다.</div>
          </div>
        </div>
        <div v-if="mealsApi.isLoading.value" class="empty-state">
          <i class="bi bi-hourglass-split"></i>
          <p class="mb-0">식단을 불러오는 중입니다.</p>
        </div>
        <div v-else-if="!mealsApi.data.value || mealsApi.data.value.length === 0" class="empty-state">
          <i class="bi bi-journal-x"></i>
          <p class="mb-0">조회된 식단이 없습니다.</p>
        </div>
        <div v-else>
          <div v-for="meal in mealsApi.data.value" :key="meal.id" class="meal-card">
            <div class="d-flex justify-content-between align-items-start gap-3">
              <div>
                <div class="d-flex align-items-center gap-2 mb-2">
                  <span class="tag">{{ mealTypeLabel(meal.mealType) }}</span>
                  <span class="mini-note">{{ formatDate(meal.mealDate) }}</span>
                </div>
                <div class="mini-note">{{ intValue(meal.nutrition.calories) }} kcal</div>
                <div class="mini-note mt-2">{{ meal.memo }}</div>
              </div>
              <div class="d-flex gap-2">
                <RouterLink
                  class="btn btn-outline-primary btn-sm"
                  :to="{ name: 'meal-detail', query: { mealId: meal.id } }"
                >
                  상세
                </RouterLink>
                <RouterLink
                  class="btn btn-outline-success btn-sm"
                  :to="{ name: 'meal-edit', query: { mealId: meal.id } }"
                >
                  수정
                </RouterLink>
                <button class="btn btn-outline-danger btn-sm" type="button" @click="removeMeal(meal.id)">
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
