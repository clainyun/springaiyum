<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map,com.ssafy.yumyum.model.Challenge,com.ssafy.yumyum.model.ChallengeMembership,com.ssafy.yumyum.model.ChallengeParticipant,com.ssafy.yumyum.model.User,com.ssafy.yumyum.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
List<Challenge> challenges = (List<Challenge>) request.getAttribute("challenges");
Map<String, ChallengeMembership> membershipMap = (Map<String, ChallengeMembership>) request.getAttribute("membershipMap");
Map<String, List<ChallengeParticipant>> participantMap = (Map<String, List<ChallengeParticipant>>) request.getAttribute("participantMap");
Integer joinedCount = (Integer) request.getAttribute("joinedCount");
Integer completedCount = (Integer) request.getAttribute("completedCount");
Integer createdCount = (Integer) request.getAttribute("createdCount");
User challengeUser = (User) request.getAttribute("currentUser");
%>
<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>챌린지</h2>
        <div class="subtle-text mt-2">챌린지 생성, 참여, 진행률 수정, 삭제를 모두 Controller에서 처리합니다.</div>
      </div>
    </section>
    <section class="mini-grid mb-4">
      <article class="metric-card"><div class="label">참여 중</div><div class="value"><%= joinedCount %></div><div class="meta">현재 참여한 챌린지</div></article>
      <article class="metric-card"><div class="label">완료</div><div class="value"><%= completedCount %></div><div class="meta">완료된 챌린지</div></article>
      <article class="metric-card"><div class="label">생성</div><div class="value"><%= createdCount %></div><div class="meta">내가 만든 챌린지</div></article>
      <article class="metric-card"><div class="label">전체</div><div class="value"><%= challenges == null ? 0 : challenges.size() %></div><div class="meta">등록된 챌린지</div></article>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>챌린지 생성</h5><div class="mini-note">F112, F113 요구사항 대응 구조입니다.</div></div></div>
          <form method="post" action="<%= request.getContextPath() %>/challenges">
            <input type="hidden" name="action" value="create">
            <div class="row g-3">
              <div class="col-md-6"><label class="form-label fw-semibold">제목</label><input class="form-control" type="text" name="title"></div>
              <div class="col-md-3"><label class="form-label fw-semibold">목표 횟수</label><input class="form-control" type="number" name="targetCount" value="7"></div>
              <div class="col-md-3"><label class="form-label fw-semibold">종료일</label><input class="form-control" type="date" name="endDate"></div>
              <div class="col-md-4"><label class="form-label fw-semibold">분류</label><select class="form-select" name="category"><option value="식습관">식습관</option><option value="영양 관리">영양 관리</option><option value="운동">운동</option><option value="수분 섭취">수분 섭취</option></select></div>
              <div class="col-md-8"><label class="form-label fw-semibold">설명</label><input class="form-control" type="text" name="description"></div>
            </div>
            <button class="btn btn-success w-100 mt-4" type="submit">챌린지 생성</button>
          </form>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>챌린지 보드</h5><div class="mini-note">참여자 진행률과 상태를 같이 확인할 수 있습니다.</div></div></div>
          <% if (challenges == null || challenges.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-trophy"></i><p class="mb-0">등록된 챌린지가 없습니다.</p></div>
          <% } else { for (Challenge challenge : challenges) {
               ChallengeMembership membership = membershipMap.get(challenge.getId());
               List<ChallengeParticipant> participants = participantMap.get(challenge.getId());
          %>
          <div class="challenge-card mb-3">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <div class="d-flex align-items-center gap-2 mb-2">
                  <span class="tag"><%= challenge.getCategory() %></span>
                  <% if (challengeUser.getId().equals(challenge.getCreatedBy())) { %><span class="tag">내가 생성</span><% } %>
                </div>
                <div class="fw-semibold fs-5"><%= challenge.getTitle() %></div>
                <div class="mini-note mt-2"><%= challenge.getDescription() %></div>
                <div class="mini-note mt-2">기간 <%= ViewHelper.formatDate(challenge.getStartDate()) %> ~ <%= ViewHelper.formatDate(challenge.getEndDate()) %> | 목표 <%= challenge.getTargetCount() %>회</div>
              </div>
              <span class="soft-pill"><%= membership == null ? "모집 중" : ViewHelper.challengeStatusLabel(membership.getStatus()) %></span>
            </div>
            <div class="mt-3 mini-note">
              <% if (participants != null && !participants.isEmpty()) { for (ChallengeParticipant participant : participants) { %>
              <div class="d-flex justify-content-between"><span><%= participant.getNickname() %></span><strong><%= participant.getProgress() %></strong></div>
              <% }} else { %>참여자가 없습니다.<% } %>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-4">
              <% if (membership == null) { %>
              <form method="post" action="<%= request.getContextPath() %>/challenges">
                <input type="hidden" name="action" value="join">
                <input type="hidden" name="challengeId" value="<%= challenge.getId() %>">
                <button class="btn btn-outline-success btn-sm" type="submit">참여하기</button>
              </form>
              <% } else { %>
              <form class="d-flex gap-2" method="post" action="<%= request.getContextPath() %>/challenges">
                <input type="hidden" name="action" value="progress">
                <input type="hidden" name="challengeId" value="<%= challenge.getId() %>">
                <input class="form-control form-control-sm" style="width:100px;" type="number" name="progress" value="<%= membership.getProgress() %>">
                <button class="btn btn-outline-primary btn-sm" type="submit">진행률 저장</button>
              </form>
              <form method="post" action="<%= request.getContextPath() %>/challenges">
                <input type="hidden" name="action" value="leave">
                <input type="hidden" name="challengeId" value="<%= challenge.getId() %>">
                <button class="btn btn-outline-danger btn-sm" type="submit">나가기</button>
              </form>
              <% } %>
              <% if (challengeUser.getId().equals(challenge.getCreatedBy())) { %>
              <form method="post" action="<%= request.getContextPath() %>/challenges">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="challengeId" value="<%= challenge.getId() %>">
                <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
              </form>
              <% } %>
            </div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
