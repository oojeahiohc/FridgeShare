<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>게시글 상세 | 냉장고 구조대</title>
    <script defer src="<c:url value='/js/post-detail.js'/>"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main id="post-detail" data-id="${post.id()}" data-api="<c:url value='/api/posts'/>"
      data-posts="<c:url value='/posts'/>">
    <h1><c:out value="${post.title()}"/></h1>
    <dl>
        <dt>식재료명</dt><dd><c:out value="${post.foodName()}"/></dd>
        <dt>수량</dt><dd><c:out value="${post.quantity()}"/></dd>
        <dt>소비기한</dt><dd><c:out value="${post.expiryDate()}"/></dd>
        <dt>동네</dt><dd><c:out value="${post.neighborhood()}"/></dd>
        <dt>작성자</dt><dd><c:out value="${post.authorNickname()}"/></dd>
        <dt>상태</dt>
        <dd>
            <c:choose>
                <c:when test="${post.status() eq 'AVAILABLE'}">모집 중</c:when>
                <c:when test="${post.status() eq 'RESERVED'}">예약 중</c:when>
                <c:when test="${post.status() eq 'COMPLETED'}">나눔 완료</c:when>
            </c:choose>
        </dd>
        <dt>설명</dt><dd class="multiline"><c:out value="${post.description()}"/></dd>
    </dl>
    <p id="error" role="alert"></p>
    <div class="actions">
        <a href="<c:url value='/posts'/>">목록</a>
        <c:if test="${editable}">
            <a href="<c:url value='/posts/${post.id()}/edit'/>">수정</a>
            <button id="delete" type="button">삭제</button>
        </c:if>
    </div>
</main>
</body>
</html>
