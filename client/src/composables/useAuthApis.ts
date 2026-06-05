import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { AuthResponse, LoginRequest, MessageResponse, SignupRequest } from '@/types/api'

async function login(request: LoginRequest) {
  const response = await apiClient.post<AuthResponse>('/api/v1/auth/login', request)
  return response.data
}

async function signup(request: SignupRequest) {
  const response = await apiClient.post<AuthResponse>('/api/v1/auth/signup', request)
  return response.data
}

async function logout() {
  const response = await apiClient.post<MessageResponse>('/api/v1/auth/logout')
  return response.data
}

export function useLoginApi() {
  return useApi(login)
}

export function useSignupApi() {
  return useApi(signup)
}

export function useLogoutApi() {
  return useApi(logout)
}
