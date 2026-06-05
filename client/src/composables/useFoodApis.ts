import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { FoodResponse } from '@/types/api'

async function searchFoods(keyword = '') {
  const response = await apiClient.get<FoodResponse[]>('/api/v1/foods', {
    params: { keyword },
  })
  return response.data
}

export function useFoodSearchApi() {
  return useApi(searchFoods)
}
