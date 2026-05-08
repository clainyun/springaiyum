<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<main class="auth-layout">
  <div class="auth-shell">
    <section class="auth-brand-panel">
      <h1>회원가입</h1>
      <p class="summary mt-3">사용자 정보와 신체 정보를 저장하고, 이후 JSP 화면에서 개인화된 목표를 계산합니다.</p>
      <div class="mt-4">
        <div class="pill mb-2"><i class="bi bi-person-vcard"></i>회원 CRUD</div>
        <div class="pill"><i class="bi bi-heart-pulse"></i>일일 목표 자동 계산</div>
      </div>
    </section>
    <section class="auth-form-panel">
      <form method="post" action="<%= request.getContextPath() %>/auth/signup">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label fw-semibold">이메일</label>
            <input class="form-control" type="email" name="email" required>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">비밀번호</label>
            <input class="form-control" type="password" name="password" required>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">닉네임</label>
            <input class="form-control" type="text" name="nickname" required>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold">목표</label>
            <select class="form-select" name="goal">
              <option value="health">건강 유지</option>
              <option value="diet">체중 감량</option>
              <option value="muscle">근육 증가</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">성별</label>
            <select class="form-select" name="gender">
              <option value="male">남성</option>
              <option value="female">여성</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label fw-semibold">출생 연도</label>
            <input class="form-control" type="number" name="birthYear" value="1998" min="1940" max="2010">
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">키</label>
            <input class="form-control" type="number" name="height" value="165" min="120" max="230">
          </div>
          <div class="col-md-2">
            <label class="form-label fw-semibold">몸무게</label>
            <input class="form-control" type="number" name="weight" value="60" min="30" max="250">
          </div>
          <div class="col-12">
            <label class="form-label fw-semibold">건강 메모</label>
            <input class="form-control" type="text" name="healthNote" placeholder="알레르기나 주의할 항목을 입력하세요.">
          </div>
        </div>
        <button class="btn btn-success w-100 mt-4" type="submit">회원가입</button>
      </form>
    </section>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
