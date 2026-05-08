<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,com.yamyam.model.Meal,com.yamyam.model.Challenge,com.yamyam.model.CoachAdvice,com.yamyam.model.NutritionSummary,com.yamyam.model.DailyGoal,com.yamyam.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
List<Meal> recentMeals = (List<Meal>) request.getAttribute("recentMeals");
List<Challenge> activeChallenges = (List<Challenge>) request.getAttribute("activeChallenges");
NutritionSummary todaySummary = (NutritionSummary) request.getAttribute("todaySummary");
DailyGoal dailyGoal = (DailyGoal) request.getAttribute("dailyGoal");
CoachAdvice coachAdvice = (CoachAdvice) request.getAttribute("coachAdvice");
Integer followingCount = (Integer) request.getAttribute("followingCount");
Integer followerCount = (Integer) request.getAttribute("followerCount");
%>
<main class="page-shell">
  <div class="container">
    <section class="hero-card mb-4">
      <div class="page-heading mb-0">
        <div>
          <div class="pill mb-3"><i class="bi bi-diagram-3"></i>JSP + MVC + 알고리즘 적용</div>
          <h1>냠냠 프로젝트 대시보드</h1>
          <p class="mb-0 mt-3 fs-5 text-white-50"><%= coachAdvice.getSummary() %></p>
        </div>
      </div>
    </section>

    <section class="mini-grid mb-4">
      <article class="metric-card">
        <div class="label">오늘 칼로리</div>
        <div class="value"><%= (int) todaySummary.getCalories() %> kcal</div>
        <div class="meta">목표 <%= dailyGoal.getCalories() %> kcal</div>
      </article>
      <article class="metric-card">
        <div class="label">탄단지 비율</div>
        <div class="value"><%= todaySummary.getCarbsPct() %>/<%= todaySummary.getProteinPct() %>/<%= todaySummary.getFatPct() %></div>
        <div class="meta">탄수화물 / 단백질 / 지방</div>
      </article>
      <article class="metric-card">
        <div class="label">팔로잉</div>
        <div class="value"><%= followingCount %></div>
        <div class="meta">팔로워 <%= followerCount %>명</div>
      </article>
      <article class="metric-card">
        <div class="label">AI 코치</div>
        <div class="value"><%= coachAdvice.getRecentAnalyses().isEmpty() ? "-" : coachAdvice.getRecentAnalyses().get(0).getGrade() %></div>
        <div class="meta"><%= coachAdvice.getRecovery() %></div>
      </article>
    </section>

    <section class="quick-links mb-4">
      <a class="quick-link" href="<%= request.getContextPath() %>/meals"><span class="icon"><i class="bi bi-journal-text"></i></span><strong>식단 관리</strong><span class="mini-note">목록, 등록, 수정, 삭제</span></a>
      <a class="quick-link" href="<%= request.getContextPath() %>/community"><span class="icon"><i class="bi bi-chat-left-dots"></i></span><strong>커뮤니티</strong><span class="mini-note">게시글과 댓글 CRUD</span></a>
      <a class="quick-link" href="<%= request.getContextPath() %>/coach"><span class="icon"><i class="bi bi-stars"></i></span><strong>AI 코치</strong><span class="mini-note">운동 및 식단 분석</span></a>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>최근 식단</h5>
              <div class="mini-note">Quick Sort로 최신순 정렬된 식단 기록입니다.</div>
            </div>
            <a class="btn btn-outline-success btn-sm" href="<%= request.getContextPath() %>/meals/new">식단 등록</a>
          </div>
          <% if (recentMeals == null || recentMeals.isEmpty()) { %>
          <div class="empty-state">
            <i class="bi bi-journal-x"></i>
            <p class="mb-0">저장된 식단이 없습니다.</p>
          </div>
          <% } else { for (Meal meal : recentMeals) { %>
          <div class="meal-card">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <div class="tag mb-2"><%= ViewHelper.mealTypeLabel(meal.getMealType()) %></div>
                <div class="fw-semibold"><%= ViewHelper.formatDate(meal.getMealDate()) %></div>
                <div class="mini-note mt-2"><%= meal.getMemo() %></div>
              </div>
              <a class="btn btn-outline-primary btn-sm" href="<%= request.getContextPath() %>/meals/detail?mealId=<%= meal.getId() %>">상세 보기</a>
            </div>
          </div>
          <% }} %>
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
          <% if (activeChallenges == null || activeChallenges.isEmpty()) { %>
          <div class="empty-state">
            <i class="bi bi-trophy"></i>
            <p class="mb-0">활성 챌린지가 없습니다.</p>
          </div>
          <% } else { for (Challenge challenge : activeChallenges) { %>
          <div class="challenge-card mb-3">
            <div class="fw-semibold"><%= challenge.getTitle() %></div>
            <div class="mini-note mt-2"><%= challenge.getDescription() %></div>
            <div class="mini-note mt-2">목표 <%= challenge.getTargetCount() %>회 | 종료 <%= ViewHelper.formatDate(challenge.getEndDate()) %></div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
