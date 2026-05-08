<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map,com.ssafy.yumyum.model.CommunityPost,com.ssafy.yumyum.model.CommunityComment,com.ssafy.yumyum.model.Meal,com.ssafy.yumyum.model.User,com.ssafy.yumyum.util.ViewHelper" %>
<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navbar.jspf" %>
<%@ include file="../common/flash.jspf" %>
<%
List<CommunityPost> posts = (List<CommunityPost>) request.getAttribute("posts");
Map<String, List<CommunityComment>> commentMap = (Map<String, List<CommunityComment>>) request.getAttribute("commentMap");
Map<String, User> authorMap = (Map<String, User>) request.getAttribute("authorMap");
List<Meal> meals = (List<Meal>) request.getAttribute("meals");
CommunityPost editPost = (CommunityPost) request.getAttribute("editPost");
CommunityComment editComment = (CommunityComment) request.getAttribute("editComment");
String selectedCategory = (String) request.getAttribute("selectedCategory");
User communityUser = (User) request.getAttribute("currentUser");
%>
<main class="page-shell">
  <div class="container">
    <section class="page-heading">
      <div>
        <h2>커뮤니티</h2>
        <div class="subtle-text mt-2">게시글/댓글 CRUD를 JSP 폼과 서블릿 컨트롤러로 분리했습니다.</div>
      </div>
    </section>
    <div class="split-grid">
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title"><div><h5><%= editPost != null ? "게시글 수정" : "게시글 작성" %></h5><div class="mini-note">F114 게시글 CRUD, F115 댓글 CRUD</div></div></div>
          <form method="post" action="<%= request.getContextPath() %>/community">
            <input type="hidden" name="action" value="<%= editPost != null ? "updatePost" : "createPost" %>">
            <% if (editPost != null) { %><input type="hidden" name="postId" value="<%= editPost.getId() %>"><% } %>
            <div class="row g-3">
              <div class="col-md-4"><label class="form-label fw-semibold">분류</label><select class="form-select" name="category">
                <option value="review" <%= editPost != null && "review".equals(editPost.getCategory()) ? "selected" : "" %>>식단 리뷰</option>
                <option value="expert" <%= editPost != null && "expert".equals(editPost.getCategory()) ? "selected" : "" %>>전문가 팁</option>
                <option value="free" <%= editPost != null && "free".equals(editPost.getCategory()) ? "selected" : "" %>>자유 게시판</option>
              </select></div>
              <div class="col-md-8"><label class="form-label fw-semibold">연결 식단</label><select class="form-select" name="linkedMealId"><option value="">연결하지 않음</option>
                <% for (Meal meal : meals) { %>
                <option value="<%= meal.getId() %>" <%= editPost != null && meal.getId().equals(editPost.getLinkedMealId()) ? "selected" : "" %>><%= ViewHelper.formatDate(meal.getMealDate()) %> | <%= ViewHelper.mealTypeLabel(meal.getMealType()) %></option>
                <% } %>
              </select></div>
              <div class="col-12"><label class="form-label fw-semibold">제목</label><input class="form-control" type="text" name="title" value="<%= editPost == null ? "" : editPost.getTitle() %>"></div>
              <div class="col-12"><label class="form-label fw-semibold">내용</label><textarea class="form-control" name="content" rows="5"><%= editPost == null ? "" : editPost.getContent() %></textarea></div>
            </div>
            <button class="btn btn-success w-100 mt-4" type="submit"><%= editPost != null ? "게시글 수정" : "게시글 등록" %></button>
          </form>
        </section>
      </div>
      <div class="stack-grid">
        <section class="surface-card">
          <div class="section-title">
            <div><h5>게시판 피드</h5><div class="mini-note">게시글 목록, 댓글 작성/수정/삭제</div></div>
            <form method="get" action="<%= request.getContextPath() %>/community">
              <select class="form-select" name="category" onchange="this.form.submit()">
                <option value="all" <%= "all".equals(selectedCategory) ? "selected" : "" %>>전체</option>
                <option value="review" <%= "review".equals(selectedCategory) ? "selected" : "" %>>식단 리뷰</option>
                <option value="expert" <%= "expert".equals(selectedCategory) ? "selected" : "" %>>전문가 팁</option>
                <option value="free" <%= "free".equals(selectedCategory) ? "selected" : "" %>>자유 게시판</option>
              </select>
            </form>
          </div>
          <% if (posts == null || posts.isEmpty()) { %>
          <div class="empty-state"><i class="bi bi-chat-left-dots"></i><p class="mb-0">게시글이 없습니다.</p></div>
          <% } else { for (CommunityPost post : posts) { User author = authorMap.get(post.getUserId()); %>
          <div class="community-card mb-3">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <div class="d-flex gap-2 mb-2">
                  <span class="tag"><%= ViewHelper.postCategoryLabel(post.getCategory()) %></span>
                  <% if (post.getLinkedMealId() != null) { %><span class="tag">연결 식단</span><% } %>
                </div>
                <div class="fw-semibold fs-5"><%= post.getTitle() %></div>
                <div class="mini-note mt-2"><%= author == null ? "알 수 없음" : author.getNickname() %> | <%= ViewHelper.formatDateTime(post.getCreatedAt()) %></div>
                <div class="mt-3"><%= post.getContent() %></div>
              </div>
              <% if (communityUser.getId().equals(post.getUserId())) { %>
              <div class="d-flex flex-column gap-2">
                <a class="btn btn-outline-primary btn-sm" href="<%= request.getContextPath() %>/community?editPostId=<%= post.getId() %>">수정</a>
                <form method="post" action="<%= request.getContextPath() %>/community">
                  <input type="hidden" name="action" value="deletePost">
                  <input type="hidden" name="postId" value="<%= post.getId() %>">
                  <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                </form>
              </div>
              <% } %>
            </div>
            <div class="mt-4 pt-3 border-top">
              <form method="post" action="<%= request.getContextPath() %>/community" class="mb-3">
                <input type="hidden" name="action" value="createComment">
                <input type="hidden" name="postId" value="<%= post.getId() %>">
                <div class="input-group">
                  <input class="form-control" type="text" name="commentContent" placeholder="댓글을 입력하세요.">
                  <button class="btn btn-success" type="submit">등록</button>
                </div>
              </form>
              <% List<CommunityComment> comments = commentMap.get(post.getId());
                 if (comments == null || comments.isEmpty()) { %>
              <div class="mini-note">댓글이 없습니다.</div>
              <% } else { for (CommunityComment comment : comments) { User commentAuthor = authorMap.get(comment.getUserId()); %>
              <div class="comment-row mb-2">
                <div class="d-flex justify-content-between align-items-start">
                  <div style="width:100%;">
                    <div class="fw-semibold"><%= commentAuthor == null ? "알 수 없음" : commentAuthor.getNickname() %></div>
                    <div class="mini-note mt-1"><%= ViewHelper.formatDateTime(comment.getCreatedAt()) %></div>
                    <% if (editComment != null && comment.getId().equals(editComment.getId())) { %>
                    <form method="post" action="<%= request.getContextPath() %>/community" class="mt-2">
                      <input type="hidden" name="action" value="updateComment">
                      <input type="hidden" name="commentId" value="<%= comment.getId() %>">
                      <div class="input-group">
                        <input class="form-control" type="text" name="commentContent" value="<%= comment.getContent() %>">
                        <button class="btn btn-outline-primary" type="submit">저장</button>
                      </div>
                    </form>
                    <% } else { %>
                    <div class="mt-2"><%= comment.getContent() %></div>
                    <% } %>
                  </div>
                  <% if (communityUser.getId().equals(comment.getUserId())) { %>
                  <div class="d-flex gap-2">
                    <a class="btn btn-outline-primary btn-sm" href="<%= request.getContextPath() %>/community?editCommentId=<%= comment.getId() %>">수정</a>
                    <form method="post" action="<%= request.getContextPath() %>/community">
                      <input type="hidden" name="action" value="deleteComment">
                      <input type="hidden" name="commentId" value="<%= comment.getId() %>">
                      <button class="btn btn-outline-danger btn-sm" type="submit">삭제</button>
                    </form>
                  </div>
                  <% } %>
                </div>
              </div>
              <% }} %>
            </div>
          </div>
          <% }} %>
        </section>
      </div>
    </div>
  </div>
</main>
<%@ include file="../common/footer.jspf" %>
