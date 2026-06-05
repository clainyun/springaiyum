import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { CoachDashboard, HomeDashboard } from '@/types/api'

async function getHomeDashboard() {
  const response = await apiClient.get<HomeDashboard>('/api/v1/dashboard/home')
  return response.data
}

async function getCoachDashboard() {
  const response = await apiClient.get<CoachDashboard>('/api/v1/coach/dashboard')
  return response.data
}

export function useHomeDashboardApi() {
  return useApi(getHomeDashboard)
}

export function useCoachDashboardApi() {
  return useApi(getCoachDashboard)
}
