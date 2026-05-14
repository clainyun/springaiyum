<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<c:url var="communityUrl" value="/community" />
<c:url var="communityListUrl" value="/community">
  <c:if test="${selectedCategory ne 'all'}">
    <c:param name="category" value="${selectedCategory}" />
  </c:if>
</c:url>
<c:url var="postCreateUrl" value="/community/posts" />
<c:choose>
  <c:when test="${not empty editPost}">
    <c:url var="postFormUrl" value="/community/posts/${editPost.id}" />
  </c:when>
  <c:otherwise>
    <c:set var="postFormUrl" value="${postCreateUrl}" />
  </c:otherwise>
</c:choose>

<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>커뮤니티</h2>
        <div class="subtle-text mt-2">게시글과 댓글 CRUD를 Spring MVC와 RESTful 라우팅으로 정리했습니다.</div>
      </div>
    </section>

    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>
                <c:choose>
                  <c:when test="${not empty editPost}">게시글 수정</c:when>
                  <c:otherwise>게시글 작성</c:otherwise>
                </c:choose>
              </h5>
              <div class="mini-note">F114 게시글 CRUD, F115 댓글 CRUD</div>
            </div>
          </div>

          <form method="post" action="${postFormUrl}">
            <c:if test="${not empty editPost}">
              <input type="hidden" name="_method" value="patch">
            </c:if>

            <div class="row g-3">
              <div class="col-md-4">
                <label class="form-label fw-semibold">분류</label>
                <select class="form-select" name="category">
                  <option value="review" ${(not empty editPost and editPost.category eq 'review') or (empty editPost and selectedCategory eq 'review') ? 'selected="selected"' : ''}>${categoryLabelMap.review}</option>
                  <option value="expert" ${(not empty editPost and editPost.category eq 'expert') or (empty editPost and selectedCategory eq 'expert') ? 'selected="selected"' : ''}>${categoryLabelMap.expert}</option>
                  <option value="free" ${(not empty editPost and editPost.category eq 'free') or (empty editPost and selectedCategory eq 'free') ? 'selected="selected"' : ''}>${categoryLabelMap.free}</option>
                </select>
              </div>

              <div class="col-md-8">
                <label class="form-label fw-semibold">연결 식단</label>
                <select class="form-select" name="linkedMealId">
                  <option value="">연결하지 않음</option>
                  <c:forEach var="meal" items="${meals}">
                    <option value="${meal.id}" ${not empty editPost and meal.id eq editPost.linkedMealId ? 'selected="selected"' : ''}>${mealLabelMap[meal.id]}</option>
                  </c:forEach>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label fw-semibold">제목</label>
                <input class="form-control" type="text" name="title" value="${empty editPost ? '' : fn:escapeXml(editPost.title)}">
              </div>

              <div class="col-12">
                <label class="form-label fw-semibold">내용</label>
                <textarea class="form-control" name="content" rows="5">${empty editPost ? '' : fn:escapeXml(editPost.content)}</textarea>
              </div>
            </div>

            <button class="btn btn-success w-100 mt-4" type="submit">
              <c:choose>
                <c:when test="${not empty editPost}">게시글 수정</c:when>
                <c:otherwise>게시글 등록</c:otherwise>
              </c:choose>
            </button>
            <c:if test="${not empty editPost}">
              <a class="btn btn-outline-secondary w-100 mt-2" href="${communityListUrl}">취소</a>
            </c:if>
          </form>
        </section>
      </div>

      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div>
              <h5>게시판 보드</h5>
              <div class="mini-note">게시글 목록과 댓글 작성, 수정, 삭제를 한 화면에서 처리합니다.</div>
            </div>

            <form method="get" action="${communityUrl}">
              <select class="form-select" name="category" onchange="this.form.submit()">
                <option value="all" ${selectedCategory eq 'all' ? 'selected="selected"' : ''}>전체</option>
                <option value="review" ${selectedCategory eq 'review' ? 'selected="selected"' : ''}>${categoryLabelMap.review}</option>
                <option value="expert" ${selectedCategory eq 'expert' ? 'selected="selected"' : ''}>${categoryLabelMap.expert}</option>
                <option value="free" ${selectedCategory eq 'free' ? 'selected="selected"' : ''}>${categoryLabelMap.free}</option>
              </select>
            </form>
          </div>

          <c:choose>
            <c:when test="${empty posts}">
              <div class="empty-state">
                <i class="bi bi-chat-left-dots"></i>
                <p class="mb-0">게시글이 없습니다.</p>
              </div>
            </c:when>
            <c:otherwise>
              <c:forEach var="post" items="${posts}">
                <c:set var="comments" value="${commentMap[post.id]}" />
                <c:url var="editPostUrl" value="/community/posts/${post.id}/edit">
                  <c:param name="category" value="${selectedCategory}" />
                </c:url>
                <c:url var="deletePostUrl" value="/community/posts/${post.id}" />
                <c:url var="createCommentUrl" value="/community/posts/${post.id}/comments" />

                <div class="community-card mb-3">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <div class="d-flex gap-2 mb-2">
                        <span class="tag">${categoryLabelMap[post.category]}</span>
                        <c:if test="${not empty post.linkedMealId}">
                          <span class="tag">연결 식단</span>
                        </c:if>
                      </div>

                      <div class="fw-semibold fs-5">
                        <c:out value="${post.title}" />
                      </div>

                      <div class="mini-note mt-2">${authorNameMap[post.userId]} | ${postCreatedAtMap[post.id]}</div>

                      <div class="mt-3">
                        <c:out value="${post.content}" />
                      </div>
                    </div>

                    <c:if test="${currentUser.id eq post.userId}">
                      <div class="d-flex flex-column gap-2">
                        <a class="btn btn-outline-primary btn-sm" href="${editPostUrl}">수정</a>
                        <form method="post" action="${deletePostUrl}">
                          <input type="hidden" name="_method" value="delete">
                          <input type="hidden" name="redirectCategory" value="${selectedCategory}">
                          <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                        </form>
                      </div>
                    </c:if>
                  </div>

                  <div class="mt-4 pt-3 border-top">
                    <form method="post" action="${createCommentUrl}" class="mb-3">
                      <input type="hidden" name="redirectCategory" value="${selectedCategory}">
                      <div class="input-group">
                        <input class="form-control" type="text" name="commentContent" placeholder="댓글을 입력하세요">
                        <button class="btn btn-success" type="submit">등록</button>
                      </div>
                    </form>

                    <c:choose>
                      <c:when test="${empty comments}">
                        <div class="mini-note">댓글이 없습니다.</div>
                      </c:when>
                      <c:otherwise>
                        <c:forEach var="comment" items="${comments}">
                          <c:url var="editCommentUrl" value="/community/comments/${comment.id}/edit">
                            <c:param name="category" value="${selectedCategory}" />
                          </c:url>
                          <c:url var="updateCommentUrl" value="/community/comments/${comment.id}" />
                          <c:url var="deleteCommentUrl" value="/community/comments/${comment.id}" />

                          <div class="comment-row mb-2">
                            <div class="d-flex justify-content-between align-items-start">
                              <div style="width:100%;">
                                <div class="fw-semibold">${authorNameMap[comment.userId]}</div>
                                <div class="mini-note mt-1">${commentCreatedAtMap[comment.id]}</div>

                                <c:choose>
                                  <c:when test="${not empty editComment and comment.id eq editComment.id}">
                                    <form method="post" action="${updateCommentUrl}" class="mt-2">
                                      <input type="hidden" name="_method" value="patch">
                                      <input type="hidden" name="redirectCategory" value="${selectedCategory}">
                                      <div class="input-group">
                                        <input class="form-control" type="text" name="commentContent" value="${fn:escapeXml(comment.content)}">
                                        <button class="btn btn-outline-primary" type="submit">수정</button>
                                      </div>
                                    </form>
                                    <a class="btn btn-outline-secondary btn-sm mt-2" href="${communityListUrl}">취소</a>
                                  </c:when>
                                  <c:otherwise>
                                    <div class="mt-2">
                                      <c:out value="${comment.content}" />
                                    </div>
                                  </c:otherwise>
                                </c:choose>
                              </div>

                              <c:if test="${currentUser.id eq comment.userId}">
                                <div class="d-flex gap-2">
                                  <a class="btn btn-outline-primary btn-sm" href="${editCommentUrl}">수정</a>
                                  <form method="post" action="${deleteCommentUrl}">
                                    <input type="hidden" name="_method" value="delete">
                                    <input type="hidden" name="redirectCategory" value="${selectedCategory}">
                                    <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                                  </form>
                                </div>
                              </c:if>
                            </div>
                          </div>
                        </c:forEach>
                      </c:otherwise>
                    </c:choose>
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
