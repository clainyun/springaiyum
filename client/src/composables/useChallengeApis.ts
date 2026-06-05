import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type { ChallengeBoard, ChallengeRequest, MessageResponse } from '@/types/api'

async function getChallenges() {
  const response = await apiClient.get<ChallengeBoard>('/api/v1/challenges')
  return response.data
}

async function createChallenge(request: ChallengeRequest) {
  const response = await apiClient.post<MessageResponse>('/api/v1/challenges', request)
  return response.data
}

async function joinChallenge(challengeId: string) {
  const response = await apiClient.post<MessageResponse>(`/api/v1/challenges/${challengeId}/memberships`)
  return response.data
}

async function updateProgress(challengeId: string, progress: number) {
  const response = await apiClient.patch<MessageResponse>(`/api/v1/challenges/${challengeId}/memberships/me`, {
    progress,
  })
  return response.data
}

async function leaveChallenge(challengeId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/challenges/${challengeId}/memberships/me`)
  return response.data
}

async function deleteChallenge(challengeId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/challenges/${challengeId}`)
  return response.data
}

export function useChallengesApi() {
  return useApi(getChallenges)
}

export function useCreateChallengeApi() {
  return useApi(createChallenge)
}

export function useJoinChallengeApi() {
  return useApi(joinChallenge)
}

export function useUpdateChallengeProgressApi() {
  return useApi(updateProgress)
}

export function useLeaveChallengeApi() {
  return useApi(leaveChallenge)
}

export function useDeleteChallengeApi() {
  return useApi(deleteChallenge)
}
