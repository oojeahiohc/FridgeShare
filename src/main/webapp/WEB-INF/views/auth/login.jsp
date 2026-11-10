<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>로그인 | 냉장고 구조대</title>
</head>
<body>
<main>
    <h1>로그인</h1>
    <c:if test="${param.error ne null}"><p role="alert">아이디와 비밀번호를 확인하세요.</p></c:if>
    <%-- Spring Security가 loginId 필드와 CSRF 토큰을 받아 폼 로그인을 처리한다. --%>
    <form action="<c:url value='/login'/>" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>아이디 <input name="loginId" autocomplete="username" required></label>
        <label>비밀번호 <input name="password" type="password" autocomplete="current-password" required></label>
        <div class="actions">
            <button type="submit">로그인</button>
            <a href="<c:url value='/signup'/>">회원가입</a>
        </div>
    </form>
</main>
</body>
</html>