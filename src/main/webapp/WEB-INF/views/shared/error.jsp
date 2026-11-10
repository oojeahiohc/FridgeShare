<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/shared/head.jspf" %>
    <title>요청 오류 | 냉장고 구조대</title>
    
</head>
<body>
<%@ include file="/WEB-INF/views/shared/nav.jspf" %>
<main>
    <h1>요청을 처리할 수 없습니다</h1>
    <p role="alert"><c:out value="${message}"/></p>
    <a href="<c:url value='/posts'/>">목록으로 돌아가기</a>
</main>
</body>
</html>
