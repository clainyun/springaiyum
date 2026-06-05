<template>
  <main class="page-shell">
    <div class="container">
      <section class="hero-card mb-4">
        <div class="page-heading mb-0">
          <div>
            <div class="pill mb-3"><i class="bi bi-diagram-3"></i>JSP + MVC + 알고리즘 적용</div>
            <h1>냠냠 프로젝트 대시보드</h1>
            <p class="mb-0 mt-3 fs-5 text-white-50">오늘의 식단과 활동을 한눈에 확인하세요.</p>
          </div>
        </div>
      </section>

      <section class="mini-grid mb-4">
        <article class="metric-card">
          <div class="label">오늘 칼로리</div>
          <div class="value">- kcal</div>
          <div class="meta">목표 - kcal</div>
        </article>
        <article class="metric-card">
          <div class="label">탄단지 비율</div>
          <div class="value">-/-/-</div>
          <div class="meta">탄수화물 / 단백질 / 지방</div>
        </article>
        <article class="metric-card">
          <div class="label">팔로잉</div>
          <div class="value">-</div>
          <div class="meta">팔로워 -명</div>
        </article>
        <article class="metric-card">
          <div class="label">AI 코치</div>
          <div class="value">-</div>
          <div class="meta">규칙 기반 분석 준비 중</div>
        </article>
      </section>

      <section class="quick-links mb-4">
        <RouterLink class="quick-link" :to="{ name: 'meal-list' }">
          <span class="icon"><i class="bi bi-journal-text"></i></span>
          <strong>식단 관리</strong>
          <span class="mini-note">목록, 등록, 수정, 삭제</span>
        </RouterLink>
        <RouterLink class="quick-link" :to="{ name: 'community' }">
          <span class="icon"><i class="bi bi-chat-left-dots"></i></span>
          <strong>커뮤니티</strong>
          <span class="mini-note">게시글과 댓글 CRUD</span>
        </RouterLink>
        <RouterLink class="quick-link" :to="{ name: 'coach' }">
          <span class="icon"><i class="bi bi-stars"></i></span>
          <strong>AI 코치</strong>
          <span class="mini-note">운동 및 식단 분석</span>
        </RouterLink>
      </section>

      <div class="split-grid">
        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>최근 식단</h5>
                <div class="mini-note">Quick Sort로 최신순 정렬된 식단 기록입니다.</div>
              </div>
              <RouterLink class="btn btn-outline-success btn-sm" :to="{ name: 'meal-new' }">
                식단 등록
              </RouterLink>
            </div>
            <div class="empty-state">
              <i class="bi bi-journal-x"></i>
              <p class="mb-0">저장된 식단이 없습니다.</p>
            </div>
          </section>
        </div>

        <div class="stack-grid">
          <section class="surface-card">
            <div class="section-title">
              <div>
                <h5>활성 챌린지</h5>
                <div class="mini-note">챌린지 참여 현황을 확인하세요.</div>
              </div>
            </div>
            <div class="empty-state">
              <i class="bi bi-trophy"></i>
              <p class="mb-0">활성 챌린지가 없습니다.</p>
            </div>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
