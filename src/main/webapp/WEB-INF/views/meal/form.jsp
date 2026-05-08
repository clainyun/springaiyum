<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Set,com.yamyam.model.Meal,com.yamyam.model.FoodItem,com.yamyam.model.FoodRecommendation,com.yamyam.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
Meal meal = (Meal) request.getAttribute("meal");
boolean editMode = Boolean.TRUE.equals(request.getAttribute("editMode"));
List<FoodItem> catalogFoods = (List<FoodItem>) request.getAttribute("catalogFoods");
List<FoodItem> selectedFoods = (List<FoodItem>) request.getAttribute("selectedFoods");
Set<String> selectedCodeSet = (Set<String>) request.getAttribute("selectedCodeSet");
List<FoodRecommendation> recommendations = (List<FoodRecommendation>) request.getAttribute("recommendations");
String keyword = (String) request.getAttribute("keyword");
String mealDate = meal != null && meal.getMealDate() != null ? meal.getMealDate().toString() : "";
String mealType = meal != null && meal.getMealType() != null ? meal.getMealType() : "lunch";
String memo = meal != null && meal.getMemo() != null ? meal.getMemo() : "";
%>
<main class="page-shell">
  <div class="container" style="max-width: 1180px;">
    <section class="page-heading">
      <div>
        <h2><%= editMode ? "식단 수정" : "식단 등록" %></h2>
        <div class="subtle-text mt-2">식사 정보와 음식 DB 선택을 이용해 JSP 폼에서 식단을 저장합니다.</div>
      </div>
      <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/meals">목록으로</a>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <form method="post" action="<%= request.getContextPath() %><%= editMode ? "/meals/edit" : "/meals/new" %>">
            <% if (editMode && meal != null) { %>
            <input type="hidden" name="mealId" value="<%= meal.getId() %>">
            <% } %>
            <div class="row g-3">
              <div class="col-md-4">
                <label class="form-label fw-semibold">날짜</label>
                <input class="form-control" type="date" name="mealDate" value="<%= mealDate %>" required>
              </div>
              <div class="col-md-4">
                <label class="form-label fw-semibold">식사 유형</label>
                <select class="form-select" name="mealType">
                  <option value="breakfast" <%= "breakfast".equals(mealType) ? "selected" : "" %>>아침</option>
                  <option value="lunch" <%= "lunch".equals(mealType) ? "selected" : "" %>>점심</option>
                  <option value="dinner" <%= "dinner".equals(mealType) ? "selected" : "" %>>저녁</option>
                  <option value="snack" <%= "snack".equals(mealType) ? "selected" : "" %>>간식</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label fw-semibold">메모</label>
                <input class="form-control" type="text" name="memo" value="<%= memo %>" maxlength="200">
              </div>
            </div>

            <div class="section-title mt-4">
              <div>
                <h5>음식 DB 선택</h5>
                <div class="mini-note">Selection Sort는 저장 시 선택 음식들을 칼로리 높은 순으로 정렬합니다.</div>
              </div>
            </div>

            <div class="row g-3 mb-3">
              <div class="col-md-8">
                <input class="form-control" type="text" name="keyword" value="<%= keyword == null ? "" : keyword %>" placeholder="음식명 또는 분류 검색">
              </div>
              <div class="col-md-4 d-grid">
                <button class="btn btn-outline-secondary" formaction="<%= request.getContextPath() %><%= editMode ? "/meals/edit" : "/meals/new" %>" formmethod="get" type="submit">검색 반영</button>
              </div>
            </div>

            <div class="table-responsive">
              <table class="table align-middle">
                <thead>
                  <tr>
                    <th>선택</th>
                    <th>음식</th>
                    <th>분류</th>
                    <th>100g 기준 kcal</th>
                    <th>중량(g)</th>
                  </tr>
                </thead>
                <tbody>
                  <% for (FoodItem food : catalogFoods) {
                       boolean checked = selectedCodeSet != null && selectedCodeSet.contains(food.getCode());
                       double gramsValue = 100;
                       if (selectedFoods != null) {
                           for (FoodItem selected : selectedFoods) {
                               if (food.getCode().equals(selected.getCode())) {
                                   gramsValue = selected.getGrams();
                               }
                           }
                       }
                  %>
                  <tr>
                    <td><input type="checkbox" name="foodCode" value="<%= food.getCode() %>" <%= checked ? "checked" : "" %>></td>
                    <td><%= food.getName() %></td>
                    <td><%= food.getCategory() %></td>
                    <td><%= (int) food.getEnergy() %></td>
                    <td><input class="form-control" style="min-width:100px;" type="number" name="grams_<%= food.getCode() %>" value="<%= gramsValue %>" min="50" max="500"></td>
                  </tr>
                  <% } %>
                </tbody>
              </table>
            </div>

            <button class="btn btn-success w-100 mt-3" type="submit"><%= editMode ? "식단 수정" : "식단 저장" %></button>
          </form>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>선택 음식</h5>
              <div class="mini-note">현재 폼에 반영된 음식 목록입니다.</div>
            </div>
          </div>
          <% if (selectedFoods == null || selectedFoods.isEmpty()) { %>
          <div class="empty-state">
            <i class="bi bi-basket"></i>
            <p class="mb-0">선택한 음식이 없습니다.</p>
          </div>
          <% } else { for (FoodItem food : selectedFoods) { %>
          <div class="selected-food-row">
            <div>
              <div class="fw-semibold"><%= food.getName() %></div>
              <div class="mini-note"><%= food.getCategory() %></div>
            </div>
            <div class="mini-note"><%= (int) food.getGrams() %>g | <%= (int) food.getEnergy() %> kcal</div>
          </div>
          <% }} %>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>추천 음식</h5>
              <div class="mini-note">Counting Sort로 목표 칼로리와 차이가 적은 음식을 추천합니다.</div>
            </div>
          </div>
          <% if (recommendations == null || recommendations.isEmpty()) { %>
          <div class="empty-state">
            <i class="bi bi-stars"></i>
            <p class="mb-0">추천할 음식이 없습니다.</p>
          </div>
          <% } else { for (FoodRecommendation recommendation : recommendations) { %>
          <div class="recommend-card mb-3">
            <div class="fw-semibold"><%= recommendation.getFood().getName() %></div>
            <div class="mini-note mt-2"><%= recommendation.getFood().getCategory() %></div>
            <div class="mini-note mt-2"><%= (int) recommendation.getFood().getEnergy() %> kcal | 차이 <%= recommendation.getEnergyGap() %> kcal</div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
