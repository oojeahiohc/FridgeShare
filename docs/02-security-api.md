### 공통 보안 설정과 API 요청 처리
- [`SessionUser.java`](../src/main/java/com/fridegeshare/common/SessionUser.java)
    - 로그인한 회원의 정보를 담는 클래스
    - 로그인 처리 코드가 DB에서 회원을 조회하면, 그 결과로 SessionUser를 만들어 Spring Security에 전달
- [`SecurityConfig.java`](../src/main/java/com/fridegeshare/common/SecurityConfig.java)
    - Spring Security 설정 클래스
- [`ApiException.java`](../src/main/java/com/fridegeshare/common/ApiException.java)
    - 어떤 오류가 발생했는지 HTTP 상태, 오류 코드, 메시지를 담는 클래스
- [`ApiErrors.java`](../src/main/java/com/fridegeshare/common/ApiErrors.java)
    - API 처리 중 던져진 예외를 받아 HTTP 상태를 설정하고, 오류 코드와 메시지를 JSON 응답으로 보내는 클래스
- [`Inputs.java`](../src/main/java/com/fridegeshare/common/Inputs.java)
    - 입력값을 검사하고 날짜, 시간을 서울 기준으로 처리하는 클래스
- [`LoginPageController.java`](../src/main/java/com/fridegeshare/common/LoginPageController.java) 로그인 화면을 보여주는 컨트롤러
---
- [`login.jsp`](../src/main/webapp/WEB-INF/views/auth/login.jsp) 임시 로그인 화면
- [`head.jspf`](../src/main/webapp/WEB-INF/views/shared/head.jspf) 공통 모바일 뷰포트, 테스트용 최대 너비와 CSRF 메타 태그
- [`nav.jspf`](../src/main/webapp/WEB-INF/views/shared/nav.jspf) 공통 메뉴와 로그아웃 폼
- [`api.js`](../src/main/resources/static/js/api.js) JSP 화면에서 서버에 JSON 요청을 보내고, CSRF 토큰과 오류 응답을 공통으로 처리하는 JavaScript 파일
---
### 로그인 및 API 요청 흐름
```
GET /login 요청 → SecurityConfig에서 접근 허용, LoginPageController에서 login.jsp 표시

실제 로그인 기능 연결 시 Spring Security가 로그인 처리 후 인증된 회원의 SessionUser를 보관한다. 이후 게시판 등에서 이 객체의 회원 ID와 동네 코드를 사용

head.jspf는 CSRF 토큰과 api.js를 준비, nav.jspf는 메뉴와 로그아웃 폼을 제공, JavaScript로 데이터를 변경할 때 api.js는 CSRF 토큰을 헤더에 넣어 전송한다.

API 처리 중 Inputs가 입력값을 검증한다. 값이 잘못되면 ApiException을 던지고 ApiErrors가 HTTP 상태와 JSON 오류 응답으로 변환한다.

로그인이나 권한 검사에서 먼저 막힌 요청은 SecurityConfig의 오류 처리로 응답한다.
```
---
- `./gradlew bootRun`
    - 서버 시작 로그 확인 후 http://localhost:8080/login 을 열어 화면 표시 확인
- 빌드 필요 시
    - `./gradlew bootWar && java -jar build/libs/FridegeShare.war`
