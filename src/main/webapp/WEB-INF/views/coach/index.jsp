<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map,com.ssafy.yumyum.model.CoachAdvice,com.ssafy.yumyum.model.WorkoutSession,com.ssafy.yumyum.model.MealAnalysis,com.ssafy.yumyum.model.NutritionSummary,com.ssafy.yumyum.model.DailyGoal,com.ssafy.yumyum.model.ChallengeMembership,com.ssafy.yumyum.model.Challenge" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
CoachAdvice coachAdvice = (CoachAdvice) request.getAttribute("coachAdvice");
NutritionSummary todaySummary = (NutritionSummary) request.getAttribute("todaySummary");
DailyGoal dailyGoal = (DailyGoal) request.getAttribute("dailyGoal");
List<ChallengeMembership> memberships = (List<ChallengeMembership>) request.getAttribute("memberships");
Map<String, Challenge> challengeMap = (Map<String, Challenge>) request.getAttribute("challengeMap");
int todayPct = dailyGoal.getCalories() == 0 ? 0 : (int) Math.round((todaySummary.getCalories() / dailyGoal.getCalories()) * 100);
%>
<main class="page-shell">
  <div class="container">
    <section class="hero-card mb-4">
      <div class="page-heading mb-0">
        <div>
          <div class="pill mb-3"><i class="bi bi-stars"></i>F116 + F117 대응</div>
          <h1>AI 코치</h1>
          <p class="mb-0 mt-3 fs-5 text-white-50"><%= coachAdvice.getSummary() %></p>
        </div>
      </div>
    </section>
    <section class="mini-grid mb-4">
      <article class="metric-card"><div class="label">오늘 칼로리</div><div class="value"><%= (int) todaySummary.getCalories() %></div><div class="meta">목표 <%= dailyGoal.getCalories() %> kcal</div></article>
      <article class="metric-card"><div class="label">오늘 목표 비율</div><div class="value"><%= todayPct %>%</div><div class="meta">칼로리 기준</div></article>
      <article class="metric-card"><div class="label">단백질</div><div class="value"><%= (int) todaySummary.getProtein() %>g</div><div class="meta">목표 <%= dailyGoal.getProtein() %>g</div></article>
      <article class="metric-card"><div class="label">회복 메모</div><div class="value" style="font-size:1rem;"><%= coachAdvice.getRecovery() %></div><div class="meta">식단 기반 코칭</div></article>
    </section>
    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>운동 계획</h5><div class="mini-note">Service 계층에서 목표와 섭취량을 바탕으로 생성한 루틴입니다.</div></div></div>
          <div class="ai-callout mb-4">
            <div class="fw-semibold"><%= coachAdvice.getSummary() %></div>
            <div class="mini-note mt-2"><%= coachAdvice.getRecovery() %></div>
          </div>
          <div class="row g-3">
            <% for (WorkoutSession workoutSession : coachAdvice.getSessions()) { %>
            <div class="col-md-6">
              <div class="recommend-card h-100">
                <div class="fw-semibold"><%= workoutSession.getTitle() %></div>
                <div class="mini-note mt-2"><%= workoutSession.getDetail() %></div>
                <span class="tag mt-3"><%= workoutSession.getIntensity() %></span>
              </div>
            </div>
            <% } %>
          </div>
        </section>
        <section class="surface-card">
          <div class="section-title"><div><h5>다음 액션</h5><div class="mini-note">식단과 챌린지를 기준으로 제안합니다.</div></div></div>
          <ul class="mb-0 ps-3">
            <% for (String action : coachAdvice.getNextActions()) { %>
            <li class="mb-2"><%= action %></li>
            <% } %>
          </ul>
        </section>
      </div>
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>최근 식단 분석</h5><div class="mini-note">최근 3개 식단 분석 결과입니다.</div></div></div>
          <% if (coachAdvice.getRecentAnalyses().isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-stars"></i><p class="mb-0">최근 식단 데이터가 없습니다.</p></div>
          <% } else { for (MealAnalysis analysis : coachAdvice.getRecentAnalyses()) { %>
          <div class="community-card mb-3">
            <div class="fw-semibold"><%= analysis.getHeadline() %></div>
            <div class="mini-note mt-2"><%= analysis.getNextAction() %></div>
            <div class="tag mt-3">등급 <%= analysis.getGrade() %> / <%= analysis.getScore() %>점</div>
          </div>
          <% }} %>
        </section>
        <section class="surface-card">
          <div class="section-title"><div><h5>챌린지 연계</h5><div class="mini-note">참여 챌린지와 현재 진행률입니다.</div></div></div>
          <% if (memberships == null || memberships.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-trophy"></i><p class="mb-0">참여 중인 챌린지가 없습니다.</p></div>
          <% } else { for (ChallengeMembership membership : memberships) { Challenge challenge = challengeMap.get(membership.getChallengeId()); %>
          <div class="challenge-card mb-3">
            <div class="fw-semibold"><%= challenge == null ? "챌린지" : challenge.getTitle() %></div>
            <div class="mini-note mt-2"><%= challenge == null ? "" : challenge.getDescription() %></div>
            <div class="mini-note mt-2">진행률 <%= membership.getProgress() %> / <%= challenge == null ? 0 : challenge.getTargetCount() %></div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
