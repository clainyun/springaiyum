import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { MessageResponse, SocialDashboard } from '@/types/api'

async function getSocialDashboard() {
  const response = await apiClient.get<SocialDashboard>('/api/v1/social')
  return response.data
}

async function followUser(targetUserId: string) {
  const response = await apiClient.post<MessageResponse>(`/api/v1/social/following/${targetUserId}`)
  return response.data
}

async function unfollowUser(targetUserId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/social/following/${targetUserId}`)
  return response.data
}

export function useSocialDashboardApi() {
  return useApi(getSocialDashboard)
}

export function useFollowUserApi() {
  return useApi(followUser)
}

export function useUnfollowUserApi() {
  return useApi(unfollowUser)
}
