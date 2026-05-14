<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>

<main class="page-shell">
  <div class="container">
    <section class="surface-card">
      <h1>오류가 발생했습니다</h1>

      <p class="mt-3">
        상태 코드: <strong>${statusCode}</strong>
      </p>

      <p>
        ${errorMessage}
      </p>

      <div class="mt-4">
        <a class="btn btn-success" href="<%= request.getContextPath() %>/home">홈으로 이동</a>
        <a class="btn btn-outline-success" href="javascript:history.back()">이전 페이지</a>
      </div>
    </section>
  </div>
</main>

<%@ include file="../common/footer.jspf" %>