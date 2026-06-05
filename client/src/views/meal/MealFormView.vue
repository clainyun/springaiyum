<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFoodSearchApi } from '@/composables/useFoodApis'
import { useCreateMealApi, useMealApi, useUpdateMealApi } from '@/composables/useMealApis'
import { useSessionStore } from '@/stores/session'
import type { FoodResponse, MealFoodSelection, MealRequest } from '@/types/api'
import { intValue, oneDecimal } from '@/utils/format'

type SelectedFood = FoodResponse & {
  selectedGrams: number
}

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const foodSearchApi = useFoodSearchApi()
const mealApi = useMealApi()
const createMealApi = useCreateMealApi()
const updateMealApi = useUpdateMealApi()

const editMode = computed(() => route.name === 'meal-edit')
const mealId = computed(() => (typeof route.query.mealId === 'string' ? route.query.mealId : ''))

const form = ref({
  mealDate: new Date().toISOString().slice(0, 10),
  mealType: 'lunch',
  memo: '',
})
const keyword = ref('')
const selectedFoods = ref<SelectedFood[]>([])

const recommendations = computed(() => {
  const selectedCodes = new Set(selectedFoods.value.map((food) => food.code))
  const foods = foodSearchApi.data.value || []
  const currentCalories = selectedFoods.value.reduce((sum, food) => sum + food.energy, 0)
  const target = Math.max(300, 700 - currentCalories)

  return foods
    .filter((food) => !selectedCodes.has(food.code))
    .map((food) => ({
      food,
      energyGap: Math.abs(Math.round(food.energy - target)),
    }))
    .sort((left, right) => left.energyGap - right.energyGap)
    .slice(0, 3)
})

function scaledFood(food: FoodResponse, grams: number): SelectedFood {
  const ratio = grams / (food.grams || 100)
  return {
    ...food,
    selectedGrams: grams,
    grams,
    energy: oneDecimal(food.energy * ratio),
    carbs: oneDecimal(food.carbs * ratio),
    protein: oneDecimal(food.protein * ratio),
    fat: oneDecimal(food.fat * ratio),
  }
}

function isSelected(foodCode: string) {
  return selectedFoods.value.some((food) => food.code === foodCode)
}

function gramsFor(foodCode: string) {
  const selected = selectedFoods.value.find((food) => food.code === foodCode)
  return selected?.selectedGrams || 100
}

function toggleFood(food: FoodResponse, checked: boolean) {
  if (checked) {
    if (!isSelected(food.code)) {
      selectedFoods.value.push(scaledFood(food, gramsFor(food.code)))
    }
    return
  }

  selectedFoods.value = selectedFoods.value.filter((selected) => selected.code !== food.code)
}

function updateGrams(food: FoodResponse, rawGrams: number | string) {
  const grams = Number(rawGrams) > 0 ? Number(rawGrams) : 100
  selectedFoods.value = selectedFoods.value.map((selected) =>
    selected.code === food.code ? scaledFood(food, grams) : selected,
  )
}

function removeSelected(foodCode: string) {
  selectedFoods.value = selectedFoods.value.filter((food) => food.code !== foodCode)
}

function selectedPayload(): MealFoodSelection[] {
  return selectedFoods.value.map((food) => ({
    code: food.code,
    grams: food.selectedGrams,
  }))
}

function mealRequest(): MealRequest {
  return {
    mealDate: form.value.mealDate,
    mealType: form.value.mealType,
    memo: form.value.memo,
    foods: selectedPayload(),
  }
}

function searchFoods() {
  const query: Record<string, string> = {}
  if (mealId.value) query.mealId = mealId.value
  if (keyword.value) query.keyword = keyword.value
  void router.push({ name: editMode.value ? 'meal-edit' : 'meal-new', query })
}

async function loadFoods() {
  try {
    await foodSearchApi.execute(keyword.value)
  } catch {
    sessionStore.setAlert(foodSearchApi.error.value || '음식 목록을 불러오지 못했습니다.', 'danger')
  }
}

async function loadEditMeal() {
  if (!editMode.value || !mealId.value) {
    return
  }

  try {
    const meal = await mealApi.execute(mealId.value)
    form.value = {
      mealDate: meal.mealDate,
      mealType: meal.mealType,
      memo: meal.memo,
    }
    selectedFoods.value = meal.foods.map((food) => ({
      ...food,
      selectedGrams: food.grams,
    }))
  } catch {
    sessionStore.setAlert(mealApi.error.value || '수정할 식단을 불러오지 못했습니다.', 'danger')
  }
}

async function submitMeal() {
  try {
    const request = mealRequest()
    const meal =
      editMode.value && mealId.value
        ? await updateMealApi.execute(mealId.value, request)
        : await createMealApi.execute(request)
    sessionStore.setAlert(editMode.value ? '식단이 수정되었습니다.' : '식단이 등록되었습니다.', 'success')
    await router.push({ name: 'meal-detail', query: { mealId: meal.id } })
  } catch {
    const error = editMode.value ? updateMealApi.error.value : createMealApi.error.value
    sessionStore.setAlert(error || '식단을 저장하지 못했습니다.', 'danger')
  }
}

