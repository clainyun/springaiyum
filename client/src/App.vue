<script setup lang="ts">
import AppAlert from '@/components/common/AppAlert.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import AppNavbar from '@/components/common/AppNavbar.vue'
</script>

<template>
  <AppNavbar />
  <AppAlert />
  <RouterView />
  <AppFooter />
</template>

<style scoped></style>
