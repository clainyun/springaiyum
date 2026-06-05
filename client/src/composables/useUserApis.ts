import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { UpdateUserProfileRequest, UserProfile } from '@/types/api'

async function getMyProfile() {
  const response = await apiClient.get<UserProfile>('/api/v1/users/me')
  return response.data
}

async function updateMyProfile(request: UpdateUserProfileRequest) {
  const response = await apiClient.put<UserProfile>('/api/v1/users/me', request)
  return response.data
}

export function useMyProfileApi() {
  return useApi(getMyProfile)
}

export function useUpdateMyProfileApi() {
  return useApi(updateMyProfile)
}
