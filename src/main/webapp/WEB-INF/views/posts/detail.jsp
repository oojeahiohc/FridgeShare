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
<main id="post-detail" data-posts="<c:url value='/posts'/>" data-rooms="<c:url value='/chat-rooms'/>">
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
    <c:choose>
        <c:when test="${expired}"><p role="status">소비기한이 지났습니다.</p></c:when>
        <c:when test="${post.status() eq 'RESERVED' and not activeReservation}">
            <p role="status">예약이 마감되었습니다.</p>
        </c:when>
    </c:choose>
    <p id="error" role="alert"></p>
    <div class="actions">
        <a href="<c:url value='/posts'/>">목록</a>
        <c:if test="${editable}">
            <a href="<c:url value='/posts/${post.id()}/edit'/>">수정</a>
            <button type="button" data-action="delete" data-method="DELETE"
                    data-url="<c:url value='/api/posts/${post.id()}'/>"
                    data-confirm="이 게시글을 삭제할까요?">삭제</button>
        </c:if>
        <c:if test="${claimable}">
            <button type="button" data-action="claim" data-method="POST"
                    data-url="<c:url value='/api/posts/${post.id()}/reservations'/>">나눔 신청</button>
        </c:if>
        <c:if test="${activeReservation}">
            <button type="button" data-action="cancel" data-method="PATCH"
                    data-url="<c:url value='/api/reservations/${reservationId}/cancel'/>">예약 취소</button>
            <c:if test="${owner}">
                <button type="button" data-action="complete" data-method="PATCH"
                        data-url="<c:url value='/api/reservations/${reservationId}/complete'/>"
                        data-confirm="나눔을 완료할까요?">나눔 완료</button>
                <c:if test="${empty chatRoomId}">
                    <button type="button" data-action="start" data-method="POST"
                            data-url="<c:url value='/api/reservations/${reservationId}/chat-room'/>">채팅 시작</button>
                </c:if>
            </c:if>
        </c:if>
        <c:if test="${not empty chatRoomId}">
            <a href="<c:url value='/chat-rooms/${chatRoomId}'/>">채팅방 이동</a>
        </c:if>
    </div>
</main>
</body>
</html>
