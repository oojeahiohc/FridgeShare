### 게시판 CRUD와 검색
- [`PostMapper.java`](../src/main/java/com/fridegeshare/post/PostMapper.java) 동네별 목록과 검색, 게시글 조회, 저장, 수정, 논리 삭제 SQL
- [`PostService.java`](../src/main/java/com/fridegeshare/post/PostService.java) 게시글 입력 검증, 작성자와 모집 상태 확인, 예약, 채팅 정보 공개 범위 결정
- [`PostApiController.java`](../src/main/java/com/fridegeshare/post/PostApiController.java) 게시글 CRUD와 내 글 목록 JSON API
- [`PostPageController.java`](../src/main/java/com/fridegeshare/post/PostPageController.java) 게시판 목록, 상세, 등록, 수정 화면 연결
---
- [`error.jsp`](../src/main/webapp/WEB-INF/views/shared/error.jsp) 게시판 오류 화면
- [`list.jsp`](../src/main/webapp/WEB-INF/views/posts/list.jsp) 동네 게시글 목록과 검색 화면
- [`form.jsp`](../src/main/webapp/WEB-INF/views/posts/form.jsp) 등록, 수정 공용 폼
- [`detail.jsp`](../src/main/webapp/WEB-INF/views/posts/detail.jsp) 상세 데이터와 수정, 삭제 버튼을 서버에서 렌더링
- [`post-list.js`](../src/main/resources/static/js/post-list.js) 검색 요청과 목록 갱신
- [`post-form.js`](../src/main/resources/static/js/post-form.js) 등록, 수정 요청과 폼 처리
- [`post-detail.js`](../src/main/resources/static/js/post-detail.js) 삭제 요청과 결과 처리
---
### 게시판 흐름
```
목록, 검색 → 세션의 동네 코드로 조회 범위를 제한하고 제목 또는 식재료명으로 검색
상세 조회 → 미삭제 글을 조회하고 작성자와 신청자에게만 예약, 채팅 ID 제공
등록 → 소비기한과 필수값을 검증하고 로그인 회원과 동네 코드로 저장
수정, 삭제 → 작성자이며 모집 중인 글인지 확인하고 DB 변경 시에도 같은 조건을 적용
삭제 → deleted_at을 기록하고 이후 목록과 상세 조회에서 제외
```
---
- [`PostBranchTest.java`](../src/test/java/com/fridegeshare/post/PostBranchTest.java) 같은 동네 검색, 다른 동네 상세 조회, 수정 권한 및 논리 삭제 검증
