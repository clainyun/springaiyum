<template>
  <main class="page-shell">
    <div class="container">
      <section class="surface-card">
        <div class="section-title">
          <div>
            <h1 class="h4">식단</h1>
            <div class="mini-note">식단 목록 화면 전환 예정</div>
          </div>
          <RouterLink class="btn btn-success btn-sm" :to="{ name: 'meal-new' }">식단 등록</RouterLink>
        </div>
      </section>
    </div>
  </main>
</template>
