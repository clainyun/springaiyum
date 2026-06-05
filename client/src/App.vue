<script setup lang="ts">
import AppAlert from '@/components/common/AppAlert.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import AppNavbar from '@/components/common/AppNavbar.vue'
import { useMyProfileApi } from '@/composables/useUserApis'
import { useSessionStore } from '@/stores/session'

const sessionStore = useSessionStore()
const myProfileApi = useMyProfileApi()

async function restoreSession() {
  if (sessionStore.isLoggedIn) {
    return
  }

  try {
    const user = await myProfileApi.execute()
    sessionStore.setLoginUser(user)
  } catch {
    sessionStore.clearLoginUser()
  }
}

void restoreSession()
</script>

<template>
  <AppNavbar />
  <AppAlert />
  <RouterView />
  <AppFooter />
</template>

<style scoped></style>
