<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ssafy.yumyum.model.User,com.ssafy.yumyum.model.DailyGoal,com.ssafy.yumyum.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
User profileUser = (User) request.getAttribute("currentUser");
DailyGoal dailyGoal = (DailyGoal) request.getAttribute("dailyGoal");
Integer mealCount = (Integer) request.getAttribute("mealCount");
Integer followingCount = (Integer) request.getAttribute("followingCount");
Integer followerCount = (Integer) request.getAttribute("followerCount");
Integer joinedChallengeCount = (Integer) request.getAttribute("joinedChallengeCount");
%>
<main class="page-shell">
  <div class="container" style="max-width: 1120px;">
    <section class="page-heading">
      <div>
        <h2>프로필</h2>
        <div class="subtle-text mt-2">회원 정보 조회/수정과 계정 관리 기능을 JSP 폼으로 제공합니다.</div>
      </div>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="d-flex align-items-center gap-3">
            <div class="avatar-badge"><%= profileUser.getNickname().substring(0, 1) %></div>
            <div>
              <h4 class="mb-1"><%= profileUser.getNickname() %></h4>
              <div class="subtle-text"><%= profileUser.getEmail() %></div>
              <div class="d-flex gap-2 mt-2">
                <span class="tag"><%= ViewHelper.goalLabel(profileUser.getGoal()) %></span>
                <span class="tag"><%= ViewHelper.genderLabel(profileUser.getGender()) %></span>
              </div>
            </div>
          </div>
          <div class="detail-grid mt-4">
            <div class="recommend-card"><div class="mini-note">출생 연도</div><div class="fw-semibold mt-2"><%= profileUser.getBirthYear() %></div></div>
            <div class="recommend-card"><div class="mini-note">키</div><div class="fw-semibold mt-2"><%= profileUser.getHeight() %> cm</div></div>
            <div class="recommend-card"><div class="mini-note">몸무게</div><div class="fw-semibold mt-2"><%= profileUser.getWeight() %> kg</div></div>
            <div class="recommend-card"><div class="mini-note">건강 메모</div><div class="fw-semibold mt-2"><%= ViewHelper.nvl(profileUser.getHealthNote(), "없음") %></div></div>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>프로필 수정</h5>
              <div class="mini-note">회원 수정 F108 및 계정 관리 기능입니다.</div>
            </div>
          </div>
          <form method="post" action="<%= request.getContextPath() %>/profile">
            <input type="hidden" name="action" value="update">
            <div class="row g-3">
              <div class="col-md-6"><label class="form-label fw-semibold">이메일</label><input class="form-control" type="email" name="email" value="<%= profileUser.getEmail() %>"></div>
              <div class="col-md-6"><label class="form-label fw-semibold">닉네임</label><input class="form-control" type="text" name="nickname" value="<%= profileUser.getNickname() %>"></div>
              <div class="col-md-6"><label class="form-label fw-semibold">새 비밀번호</label><input class="form-control" type="password" name="password" placeholder="비워두면 유지됩니다."></div>
              <div class="col-md-6"><label class="form-label fw-semibold">목표</label><select class="form-select" name="goal">
                <option value="health" <%= "health".equals(profileUser.getGoal()) ? "selected" : "" %>>건강 유지</option>
                <option value="diet" <%= "diet".equals(profileUser.getGoal()) ? "selected" : "" %>>체중 감량</option>
                <option value="muscle" <%= "muscle".equals(profileUser.getGoal()) ? "selected" : "" %>>근육 증가</option>
              </select></div>
              <div class="col-md-3"><label class="form-label fw-semibold">성별</label><select class="form-select" name="gender"><option value="male" <%= "male".equals(profileUser.getGender()) ? "selected" : "" %>>남성</option><option value="female" <%= "female".equals(profileUser.getGender()) ? "selected" : "" %>>여성</option></select></div>
              <div class="col-md-3"><label class="form-label fw-semibold">출생 연도</label><input class="form-control" type="number" name="birthYear" value="<%= profileUser.getBirthYear() %>"></div>
              <div class="col-md-3"><label class="form-label fw-semibold">키</label><input class="form-control" type="number" name="height" value="<%= profileUser.getHeight() %>"></div>
              <div class="col-md-3"><label class="form-label fw-semibold">몸무게</label><input class="form-control" type="number" name="weight" value="<%= profileUser.getWeight() %>"></div>
              <div class="col-12"><label class="form-label fw-semibold">건강 메모</label><input class="form-control" type="text" name="healthNote" value="<%= profileUser.getHealthNote() %>"></div>
            </div>
            <button class="btn btn-success w-100 mt-4" type="submit">프로필 저장</button>
          </form>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>일일 목표</h5>
              <div class="mini-note">Service 계층에서 Harris-Benedict 방식으로 계산한 목표입니다.</div>
            </div>
          </div>
          <div class="mini-grid">
            <div class="metric-card"><div class="label">칼로리</div><div class="value"><%= dailyGoal.getCalories() %></div><div class="meta">kcal</div></div>
            <div class="metric-card"><div class="label">탄수화물</div><div class="value"><%= dailyGoal.getCarbs() %>g</div><div class="meta">하루 목표</div></div>
            <div class="metric-card"><div class="label">단백질</div><div class="value"><%= dailyGoal.getProtein() %>g</div><div class="meta">하루 목표</div></div>
            <div class="metric-card"><div class="label">지방</div><div class="value"><%= dailyGoal.getFat() %>g</div><div class="meta">하루 목표</div></div>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>활동 요약</h5>
              <div class="mini-note">식단, 소셜, 챌린지 활동 집계입니다.</div>
            </div>
          </div>
          <div class="detail-grid">
            <div class="recommend-card"><div class="mini-note">식단 기록</div><div class="fw-bold fs-4 mt-2"><%= mealCount %></div></div>
            <div class="recommend-card"><div class="mini-note">팔로잉</div><div class="fw-bold fs-4 mt-2"><%= followingCount %></div></div>
            <div class="recommend-card"><div class="mini-note">팔로워</div><div class="fw-bold fs-4 mt-2"><%= followerCount %></div></div>
            <div class="recommend-card"><div class="mini-note">참여 챌린지</div><div class="fw-bold fs-4 mt-2"><%= joinedChallengeCount %></div></div>
          </div>
        </section>

        <section class="surface-card danger-panel">
          <div class="section-title">
            <div>
              <h5>계정 관리</h5>
              <div class="mini-note">비활성화와 영구 삭제를 지원합니다.</div>
            </div>
          </div>
          <form method="post" action="<%= request.getContextPath() %>/profile" class="mb-2">
            <input type="hidden" name="action" value="deactivate">
            <button class="btn btn-outline-warning w-100" type="submit">계정 비활성화</button>
          </form>
          <form method="post" action="<%= request.getContextPath() %>/profile">
            <input type="hidden" name="action" value="delete">
            <button class="btn btn-outline-danger w-100" type="submit">계정 영구 삭제</button>
          </form>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
