<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>

<main class="page-shell">
  <div class="container">
    <section class="surface-card">
      <h1>로그인 성공</h1>
      <p>Spring MVC 방식으로 로그인 후 홈 화면에 도착했습니다.</p>

      <div class="mt-4">
        <a class="btn btn-success" href="<%= request.getContextPath() %>/meals">식단 관리</a>
        <a class="btn btn-outline-success" href="<%= request.getContextPath() %>/auth/logout">로그아웃</a>
      </div>
    </section>
  </div>
</main>

<%@ include file="../common/footer.jspf" %>