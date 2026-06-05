import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type {
  MessageResponse,
  UpdateUserProfileRequest,
  UserProfile,
  UserProfileDashboard,
} from '@/types/api'

async function getMyProfile() {
  const response = await apiClient.get<UserProfile>('/api/v1/users/me')
  return response.data
}

async function updateMyProfile(request: UpdateUserProfileRequest) {
  const response = await apiClient.put<UserProfile>('/api/v1/users/me', request)
  return response.data
}

async function getMyProfileDashboard() {
  const response = await apiClient.get<UserProfileDashboard>('/api/v1/users/me/dashboard')
  return response.data
}

async function deactivateMyAccount() {
  const response = await apiClient.post<MessageResponse>('/api/v1/users/me/deactivate')
  return response.data
}

async function deleteMyAccount() {
  const response = await apiClient.delete<MessageResponse>('/api/v1/users/me')
  return response.data
}

export function useMyProfileApi() {
  return useApi(getMyProfile)
}

export function useUpdateMyProfileApi() {
  return useApi(updateMyProfile)
}

export function useMyProfileDashboardApi() {
  return useApi(getMyProfileDashboard)
}

export function useDeactivateMyAccountApi() {
  return useApi(deactivateMyAccount)
}

export function useDeleteMyAccountApi() {
  return useApi(deleteMyAccount)
}
