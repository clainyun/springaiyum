import { ref } from 'vue'
import { AxiosError } from 'axios'

type ApiFunction<TArgs extends unknown[], TResult> = (...args: TArgs) => Promise<TResult>

function errorMessage(error: unknown) {
  if (error instanceof AxiosError) {
    const responseData = error.response?.data
    if (typeof responseData === 'string') {
      return responseData
    }
    if (responseData && typeof responseData === 'object') {
      if ('detail' in responseData && typeof responseData.detail === 'string') {
        return responseData.detail
      }
      if ('message' in responseData && typeof responseData.message === 'string') {
        return responseData.message
      }
      if ('title' in responseData && typeof responseData.title === 'string') {
        return responseData.title
      }
    }
    return error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return '요청을 처리하지 못했습니다.'
}

export function useApi<TArgs extends unknown[], TResult>(apiFunction: ApiFunction<TArgs, TResult>) {
  const data = ref<TResult | null>(null)
  const error = ref('')
  const isLoading = ref(false)

  async function execute(...args: TArgs) {
    isLoading.value = true
    error.value = ''

    try {
      const result = await apiFunction(...args)
      data.value = result
      return result
    } catch (caughtError) {
      error.value = errorMessage(caughtError)
      throw caughtError
    } finally {
      isLoading.value = false
    }
  }

  return {
    data,
    error,
    isLoading,
    execute,
  }
}
