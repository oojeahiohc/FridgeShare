### 회원가입과 세션 로그인
- [`UserMapper.java`](../src/main/java/com/fridegeshare/auth/UserMapper.java) 아이디 기반 계정 조회, 저장
- [`AuthPassword.java`](../src/main/java/com/fridegeshare/auth/AuthPassword.java) 비밀번호 정규화와 BCrypt 검증
- [`AuthService.java`](../src/main/java/com/fridegeshare/auth/AuthService.java) 가입 트랜잭션과 회원 조회
- [`AuthApiController.java`](../src/main/java/com/fridegeshare/auth/AuthApiController.java) 가입, 내 정보 JSON API
- [`SignupPageController.java`](../src/main/java/com/fridegeshare/auth/SignupPageController.java) 가입 화면의 행정동 목록 전달
---
- [`login.jsp`](../src/main/webapp/WEB-INF/views/auth/login.jsp) Spring Security 로그인 폼
- [`signup.jsp`](../src/main/webapp/WEB-INF/views/auth/signup.jsp) 회원 가입 폼
- [`signup.js`](../src/main/resources/static/js/signup.js) 단계별 지역 선택과 가입 요청
---
### 회원가입 및 세션 로그인 흐름
```
GET /signup
→ SignupPageController가 행정동 목록을 signup.jsp에 전달
→ signup.js가 상위 지역 선택에 맞춰 하위 목록을 갱신
→ 최종 선택한 행정동 코드를 가입 폼에 담음

POST /api/auth/signup
→ 공통 api.js가 CSRF 토큰을 헤더에 담아 JSON 전송
→ AuthService가 입력값과 행정동 코드를 검증하고 비밀번호를 BCrypt로 해시
→ UserMapper가 계정을 저장
→ 성공하면 201 응답을 받고 로그인 화면으로 이동
→ 아이디가 중복이면 409 응답을 받고 가입 화면에 오류 표시

POST /login
→ Spring Security가 AuthService를 통해 아이디로 계정 조회
→ AuthPassword가 입력한 비밀번호를 저장된 해시와 비교
→ 성공하면 SessionUser를 세션에 보관하고 /posts로 이동
→ 실패하면 /login?error로 이동

GET /api/me
→ 세션의 SessionUser에서 회원 정보를 가져옴
→ 행정동 코드에 해당하는 표시명을 붙여 JSON으로 반환
```