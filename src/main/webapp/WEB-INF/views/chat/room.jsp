<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>채팅방 | 냉장고 구조대</title>
    <style>
        .messages li {
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }
    </style>
    <script defer src="<c:url value='/js/chat.js'/>"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main id="chat-room" data-my-id="${myId}" data-api="<c:url value='/api/chat-rooms/${room.roomId()}/messages'/>">
    <h1><c:out value="${room.foodName()}"/> 채팅</h1>
    <p>상대방 <c:out value="${room.counterpartNickname()}"/></p>
    <ol id="messages" class="messages"></ol>
    <p id="chat-status" role="status">대화를 불러오는 중입니다.</p>
    <p id="error" role="alert"></p>
    <form id="chat-form">
        <label>메시지 <textarea name="content" rows="3" maxlength="1000" required disabled></textarea></label>
        <button type="submit" disabled>전송</button>
    </form>
    <div class="actions">
        <a href="<c:url value='/mypage'/>">마이페이지</a>
        <c:if test="${not room.postDeleted()}">
            <a href="<c:url value='/posts/${room.postId()}'/>">게시글로 이동</a>
        </c:if>
    </div>
</main>
</body>
</html>
