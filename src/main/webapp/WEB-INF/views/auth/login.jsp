<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
String emailValue = (String) request.getAttribute("email");
if (emailValue == null) {
    emailValue = "";
}
%>
<main class="auth-layout">
  <div class="auth-shell">
    <section class="auth-brand-panel">
      <h1>YumYum MVC</h1>
      <p class="summary mt-3">정적 프론트 프로젝트를 JSP와 Servlet MVC 구조로 전환한 버전입니다.</p>
      <div class="mt-4">
        <div class="pill mb-2"><i class="bi bi-diagram-3"></i>Controller - Service - Repository</div>
        <div class="pill"><i class="bi bi-kanban"></i>JSP 기반 화면 렌더링</div>
      </div>
    </section>
    <section class="auth-form-panel">
      <h2 class="fw-bold mb-3">로그인</h2>
      <p class="subtle-text">데모 계정으로 바로 확인할 수 있습니다.</p>
      <form method="post" action="<%= request.getContextPath() %>/auth/login" class="mt-4">
        <div class="mb-3">
          <label class="form-label fw-semibold" for="email">이메일</label>
          <!-- <input class="form-control" type="email" id="email" name="email" value="<%= emailValue %>" required> -->
          <input class="form-control" type="email" id="email" name="email" value="demo@yamyam.com" required>
        </div>
        <div class="mb-3">
          <label class="form-label fw-semibold" for="password">비밀번호</label>
          <input class="form-control" type="password" id="password" name="password" value="Demo1234!" required>
        </div>
        <button class="btn btn-success w-100" type="submit">로그인</button>
      </form>
      <div class="mt-3 subtle-text">
        계정이 없다면 <a class="link-button" href="<%= request.getContextPath() %>/auth/signup">회원가입</a>으로 이동하세요.
      </div>
    </section>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
