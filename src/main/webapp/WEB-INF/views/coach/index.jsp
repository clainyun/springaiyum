<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<c:url var="coachDashboardApiUrl" value="/api/me/coach-dashboard" />
<c:url var="loginUrl" value="/auth/login" />

<main class="page-shell">
  <div class="container" id="coachDashboard" data-api-url="${coachDashboardApiUrl}" data-login-url="${loginUrl}">
    <div id="coachDashboardError" class="alert alert-warning d-none mb-4" role="alert"></div>

    <section class="hero-card mb-4">
      <div class="page-heading mb-0">
        <div>
          <div class="pill mb-3"><i class="bi bi-stars"></i>오늘의 코칭</div>
          <h1>AI 코치</h1>
          <p id="coachSummaryHero" class="mb-0 mt-3 fs-5 text-white-50">코칭 데이터를 불러오는 중입니다.</p>
        </div>
      </div>
    </section>

    <section class="mini-grid mb-4">
      <article class="metric-card">
        <div class="label">오늘 칼로리</div>
        <div id="coachCaloriesValue" class="value">-</div>
        <div id="coachCaloriesMeta" class="meta">목표 정보를 불러오는 중입니다.</div>
      </article>
      <article class="metric-card">
        <div class="label">오늘 목표 비율</div>
        <div id="coachTodayPct" class="value">-</div>
        <div class="meta">칼로리 기준</div>
      </article>
      <article class="metric-card">
        <div class="label">단백질</div>
        <div id="coachProteinValue" class="value">-</div>
        <div id="coachProteinMeta" class="meta">목표 정보를 불러오는 중입니다.</div>
      </article>
      <article class="metric-card">
        <div class="label">회복 메모</div>
        <div id="coachRecoveryMetric" class="value" style="font-size:1rem;">회복 코멘트를 불러오는 중입니다.</div>
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
            <div id="coachSummaryCallout" class="fw-semibold">코칭 데이터를 불러오는 중입니다.</div>
            <div id="coachRecoveryCallout" class="mini-note mt-2">회복 메모를 불러오는 중입니다.</div>
          </div>

          <div id="coachSessions" class="row g-3">
            <div class="col-12">
              <div class="empty-state">
                <i class="bi bi-arrow-repeat"></i>
                <p class="mb-0">추천 운동을 불러오는 중입니다.</p>
              </div>
            </div>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>다음 액션</h5>
              <div class="mini-note">식단과 챌린지 상태를 기준으로 제안합니다.</div>
            </div>
          </div>

          <div id="coachNextActions">
            <div class="empty-state">
              <i class="bi bi-arrow-repeat"></i>
              <p class="mb-0">다음 액션을 불러오는 중입니다.</p>
            </div>
          </div>
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

          <div id="coachRecentAnalyses">
            <div class="empty-state">
              <i class="bi bi-arrow-repeat"></i>
              <p class="mb-0">최근 식단 분석을 불러오는 중입니다.</p>
            </div>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>챌린지 연계</h5>
              <div class="mini-note">참여 중인 챌린지의 현재 진행도를 보여줍니다.</div>
            </div>
          </div>

          <div id="coachChallenges">
            <div class="empty-state">
              <i class="bi bi-arrow-repeat"></i>
              <p class="mb-0">챌린지 진행 현황을 불러오는 중입니다.</p>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</main>

