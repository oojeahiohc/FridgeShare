<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!doctype html>
        <html lang="ko">

        <head>
            <%@ include file="/WEB-INF/views/shared/head.jspf" %>
                <title>회원가입 | 냉장고 구조대</title>
                <script defer src="<c:url value='/js/signup.js'/>"></script>
        </head>

        <body>
            <main>
                <h1>회원가입</h1>
                <form id="signup" method="post" data-api="<c:url value='/api/auth/signup'/>"
                    data-next="<c:url value='/login'/>">
                    <label>아이디 <input name="loginId" autocomplete="username" required></label>
                    <label>비밀번호
                        <input name="password" type="password" autocomplete="new-password" required>
                    </label>
                    <p>비밀번호는 8~64자입니다.</p>
                    <label>닉네임 <input name="nickname" autocomplete="nickname" required></label>
                    <fieldset>
                        <legend>활동 동네</legend>
                        <label>시·도 <select id="province" required></select></label>
                        <label>시·군·구 <select id="district" required disabled></select></label>
                        <label>읍·면·동 <select id="town" name="neighborhoodCode" required disabled></select></label>
                    </fieldset>
                    <%-- signup.js가 시·도와 시·군·구별로 필터링할 행정동 원본이다. --%>
                    <template id="region-data">
                        <select>
                            <c:forEach var="region" items="${regions}">
                                <option value="<c:out value='${region.code()}'/>"
                                    data-province="<c:out value='${region.province()}'/>"
                                    data-district="<c:out value='${region.district()}'/>">
                                    <c:out value="${region.town()}" />
                                </option>
                            </c:forEach>
                        </select>
                    </template>
                    <p id="error" role="alert"></p>
                    <button type="submit">회원가입</button>
                    <a href="<c:url value='/login'/>">로그인</a>
                </form>
            </main>
        </body>

        </html>