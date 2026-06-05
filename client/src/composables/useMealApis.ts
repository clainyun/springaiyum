import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { MealDetail, MealRequest, MealSearchParams, MealSummary, MessageResponse } from '@/types/api'

async function getMeals(params: MealSearchParams = {}) {
  const response = await apiClient.get<MealSummary[]>('/api/v1/meals', { params })
  return response.data
}

async function getMeal(mealId: string) {
  const response = await apiClient.get<MealDetail>(`/api/v1/meals/${mealId}`)
  return response.data
}

async function createMeal(request: MealRequest) {
  const response = await apiClient.post<MealDetail>('/api/v1/meals', request)
  return response.data
}

async function updateMeal(mealId: string, request: MealRequest) {
  const response = await apiClient.put<MealDetail>(`/api/v1/meals/${mealId}`, request)
  return response.data
}

async function deleteMeal(mealId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/meals/${mealId}`)
  return response.data
}

export function useMealsApi() {
  return useApi(getMeals)
}

export function useMealApi() {
  return useApi(getMeal)
}

export function useCreateMealApi() {
  return useApi(createMeal)
}

export function useUpdateMealApi() {
  return useApi(updateMeal)
}

export function useDeleteMealApi() {
  return useApi(deleteMeal)
}