watch(
  () => route.query,
  () => {
    keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
    void loadFoods()
  },
  { immediate: true },
)

watch(
  () => route.query.mealId,
  () => {
    void loadEditMeal()
  },
  { immediate: true },
)
</script>

<template>
  <main class="page-shell">
    <div class="container" style="max-width: 1180px">
      <section class="page-heading">
        <div>
          <h2>{{ editMode ? '식단 수정' : '식단 등록' }}</h2>
          <div class="subtle-text mt-2">식사 정보와 음식 DB 선택을 이용해 Vue 폼에서 식단을 저장합니다.</div>
        </div>
        <RouterLink class="btn btn-outline-secondary" :to="{ name: 'meal-list' }">목록으로</RouterLink>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <form @submit.prevent="submitMeal">
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label fw-semibold">날짜</label>
                  <input v-model="form.mealDate" class="form-control" type="date" required />
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">식사 유형</label>
                  <select v-model="form.mealType" class="form-select">
                    <option value="breakfast">아침</option>
                    <option value="lunch">점심</option>
                    <option value="dinner">저녁</option>
                    <option value="snack">간식</option>
                  </select>
                </div>
                <div class="col-md-4">
                  <label class="form-label fw-semibold">메모</label>
                  <input v-model="form.memo" class="form-control" type="text" maxlength="200" />
                </div>
              </div>

              <div class="section-title mt-4">
                <div>
                  <h5>음식 DB 선택</h5>
                  <div class="mini-note">Selection Sort는 저장 시 선택 음식들을 칼로리 높은 순으로 정렬합니다.</div>
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-8">
                  <input
                    v-model="keyword"
                    class="form-control"
                    type="text"
                    placeholder="음식명 또는 분류 검색"
                  />
                </div>
                <div class="col-md-4 d-grid">
                  <button class="btn btn-outline-secondary" type="button" @click="searchFoods">
                    검색 반영
                  </button>
                </div>
              </div>

              <div class="table-responsive">
                <table class="table align-middle">
                  <thead>
                    <tr>
                      <th>선택</th>
                      <th>음식</th>
                      <th>분류</th>
                      <th>100g 기준 kcal</th>
                      <th>중량(g)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="foodSearchApi.isLoading.value">
                      <td colspan="5" class="text-center subtle-text">음식 목록을 불러오는 중입니다.</td>
                    </tr>
                    <tr v-for="food in foodSearchApi.data.value || []" v-else :key="food.code">
                      <td>
                        <input
                          type="checkbox"
                          :checked="isSelected(food.code)"
                          @change="toggleFood(food, ($event.target as HTMLInputElement).checked)"
                        />
                      </td>
                      <td>{{ food.name }}</td>
                      <td>{{ food.category }}</td>
                      <td>{{ intValue(food.energy) }}</td>
                      <td>
                        <input
                          class="form-control"
                          style="min-width: 100px"
                          type="number"
                          :value="gramsFor(food.code)"
                          min="50"
                          max="500"
                          @input="updateGrams(food, ($event.target as HTMLInputElement).value)"
                        />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <button
                class="btn btn-success w-100 mt-3"
                type="submit"
                :disabled="createMealApi.isLoading.value || updateMealApi.isLoading.value"
              >
                {{ editMode ? '식단 수정' : '식단 저장' }}
              </button>
            </form>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>선택 음식</h5>
                <div class="mini-note">현재 폼에 반영된 음식 목록입니다.</div>
              </div>
            </div>
            <div v-if="selectedFoods.length === 0" class="empty-state">
              <i class="bi bi-basket"></i>
              <p class="mb-0">선택한 음식이 없습니다.</p>
            </div>
            <div v-for="food in selectedFoods" v-else :key="food.code" class="selected-food-row">
              <div>
                <div class="fw-semibold">{{ food.name }}</div>
                <div class="mini-note">{{ food.category }}</div>
              </div>
              <div class="d-flex align-items-center gap-2">
                <div class="mini-note">{{ intValue(food.selectedGrams) }}g | {{ intValue(food.energy) }} kcal</div>
                <button class="btn btn-outline-danger btn-sm" type="button" @click="removeSelected(food.code)">
                  삭제
                </button>
              </div>
            </div>
          </section>

          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>추천 음식</h5>
                <div class="mini-note">Counting Sort로 목표 칼로리와 차이가 적은 음식을 추천합니다.</div>
              </div>
            </div>
            <div v-if="recommendations.length === 0" class="empty-state">
              <i class="bi bi-stars"></i>
              <p class="mb-0">추천할 음식이 없습니다.</p>
            </div>
            <div
              v-for="recommendation in recommendations"
              v-else
              :key="recommendation.food.code"
              class="recommend-card mb-3"
            >
              <div class="fw-semibold">{{ recommendation.food.name }}</div>
              <div class="mini-note mt-2">{{ recommendation.food.category }}</div>
              <div class="mini-note mt-2">
                {{ intValue(recommendation.food.energy) }} kcal | 차이 {{ recommendation.energyGap }} kcal
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
