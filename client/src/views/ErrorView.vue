<template>
  <main class="page-shell">
    <div class="container">
      <section class="surface-card">
        <div class="empty-state">
          <i class="bi bi-exclamation-triangle"></i>
          <h1 class="h4 mb-2">페이지를 찾을 수 없습니다.</h1>
          <p class="mb-3">요청한 화면이 없거나 이동되었습니다.</p>
          <RouterLink class="btn btn-success" :to="{ name: 'home' }">홈으로 이동</RouterLink>
        </div>
      </section>
    </div>
  </main>
</template>
