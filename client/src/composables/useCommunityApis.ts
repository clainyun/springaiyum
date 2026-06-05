import { apiClient } from '@/composables/apiClient'
import { useApi } from '@/composables/useApi'
import type {
  CommunityBoard,
  CommunityCommentRequest,
  CommunityPostRequest,
  MessageResponse,
} from '@/types/api'

async function getCommunityBoard(category = 'all') {
  const response = await apiClient.get<CommunityBoard>('/api/v1/community', {
    params: { category },
  })
  return response.data
}

async function createPost(request: CommunityPostRequest) {
  const response = await apiClient.post<MessageResponse>('/api/v1/community/posts', request)
  return response.data
}

async function updatePost(postId: string, request: CommunityPostRequest) {
  const response = await apiClient.patch<MessageResponse>(`/api/v1/community/posts/${postId}`, request)
  return response.data
}

async function deletePost(postId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/community/posts/${postId}`)
  return response.data
}

async function createComment(postId: string, request: CommunityCommentRequest) {
  const response = await apiClient.post<MessageResponse>(
    `/api/v1/community/posts/${postId}/comments`,
    request,
  )
  return response.data
}

async function updateComment(commentId: string, request: CommunityCommentRequest) {
  const response = await apiClient.patch<MessageResponse>(
    `/api/v1/community/comments/${commentId}`,
    request,
  )
  return response.data
}

async function deleteComment(commentId: string) {
  const response = await apiClient.delete<MessageResponse>(`/api/v1/community/comments/${commentId}`)
  return response.data
}

export function useCommunityBoardApi() {
  return useApi(getCommunityBoard)
}

export function useCreateCommunityPostApi() {
  return useApi(createPost)
}

export function useUpdateCommunityPostApi() {
  return useApi(updatePost)
}

export function useDeleteCommunityPostApi() {
  return useApi(deletePost)
}

export function useCreateCommunityCommentApi() {
  return useApi(createComment)
}

export function useUpdateCommunityCommentApi() {
  return useApi(updateComment)
}

export function useDeleteCommunityCommentApi() {
  return useApi(deleteComment)
}
