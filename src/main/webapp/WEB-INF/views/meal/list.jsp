<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,com.yamyam.model.Meal,com.yamyam.model.User,com.yamyam.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
List<Meal> meals = (List<Meal>) request.getAttribute("meals");
String filterStart = (String) request.getAttribute("filterStart");
String filterEnd = (String) request.getAttribute("filterEnd");
String filterMealType = (String) request.getAttribute("filterMealType");
String sortKey = (String) request.getAttribute("sortKey");
%>
<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>식단 목록</h2>
        <div class="subtle-text mt-2">JSP 화면에서 식단 목록 조회와 정렬 기능을 제공합니다.</div>
      </div>
      <a class="btn btn-success" href="<%= request.getContextPath() %>/meals/new"><i class="bi bi-plus-lg me-1"></i>식단 등록</a>
    </section>

    <section class="surface-card mb-4">
      <form method="get" action="<%= request.getContextPath() %>/meals" class="row g-3 align-items-end">
        <div class="col-md-3">
          <label class="form-label fw-semibold">시작일</label>
          <input class="form-control" type="date" name="startDate" value="<%= filterStart == null ? "" : filterStart %>">
        </div>
        <div class="col-md-3">
          <label class="form-label fw-semibold">종료일</label>
          <input class="form-control" type="date" name="endDate" value="<%= filterEnd == null ? "" : filterEnd %>">
        </div>
        <div class="col-md-2">
          <label class="form-label fw-semibold">식사 유형</label>
          <select class="form-select" name="mealType">
            <option value="">전체</option>
            <option value="breakfast" <%= "breakfast".equals(filterMealType) ? "selected" : "" %>>아침</option>
            <option value="lunch" <%= "lunch".equals(filterMealType) ? "selected" : "" %>>점심</option>
            <option value="dinner" <%= "dinner".equals(filterMealType) ? "selected" : "" %>>저녁</option>
            <option value="snack" <%= "snack".equals(filterMealType) ? "selected" : "" %>>간식</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label fw-semibold">정렬</label>
          <select class="form-select" name="sortKey">
            <option value="dateDesc" <%= "dateDesc".equals(sortKey) ? "selected" : "" %>>최신순</option>
            <option value="dateAsc" <%= "dateAsc".equals(sortKey) ? "selected" : "" %>>오래된순</option>
            <option value="energyDesc" <%= "energyDesc".equals(sortKey) ? "selected" : "" %>>칼로리 높은순</option>
            <option value="scoreDesc" <%= "scoreDesc".equals(sortKey) ? "selected" : "" %>>점수 높은순</option>
          </select>
        </div>
        <div class="col-md-2 d-grid">
          <button class="btn btn-outline-secondary" type="submit">조회</button>
        </div>
      </form>
    </section>

    <section class="surface-card">
      <div class="section-title">
        <div>
          <h5>식단 기록</h5>
          <div class="mini-note">Quick Sort 기반 정렬 결과입니다.</div>
        </div>
      </div>
      <% if (meals == null || meals.isEmpty()) { %>
      <div class="empty-state">
        <i class="bi bi-journal-x"></i>
        <p class="mb-0">조회된 식단이 없습니다.</p>
      </div>
      <% } else { for (Meal meal : meals) { %>
      <div class="meal-card">
        <div class="d-flex justify-content-between align-items-start gap-3">
          <div>
            <div class="d-flex align-items-center gap-2 mb-2">
              <span class="tag"><%= ViewHelper.mealTypeLabel(meal.getMealType()) %></span>
              <span class="mini-note"><%= ViewHelper.formatDate(meal.getMealDate()) %></span>
            </div>
            <div class="mini-note"><%= meal.getFoods().size() %>개 음식</div>
            <div class="mini-note mt-2"><%= meal.getMemo() %></div>
          </div>
          <div class="d-flex gap-2">
            <a class="btn btn-outline-primary btn-sm" href="<%= request.getContextPath() %>/meals/detail?mealId=<%= meal.getId() %>">상세</a>
            <a class="btn btn-outline-success btn-sm" href="<%= request.getContextPath() %>/meals/edit?mealId=<%= meal.getId() %>">수정</a>
            <form method="post" action="<%= request.getContextPath() %>/meals/delete">
              <input type="hidden" name="mealId" value="<%= meal.getId() %>">
              <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
            </form>
          </div>
        </div>
      </div>
      <% }} %>
    </section>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
