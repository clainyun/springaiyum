<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  useCommunityBoardApi,
  useCreateCommunityCommentApi,
  useCreateCommunityPostApi,
  useDeleteCommunityCommentApi,
  useDeleteCommunityPostApi,
  useUpdateCommunityCommentApi,
  useUpdateCommunityPostApi,
} from '@/composables/useCommunityApis'
import { useSessionStore } from '@/stores/session'
import type { CommunityComment, CommunityPost, CommunityPostRequest } from '@/types/api'
import { formatDate, mealTypeLabel } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const boardApi = useCommunityBoardApi()
const createPostApi = useCreateCommunityPostApi()
const updatePostApi = useUpdateCommunityPostApi()
const deletePostApi = useDeleteCommunityPostApi()
const createCommentApi = useCreateCommunityCommentApi()
const updateCommentApi = useUpdateCommunityCommentApi()
const deleteCommentApi = useDeleteCommunityCommentApi()

const selectedCategory = ref('all')
const editPostId = ref('')
const editCommentId = ref('')
const commentForms = ref<Record<string, string>>({})
const editCommentContent = ref('')
const postForm = ref<CommunityPostRequest>({
  category: 'review',
  linkedMealId: '',
  title: '',
  content: '',
})

function queryCategory() {
  return typeof route.query.category === 'string' ? route.query.category : 'all'
}

function categoryLabel(category: string) {
  if (category === 'review') return '식단 리뷰'
  if (category === 'expert') return '전문가 팁'
  if (category === 'free') return '자유 게시판'
  return '커뮤니티'
}

function mealLabel(mealId: string) {
  const meal = boardApi.data.value?.meals.find((item) => item.id === mealId)
  return meal ? `${formatDate(meal.mealDate)} | ${mealTypeLabel(meal.mealType)}` : ''
}

function changeCategory() {
  void router.push({
    name: 'community',
    query: selectedCategory.value === 'all' ? {} : { category: selectedCategory.value },
  })
}

async function loadBoard() {
  selectedCategory.value = queryCategory()
  try {
    await boardApi.execute(selectedCategory.value)
  } catch {
    sessionStore.setAlert(boardApi.error.value || '커뮤니티를 불러오지 못했습니다.', 'danger')
  }
}

function startEditPost(post: CommunityPost) {
  editPostId.value = post.id
  postForm.value = {
    category: post.category,
    linkedMealId: post.linkedMealId || '',
    title: post.title,
    content: post.content,
  }
}

function cancelEditPost() {
  editPostId.value = ''
  postForm.value = {
    category: selectedCategory.value === 'all' ? 'review' : selectedCategory.value,
    linkedMealId: '',
    title: '',
    content: '',
  }
}

async function submitPost() {
  try {
    const response = editPostId.value
      ? await updatePostApi.execute(editPostId.value, postForm.value)
      : await createPostApi.execute(postForm.value)
    sessionStore.setAlert(response.message, 'success')
    cancelEditPost()
    await loadBoard()
  } catch {
    const error = editPostId.value ? updatePostApi.error.value : createPostApi.error.value
    sessionStore.setAlert(error || '게시글을 저장하지 못했습니다.', 'danger')
  }
}

async function removePost(postId: string) {
  if (!window.confirm('게시글을 삭제하시겠습니까?')) return
  try {
    const response = await deletePostApi.execute(postId)
    sessionStore.setAlert(response.message, 'success')
    await loadBoard()
  } catch {
    sessionStore.setAlert(deletePostApi.error.value || '게시글을 삭제하지 못했습니다.', 'danger')
  }
}

async function submitComment(postId: string) {
  try {
    const response = await createCommentApi.execute(postId, { content: commentForms.value[postId] || '' })
    commentForms.value[postId] = ''
    sessionStore.setAlert(response.message, 'success')
    await loadBoard()
  } catch {
    sessionStore.setAlert(createCommentApi.error.value || '댓글을 등록하지 못했습니다.', 'danger')
  }
}

function startEditComment(comment: CommunityComment) {
  editCommentId.value = comment.id
  editCommentContent.value = comment.content
}

async function submitEditComment(commentId: string) {
  try {
    const response = await updateCommentApi.execute(commentId, { content: editCommentContent.value })
    editCommentId.value = ''
    editCommentContent.value = ''
    sessionStore.setAlert(response.message, 'success')
    await loadBoard()
  } catch {
    sessionStore.setAlert(updateCommentApi.error.value || '댓글을 수정하지 못했습니다.', 'danger')
  }
}

async function removeComment(commentId: string) {
  if (!window.confirm('댓글을 삭제하시겠습니까?')) return
  try {
    const response = await deleteCommentApi.execute(commentId)
    sessionStore.setAlert(response.message, 'info')
    await loadBoard()
  } catch {
    sessionStore.setAlert(deleteCommentApi.error.value || '댓글을 삭제하지 못했습니다.', 'danger')
  }
}

watch(
  () => route.query,
  () => {
    void loadBoard()
  },
  { immediate: true },
)
</script>