<script>
  (() => {
    const root = document.getElementById("coachDashboard");
    if (!root) {
      return;
    }

    const apiUrl = root.dataset.apiUrl;
    const loginUrl = root.dataset.loginUrl;
    const numberFormatter = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 0 });

    function escapeHtml(value) {
      return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
    }

    function emptyState(iconClass, message) {
      return ""
        + "<div class=\"empty-state\">"
        + "<i class=\"bi " + escapeHtml(iconClass) + "\"></i>"
        + "<p class=\"mb-0\">" + escapeHtml(message) + "</p>"
        + "</div>";
    }

    function formatNumber(value) {
      return numberFormatter.format(Number(value ?? 0));
    }

    function setText(id, value) {
      const target = document.getElementById(id);
      if (target) {
        target.textContent = value;
      }
    }

    function renderSessions(sessions) {
      const target = document.getElementById("coachSessions");
      if (!target) {
        return;
      }
      if (!sessions || sessions.length === 0) {
        target.innerHTML = "<div class=\"col-12\">" + emptyState("bi-heart-pulse", "추천 운동이 아직 없습니다.") + "</div>";
        return;
      }
      target.innerHTML = sessions.map((session) => ""
        + "<div class=\"col-md-6\">"
        + "<div class=\"recommend-card h-100\">"
        + "<div class=\"fw-semibold\">" + escapeHtml(session.title) + "</div>"
        + "<div class=\"mini-note mt-2\">" + escapeHtml(session.detail) + "</div>"
        + "<span class=\"tag mt-3\">" + escapeHtml(session.intensity) + "</span>"
        + "</div>"
        + "</div>"
      ).join("");
    }

    function renderNextActions(actions) {
      const target = document.getElementById("coachNextActions");
      if (!target) {
        return;
      }
      if (!actions || actions.length === 0) {
        target.innerHTML = emptyState("bi-check2-circle", "표시할 다음 액션이 없습니다.");
        return;
      }
      target.innerHTML = ""
        + "<ul class=\"mb-0 ps-3\">"
        + actions.map((action) => "<li class=\"mb-2\">" + escapeHtml(action) + "</li>").join("")
        + "</ul>";
    }

    function renderAnalyses(analyses) {
      const target = document.getElementById("coachRecentAnalyses");
      if (!target) {
        return;
      }
      if (!analyses || analyses.length === 0) {
        target.innerHTML = emptyState("bi-stars", "최근 식단 데이터가 없습니다.");
        return;
      }
      target.innerHTML = analyses.map((analysis) => ""
        + "<div class=\"community-card mb-3\">"
        + "<div class=\"fw-semibold\">" + escapeHtml(analysis.headline) + "</div>"
        + "<div class=\"mini-note mt-2\">" + escapeHtml(analysis.nextAction) + "</div>"
        + "<div class=\"tag mt-3\">등급 " + escapeHtml(analysis.grade) + " / " + formatNumber(analysis.score) + "점</div>"
        + "</div>"
      ).join("");
    }

    function renderChallenges(challenges) {
      const target = document.getElementById("coachChallenges");
      if (!target) {
        return;
      }
      if (!challenges || challenges.length === 0) {
        target.innerHTML = emptyState("bi-trophy", "참여 중인 챌린지가 없습니다.");
        return;
      }
      target.innerHTML = challenges.map((challenge) => ""
        + "<div class=\"challenge-card mb-3\">"
        + "<div class=\"fw-semibold\">" + escapeHtml(challenge.title || "챌린지") + "</div>"
        + "<div class=\"mini-note mt-2\">" + escapeHtml(challenge.description || "") + "</div>"
        + "<div class=\"mini-note mt-2\">진행률 " + formatNumber(challenge.progress) + " / " + formatNumber(challenge.targetCount) + "</div>"
        + "</div>"
      ).join("");
    }

    function renderError(message) {
      const errorBox = document.getElementById("coachDashboardError");
      if (!errorBox) {
        return;
      }
      errorBox.textContent = message;
      errorBox.classList.remove("d-none");
    }

    function renderDashboard(data) {
      setText("coachSummaryHero", data.summary || "오늘 코칭 데이터를 준비 중입니다.");
      setText("coachSummaryCallout", data.summary || "오늘 코칭 데이터를 준비 중입니다.");
      setText("coachRecoveryMetric", data.recovery || "회복 관련 코멘트가 아직 없습니다.");
      setText("coachRecoveryCallout", data.recovery || "회복 관련 코멘트가 아직 없습니다.");
      setText("coachCaloriesValue", formatNumber(data.todaySummary?.calories));
      setText("coachCaloriesMeta", "목표 " + formatNumber(data.dailyGoal?.calories) + " kcal");
      setText("coachTodayPct", formatNumber(data.todayPct) + "%");
      setText("coachProteinValue", formatNumber(data.todaySummary?.protein) + "g");
      setText("coachProteinMeta", "목표 " + formatNumber(data.dailyGoal?.protein) + "g");
      renderSessions(data.sessions);
      renderNextActions(data.nextActions);
      renderAnalyses(data.recentAnalyses);
      renderChallenges(data.challenges);
    }

    async function loadDashboard() {
      try {
        const response = await fetch(apiUrl, {
          headers: {
            "Accept": "application/json"
          }
        });

        if (response.status === 401) {
          window.location.href = loginUrl;
          return;
        }

        if (!response.ok) {
          throw new Error("코치 데이터를 불러오지 못했습니다.");
        }

        const data = await response.json();
        renderDashboard(data);
      } catch (error) {
        renderError(error.message || "코치 데이터를 불러오지 못했습니다.");
      }
    }

    loadDashboard();
  })();
</script>
<%@ include file="../common/footer.jspf" %>
