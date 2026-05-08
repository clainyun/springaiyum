<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.ssafy.yumyum.model.Meal,com.ssafy.yumyum.model.FoodItem,com.ssafy.yumyum.model.MealAnalysis,com.ssafy.yumyum.model.DailyGoal,com.ssafy.yumyum.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
Meal meal = (Meal) request.getAttribute("meal");
MealAnalysis analysis = (MealAnalysis) request.getAttribute("analysis");
DailyGoal dailyGoal = (DailyGoal) request.getAttribute("dailyGoal");
%>
<main class="page-shell">
  <div class="container" style="max-width: 1180px;">
    <section class="page-heading">
      <div>
        <h2><%= ViewHelper.mealTypeLabel(meal.getMealType()) %> 식단 상세</h2>
        <div class="subtle-text mt-2"><%= ViewHelper.formatDate(meal.getMealDate()) %> 기록 | AI 분석과 영양 요약을 함께 보여줍니다.</div>
      </div>
      <div class="d-flex gap-2">
        <a class="btn btn-outline-success" href="<%= request.getContextPath() %>/community">커뮤니티 공유</a>
        <a class="btn btn-outline-primary" href="<%= request.getContextPath() %>/meals/edit?mealId=<%= meal.getId() %>">수정</a>
        <form method="post" action="<%= request.getContextPath() %>/meals/delete">
          <input type="hidden" name="mealId" value="<%= meal.getId() %>">
          <button class="btn btn-outline-danger" type="submit">삭제</button>
        </form>
      </div>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>식단 정보</h5>
              <div class="mini-note"><%= meal.getMemo() %></div>
            </div>
            <span class="soft-pill"><%= ViewHelper.mealTypeLabel(meal.getMealType()) %></span>
          </div>
          <div class="table-responsive">
            <table class="table align-middle">
              <thead>
                <tr>
                  <th>음식</th>
                  <th>분류</th>
                  <th>중량</th>
                  <th>칼로리</th>
                  <th>탄수화물</th>
                  <th>단백질</th>
                  <th>지방</th>
                </tr>
              </thead>
              <tbody>
                <% for (FoodItem food : meal.getFoods()) { %>
                <tr>
                  <td><%= food.getName() %></td>
                  <td><%= food.getCategory() %></td>
                  <td><%= (int) food.getGrams() %>g</td>
                  <td><%= (int) food.getEnergy() %> kcal</td>
                  <td><%= food.getCarbs() %>g</td>
                  <td><%= food.getProtein() %>g</td>
                  <td><%= food.getFat() %>g</td>
                </tr>
                <% } %>
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>영양 분석</h5>
              <div class="mini-note">칼로리와 탄단지 비율을 기준으로 점수를 계산합니다.</div>
            </div>
          </div>
          <div class="mini-grid">
            <div class="metric-card">
              <div class="label">칼로리</div>
              <div class="value"><%= (int) analysis.getNutrition().getCalories() %></div>
              <div class="meta">하루 목표 <%= dailyGoal.getCalories() %> kcal</div>
            </div>
            <div class="metric-card">
              <div class="label">점수</div>
              <div class="value"><%= analysis.getGrade() %></div>
              <div class="meta"><%= analysis.getScore() %>점</div>
            </div>
          </div>
          <div class="mt-3">
            <div class="mini-note">탄수화물 <strong><%= analysis.getNutrition().getCarbsPct() %>%</strong> | 단백질 <strong><%= analysis.getNutrition().getProteinPct() %>%</strong> | 지방 <strong><%= analysis.getNutrition().getFatPct() %>%</strong></div>
          </div>
        </section>

        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>AI 식단 코멘트</h5>
              <div class="mini-note">서비스 계층에서 생성한 식단 분석 결과입니다.</div>
            </div>
          </div>
          <div class="ai-callout">
            <div class="fw-semibold"><%= analysis.getHeadline() %></div>
            <div class="mini-note mt-2"><%= analysis.getNextAction() %></div>
          </div>
          <ul class="mt-3 mb-0 ps-3">
            <% for (String insight : analysis.getInsights()) { %>
            <li class="mb-2"><%= insight %></li>
            <% } %>
          </ul>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
