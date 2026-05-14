<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<c:url var="challengesUrl" value="/challenges" />

<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>챌린지</h2>
        <div class="subtle-text mt-2">챌린지 생성, 참여, 진행률 수정과 삭제를 한 화면에서 관리합니다.</div>
      </div>
    </section>

    <section class="mini-grid mb-4">
      <article class="metric-card">
        <div class="label">참여 중</div>
        <div class="value">${joinedCount}</div>
        <div class="meta">현재 참여 중인 챌린지</div>
      </article>
      <article class="metric-card">
        <div class="label">완료</div>
        <div class="value">${completedCount}</div>
        <div class="meta">완료한 챌린지</div>
      </article>
      <article class="metric-card">
        <div class="label">생성</div>
        <div class="value">${createdCount}</div>
        <div class="meta">내가 만든 챌린지</div>
      </article>
      <article class="metric-card">
        <div class="label">전체</div>
        <div class="value">${challengeCount}</div>
        <div class="meta">등록된 챌린지</div>
      </article>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>챌린지 생성</h5>
              <div class="mini-note">새 챌린지를 등록하고 다른 사용자와 함께 진행해 보세요.</div>
            </div>
          </div>

          <form method="post" action="${challengesUrl}">
            <input type="hidden" name="action" value="create">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold">제목</label>
                <input class="form-control" type="text" name="title" value="${challengeForm.title}">
              </div>
              <div class="col-md-3">
                <label class="form-label fw-semibold">목표 횟수</label>
                <input class="form-control" type="number" name="targetCount" value="${challengeForm.targetCount}">
              </div>
              <div class="col-md-3">
                <label class="form-label fw-semibold">종료일</label>
                <input class="form-control" type="date" name="endDate" value="${challengeForm.endDate}">
              </div>
              <div class="col-md-4">
                <label class="form-label fw-semibold">분류</label>
                <select class="form-select" name="category">
                  <option value="습관" <c:if test="${challengeForm.category eq '습관'}">selected</c:if>>습관</option>
                  <option value="영양 관리" <c:if test="${challengeForm.category eq '영양 관리'}">selected</c:if>>영양 관리</option>
                  <option value="운동" <c:if test="${challengeForm.category eq '운동'}">selected</c:if>>운동</option>
                  <option value="수면 습관" <c:if test="${challengeForm.category eq '수면 습관'}">selected</c:if>>수면 습관</option>
                </select>
              </div>
              <div class="col-md-8">
                <label class="form-label fw-semibold">설명</label>
                <input class="form-control" type="text" name="description" value="${challengeForm.description}">
              </div>
            </div>
            <button class="btn btn-success w-100 mt-4" type="submit">챌린지 생성</button>
          </form>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>챌린지 보드</h5>
              <div class="mini-note">참여 상태와 진행률을 한 번에 확인할 수 있습니다.</div>
            </div>
          </div>

          <c:choose>
            <c:when test="${empty challenges}">
              <div class="empty-state">
                <i class="bi bi-trophy"></i>
                <p class="mb-0">등록된 챌린지가 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <c:forEach var="challenge" items="${challenges}">
                <c:set var="membership" value="${membershipMap[challenge.id]}" />
                <c:set var="participants" value="${participantMap[challenge.id]}" />
                <div class="challenge-card mb-3">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <div class="d-flex align-items-center gap-2 mb-2">
                        <span class="tag">${challenge.category}</span>
                        <c:if test="${ownedChallengeMap[challenge.id]}">
                          <span class="tag">내가 생성</span>
                        </c:if>
                      </div>
                      <div class="fw-semibold fs-5">${challenge.title}</div>
                      <div class="mini-note mt-2">${challenge.description}</div>
                      <div class="mini-note mt-2">
                        기간 ${periodLabelMap[challenge.id]} | 목표 ${challenge.targetCount}회
                      </div>
                    </div>
                    <span class="soft-pill">${statusLabelMap[challenge.id]}</span>
                  </div>

                  <div class="mt-3 mini-note">
                    <c:choose>
                      <c:when test="${empty participants}">
                        참여자가 없습니다.
                      </c:when>
                      <c:otherwise>
                        <c:forEach var="participant" items="${participants}">
                          <div class="d-flex justify-content-between">
                            <span>${participant.nickname}</span>
                            <strong>${participant.progress}</strong>
                          </div>
                        </c:forEach>
                      </c:otherwise>
                    </c:choose>
                  </div>

                  <div class="d-flex flex-wrap gap-2 mt-4">
                    <c:choose>
                      <c:when test="${empty membership}">
                        <form method="post" action="${challengesUrl}">
                          <input type="hidden" name="action" value="join">
                          <input type="hidden" name="challengeId" value="${challenge.id}">
                          <button class="btn btn-outline-success btn-sm" type="submit">참여하기</button>
                        </form>
                      </c:when>
                      <c:otherwise>
                        <form class="d-flex gap-2" method="post" action="${challengesUrl}">
                          <input type="hidden" name="action" value="progress">
                          <input type="hidden" name="challengeId" value="${challenge.id}">
                          <input class="form-control form-control-sm" style="width:100px;" type="number" name="progress" value="${membership.progress}">
                          <button class="btn btn-outline-primary btn-sm" type="submit">진행률 수정</button>
                        </form>
                        <form method="post" action="${challengesUrl}">
                          <input type="hidden" name="action" value="leave">
                          <input type="hidden" name="challengeId" value="${challenge.id}">
                          <button class="btn btn-outline-danger btn-sm" type="submit">나가기</button>
                        </form>
                      </c:otherwise>
                    </c:choose>

                    <c:if test="${ownedChallengeMap[challenge.id]}">
                      <form method="post" action="${challengesUrl}">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="challengeId" value="${challenge.id}">
                        <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                      </form>
                    </c:if>
                  </div>
                </div>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
