<script setup lang="ts">
import { useSessionStore } from '@/stores/session'

const sessionStore = useSessionStore()
</script>

<template>
  <div v-if="sessionStore.alertMsg" class="container mt-4">
    <div :class="`alert alert-${sessionStore.alertType}`">
      {{ sessionStore.alertMsg }}
    </div>
  </div>
</template>
