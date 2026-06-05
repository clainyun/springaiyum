import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export type LoginUser = {
  id: string
  email: string
  nickname: string
}

export type AlertType = 'success' | 'warning' | 'info' | 'danger' | 'secondary'

export const useSessionStore = defineStore('session', () => {
  const loginUser = ref<LoginUser | null>(null)
  const alertMsg = ref('')
  const alertType = ref<AlertType>('secondary')

  const isLoggedIn = computed(() => loginUser.value !== null)

  function setLoginUser(user: LoginUser | null) {
    loginUser.value = user
  }

  function clearLoginUser() {
    loginUser.value = null
  }

  function setAlert(message: string, type: AlertType = 'secondary') {
    alertMsg.value = message
    alertType.value = type
  }

  function clearAlert() {
    alertMsg.value = ''
    alertType.value = 'secondary'
  }

  return {
    loginUser,
    alertMsg,
    alertType,
    isLoggedIn,
    setLoginUser,
    clearLoginUser,
    setAlert,
    clearAlert,
  }
})