<template>
  <main class="page-shell">
    <div class="container">
      <section class="page-heading">
        <div>
          <h2>커뮤니티</h2>
          <div class="subtle-text mt-2">게시글과 댓글 CRUD를 Spring MVC와 RESTful 라우팅으로 정리했습니다.</div>
        </div>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>{{ editPostId ? '게시글 수정' : '게시글 작성' }}</h5>
                <div class="mini-note">F114 게시글 CRUD, F115 댓글 CRUD</div>
              </div>
            </div>

            <form @submit.prevent="submitPost">
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label fw-semibold">분류</label>
                  <select v-model="postForm.category" class="form-select">
                    <option value="review">식단 리뷰</option>
                    <option value="expert">전문가 팁</option>
                    <option value="free">자유 게시판</option>
                  </select>
                </div>
                <div class="col-md-8">
                  <label class="form-label fw-semibold">연결 식단</label>
                  <select v-model="postForm.linkedMealId" class="form-select">
                    <option value="">연결하지 않음</option>
                    <option v-for="meal in boardApi.data.value?.meals || []" :key="meal.id" :value="meal.id">
                      {{ mealLabel(meal.id) }}
                    </option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold">제목</label>
                  <input v-model="postForm.title" class="form-control" type="text" />
                </div>
                <div class="col-12">
                  <label class="form-label fw-semibold">내용</label>
                  <textarea v-model="postForm.content" class="form-control" rows="5"></textarea>
                </div>
              </div>
              <button class="btn btn-success w-100 mt-4" type="submit">
                {{ editPostId ? '게시글 수정' : '게시글 등록' }}
              </button>
              <button
                v-if="editPostId"
                class="btn btn-outline-secondary w-100 mt-2"
                type="button"
                @click="cancelEditPost"
              >
                취소
              </button>
            </form>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>게시판 보드</h5>
                <div class="mini-note">게시글 목록과 댓글 작성, 수정, 삭제를 한 화면에서 처리합니다.</div>
              </div>
              <select v-model="selectedCategory" class="form-select" style="max-width: 180px" @change="changeCategory">
                <option value="all">전체</option>
                <option value="review">식단 리뷰</option>
                <option value="expert">전문가 팁</option>
                <option value="free">자유 게시판</option>
              </select>
            </div>

            <div v-if="!boardApi.data.value?.posts.length" class="empty-state">
              <i class="bi bi-chat-left-dots"></i>
              <p class="mb-0">게시글이 없습니다.</p>
            </div>
            <div v-for="post in boardApi.data.value?.posts || []" v-else :key="post.id" class="community-card mb-3">
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <div class="d-flex gap-2 mb-2">
                    <span class="tag">{{ categoryLabel(post.category) }}</span>
                    <span v-if="post.linkedMealId" class="tag">연결 식단</span>
                  </div>
                  <div class="fw-semibold fs-5">{{ post.title }}</div>
                  <div class="mini-note mt-2">{{ post.authorName }} | {{ post.createdAt }}</div>
                  <div class="mt-3">{{ post.content }}</div>
                </div>
                <div v-if="post.canEdit" class="d-flex flex-column gap-2">
                  <button class="btn btn-outline-primary btn-sm" type="button" @click="startEditPost(post)">
                    수정
                  </button>
                  <button class="btn btn-outline-danger btn-sm" type="button" @click="removePost(post.id)">
                    삭제
                  </button>
                </div>
              </div>

              <div class="mt-4 pt-3 border-top">
                <form class="mb-3" @submit.prevent="submitComment(post.id)">
                  <div class="input-group">
                    <input
                      v-model="commentForms[post.id]"
                      class="form-control"
                      type="text"
                      placeholder="댓글을 입력하세요"
                    />
                    <button class="btn btn-success" type="submit">등록</button>
                  </div>
                </form>

                <div v-if="post.comments.length === 0" class="mini-note">댓글이 없습니다.</div>
                <div v-for="comment in post.comments" v-else :key="comment.id" class="comment-row mb-2">
                  <div class="d-flex justify-content-between align-items-start">
                    <div style="width: 100%">
                      <div class="fw-semibold">{{ comment.authorName }}</div>
                      <div class="mini-note mt-1">{{ comment.createdAt }}</div>
                      <form v-if="editCommentId === comment.id" class="mt-2" @submit.prevent="submitEditComment(comment.id)">
                        <div class="input-group">
                          <input v-model="editCommentContent" class="form-control" type="text" />
                          <button class="btn btn-outline-primary" type="submit">수정</button>
                        </div>
                        <button
                          class="btn btn-outline-secondary btn-sm mt-2"
                          type="button"
                          @click="editCommentId = ''"
                        >
                          취소
                        </button>
                      </form>
                      <div v-else class="mt-2">{{ comment.content }}</div>
                    </div>
                    <div v-if="comment.canEdit" class="d-flex gap-2">
                      <button class="btn btn-outline-primary btn-sm" type="button" @click="startEditComment(comment)">
                        수정
                      </button>
                      <button class="btn btn-outline-danger btn-sm" type="button" @click="removeComment(comment.id)">
                        삭제
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
