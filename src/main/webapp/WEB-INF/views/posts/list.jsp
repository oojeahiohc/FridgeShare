<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>게시글 목록 | 냉장고 구조대</title>
    <script defer src="<c:url value='/js/post-list.js'/>"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main id="posts-list" data-api="<c:url value='/api/posts'/>" data-posts="<c:url value='/posts'/>">
    <h1>게시글 목록</h1>
    <p>활동 동네 <c:out value="${neighborhood}"/></p>
    <form id="search" class="actions">
        <label>검색어 <input name="keyword" maxlength="100" placeholder="제목 또는 식재료명"></label>
        <button type="submit">검색</button>
        <a href="<c:url value='/posts/new'/>">글쓰기</a>
    </form>
    <p id="error" role="alert"></p>
    <p id="empty" hidden>게시글이 없습니다.</p>
    <table>
        <thead><tr><th>제목</th><th>식재료명</th><th>소비기한</th><th>상태</th></tr></thead>
        <tbody id="items"></tbody>
    </table>
    <p id="page-status" role="status"></p>
    <nav aria-label="게시글 페이지 이동">
        <button type="button" id="previous" disabled>이전</button>
        <button type="button" id="next" disabled>다음</button>
    </nav>
</main>
</body>
</html>
