<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>게시글 작성 | 냉장고 구조대</title>
    <script defer src="<c:url value='/js/post-form.js'/>"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main>
    <h1>${postId == null ? '게시글 등록' : '게시글 수정'}</h1>
    <p>활동 동네 <c:out value="${neighborhood}"/></p>
    <form id="post-form" data-id="${postId}" data-api="<c:url value='/api/posts'/>"
          data-posts="<c:url value='/posts'/>">
        <label>제목 <input name="title" maxlength="100" required></label>
        <label>식재료명 <input name="foodName" maxlength="100" required></label>
        <label>수량 <input name="quantity" maxlength="50" required></label>
        <label>소비기한 <input name="expiryDate" type="date" min="${today}" required></label>
        <label>설명 <textarea name="description" maxlength="2000" rows="6" required></textarea></label>
        <p id="error" role="alert"></p>
        <button type="submit" disabled>저장</button>
        <c:choose>
            <c:when test="${postId != null}"><a href="<c:url value='/posts/${postId}'/>">취소</a></c:when>
            <c:otherwise><a href="<c:url value='/posts'/>">취소</a></c:otherwise>
        </c:choose>
    </form>
</main>
</body>
</html>
