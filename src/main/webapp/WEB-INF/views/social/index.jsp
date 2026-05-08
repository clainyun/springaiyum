<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map,java.util.Set,com.ssafy.yumyum.model.User,com.ssafy.yumyum.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
List<User> suggestions = (List<User>) request.getAttribute("suggestions");
List<User> following = (List<User>) request.getAttribute("following");
List<User> followers = (List<User>) request.getAttribute("followers");
List<User> leaderboard = (List<User>) request.getAttribute("leaderboard");
Map<String, Integer> followerCounts = (Map<String, Integer>) request.getAttribute("followerCounts");
Set<String> followingIds = (Set<String>) request.getAttribute("followingIds");
%>
<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>소셜</h2>
        <div class="subtle-text mt-2">팔로우 관계 추가/삭제와 목록 조회를 MVC 구조로 처리합니다.</div>
      </div>
    </section>
    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>추천 사용자</h5><div class="mini-note">팔로우 추천 목록입니다.</div></div></div>
          <% if (suggestions == null || suggestions.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-people"></i><p class="mb-0">추천 사용자가 없습니다.</p></div>
          <% } else { for (User target : suggestions) { %>
          <div class="social-card mb-3">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="fw-semibold"><%= target.getNickname() %></div>
                <div class="mini-note"><%= ViewHelper.goalLabel(target.getGoal()) %> | 팔로워 <%= followerCounts.get(target.getId()) == null ? 0 : followerCounts.get(target.getId()) %>명</div>
              </div>
              <form method="post" action="<%= request.getContextPath() %>/social">
                <input type="hidden" name="action" value="follow">
                <input type="hidden" name="targetUserId" value="<%= target.getId() %>">
                <button class="btn btn-outline-success btn-sm" type="submit">팔로우</button>
              </form>
            </div>
          </div>
          <% }} %>
        </section>
        <section class="surface-card">
          <div class="section-title"><div><h5>팔로잉</h5><div class="mini-note">내가 팔로우 중인 사용자입니다.</div></div></div>
          <% if (following == null || following.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-person-x"></i><p class="mb-0">팔로우 중인 사용자가 없습니다.</p></div>
          <% } else { for (User target : following) { %>
          <div class="social-card mb-3">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="fw-semibold"><%= target.getNickname() %></div>
                <div class="mini-note"><%= target.getEmail() %></div>
              </div>
              <form method="post" action="<%= request.getContextPath() %>/social">
                <input type="hidden" name="action" value="unfollow">
                <input type="hidden" name="targetUserId" value="<%= target.getId() %>">
                <button class="btn btn-outline-danger btn-sm" type="submit">언팔로우</button>
              </form>
            </div>
          </div>
          <% }} %>
        </section>
      </div>
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5>팔로워</h5><div class="mini-note">나를 팔로우하는 사용자입니다.</div></div></div>
          <% if (followers == null || followers.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-person-plus"></i><p class="mb-0">아직 팔로워가 없습니다.</p></div>
          <% } else { for (User target : followers) { %>
          <div class="social-card mb-3">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="fw-semibold"><%= target.getNickname() %></div>
                <div class="mini-note"><%= ViewHelper.goalShortLabel(target.getGoal()) %></div>
              </div>
              <% if (followingIds != null && followingIds.contains(target.getId())) { %>
              <form method="post" action="<%= request.getContextPath() %>/social">
                <input type="hidden" name="action" value="unfollow">
                <input type="hidden" name="targetUserId" value="<%= target.getId() %>">
                <button class="btn btn-outline-danger btn-sm" type="submit">언팔로우</button>
              </form>
              <% } else { %>
              <form method="post" action="<%= request.getContextPath() %>/social">
                <input type="hidden" name="action" value="follow">
                <input type="hidden" name="targetUserId" value="<%= target.getId() %>">
                <button class="btn btn-outline-success btn-sm" type="submit">맞팔로우</button>
              </form>
              <% } %>
            </div>
          </div>
          <% }} %>
        </section>
        <section class="surface-card">
          <div class="section-title"><div><h5>리더보드</h5><div class="mini-note">팔로워 수 기준 상위 사용자입니다.</div></div></div>
          <% if (leaderboard == null || leaderboard.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-bar-chart"></i><p class="mb-0">리더보드 데이터가 없습니다.</p></div>
          <% } else { for (int i = 0; i < leaderboard.size(); i++) { User target = leaderboard.get(i); %>
          <div class="social-card mb-3">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="fw-semibold">#<%= i + 1 %> <%= target.getNickname() %></div>
                <div class="mini-note"><%= ViewHelper.goalShortLabel(target.getGoal()) %> | 팔로워 <%= followerCounts.get(target.getId()) == null ? 0 : followerCounts.get(target.getId()) %>명</div>
              </div>
              <span class="tag"><%= followingIds != null && followingIds.contains(target.getId()) ? "팔로잉" : "추천" %></span>
            </div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
