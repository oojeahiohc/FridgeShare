<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>마이페이지 | 냉장고 구조대</title>
    <script defer src="<c:url value='/js/mypage.js'/>"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main id="activity" data-me="<c:url value='/api/me'/>" data-api="<c:url value='/api'/>"
      data-posts="<c:url value='/posts'/>" data-rooms="<c:url value='/chat-rooms'/>">
    <h1>마이페이지</h1>
    <p id="profile"></p>
    <p id="error" role="alert"></p>
    <section><h2>내 게시글</h2><ul id="my-posts"></ul></section>
    <section><h2>내 예약</h2><ul id="my-reservations"></ul></section>
    <section><h2>이전 대화</h2><ul id="previous-chats"></ul></section>
</main>
</body>
</html>
