<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>

<main class="page-shell">
  <div class="container">
    <section class="hero-card mb-4">
      <div class="page-heading mb-0">
        <div>
          <div class="pill mb-3"><i class="bi bi-stars"></i>오늘의 코칭</div>
          <h1>AI 코치</h1>
          <p class="mb-0 mt-3 fs-5 text-white-50">
            <c:choose>
              <c:when test="${empty coachAdvice.summary}">
                오늘 코칭 데이터를 준비 중입니다.
              </c:when>
              <c:otherwise>
                ${coachAdvice.summary}
              </c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>
    </section>

    <section class="mini-grid mb-4">
      <article class="metric-card">
        <div class="label">오늘 칼로리</div>
        <div class="value"><fmt:formatNumber value="${todaySummary.calories}" maxFractionDigits="0" /></div>
        <div class="meta">목표 ${dailyGoal.calories} kcal</div>
      </article>
      <article class="metric-card">
        <div class="label">오늘 목표 비율</div>
        <div class="value">${todayPct}%</div>
        <div class="meta">칼로리 기준</div>
      </article>
      <article class="metric-card">
        <div class="label">단백질</div>
        <div class="value"><fmt:formatNumber value="${todaySummary.protein}" maxFractionDigits="0" />g</div>
        <div class="meta">목표 ${dailyGoal.protein}g</div>
      </article>
      <article class="metric-card">
        <div class="label">회복 메모</div>
        <div class="value" style="font-size:1rem;">
          <c:choose>
            <c:when test="${empty coachAdvice.recovery}">
              회복 관련 코멘트가 아직 없습니다.
            </c:when>
            <c:otherwise>
              ${coachAdvice.recovery}
            </c:otherwise>
          </c:choose>
        </div>
        <div class="meta">식단 기반 코칭</div>
      </article>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>운동 계획</h5>
              <div class="mini-note">서비스 계산 결과를 바탕으로 생성된 루틴입니다.</div>
            </div>
          </div>

          <div class="ai-callout mb-4">
            <div class="fw-semibold">${coachAdvice.summary}</div>
            <div class="mini-note mt-2">${coachAdvice.recovery}</div>
          </div>

          <div class="row g-3">
            <c:choose>
              <c:when test="${empty coachAdvice.sessions}">
                <div class="col-12">
                  <div class="empty-state">
                    <i class="bi bi-heart-pulse"></i>
                    <p class="mb-0">추천 운동이 아직 없습니다.</p>
                  </div>
                </div>
              </c:when>
              <c:otherwise>
                <c:forEach var="session" items="${coachAdvice.sessions}">
                  <div class="col-md-6">
                    <div class="recommend-card h-100">
                      <div class="fw-semibold">${session.title}</div>
                      <div class="mini-note mt-2">${session.detail}</div>
                      <span class="tag mt-3">${session.intensity}</span>
                    </div>
                  </div>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>다음 액션</h5>
              <div class="mini-note">식단과 챌린지 상태를 기준으로 제안합니다.</div>
            </div>
          </div>

          <c:choose>
            <c:when test="${empty coachAdvice.nextActions}">
              <div class="empty-state">
                <i class="bi bi-check2-circle"></i>
                <p class="mb-0">표시할 다음 액션이 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <ul class="mb-0 ps-3">
                <c:forEach var="action" items="${coachAdvice.nextActions}">
                  <li class="mb-2">${action}</li>
                </c:forEach>
              </ul>
            </c:otherwise>
          </c:choose>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>최근 식단 분석</h5>
              <div class="mini-note">최근 3개의 식단 분석 결과입니다.</div>
            </div>
          </div>

          <c:choose>
            <c:when test="${empty coachAdvice.recentAnalyses}">
              <div class="empty-state">
                <i class="bi bi-stars"></i>
                <p class="mb-0">최근 식단 데이터가 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <c:forEach var="analysis" items="${coachAdvice.recentAnalyses}">
                <div class="community-card mb-3">
                  <div class="fw-semibold">${analysis.headline}</div>
                  <div class="mini-note mt-2">${analysis.nextAction}</div>
                  <div class="tag mt-3">등급 ${analysis.grade} / ${analysis.score}점</div>
                </div>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>챌린지 연계</h5>
              <div class="mini-note">참여 중인 챌린지의 현재 진행도를 보여줍니다.</div>
            </div>
          </div>

          <c:choose>
            <c:when test="${empty memberships}">
              <div class="empty-state">
                <i class="bi bi-trophy"></i>
                <p class="mb-0">참여 중인 챌린지가 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <c:forEach var="membership" items="${memberships}">
                <c:set var="challenge" value="${challengeMap[membership.challengeId]}" />
                <div class="challenge-card mb-3">
                  <div class="fw-semibold">${empty challenge ? '챌린지' : challenge.title}</div>
                  <div class="mini-note mt-2">${empty challenge ? '' : challenge.description}</div>
                  <div class="mini-note mt-2">
                    진행률 ${membership.progress} / ${empty challenge ? 0 : challenge.targetCount}
                  </div>
                </div>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
