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
- [`post-list.js`](../src/main/resources/static/js/post-list.js) 목록, 검색 요청과 페이지 이동
- [`post-form.js`](../src/main/resources/static/js/post-form.js) 등록, 수정 요청과 폼 처리
- [`post-detail.js`](../src/main/resources/static/js/post-detail.js) 삭제 요청과 결과 처리
---
### 게시판 흐름
```
GET /posts → GET /api/posts
→ PostPageController가 list.jsp를 표시하고 post-list.js가 목록, 검색을 요청
→ PostApiController가 로그인 회원, 검색어, 페이지 번호(기본값 1)를 PostService.list()에 전달
→ PostService가 RegionDirectory에서 같은 시·도 및 시·군·구의 행정동 코드 목록을 가져옴
→ PostMapper가 해당 지역의 미삭제 글을 제목 또는 식재료명으로 검색하고 전체 건수와 페이지당 10건을 조회
→ post-list.js가 JSON 응답의 data.posts와 페이지 정보로 목록과 이전·다음 버튼을 갱신

GET /posts/{postId}
→ PostPageController가 PostService.detail()을 호출하고 PostMapper가 미삭제 글을 조회
→ PostService가 RegionDirectory로 지역을 비교하고 범위 밖의 글에는 404를 반환
→ detail.jsp가 상세 내용과 작성자의 모집 중인 글에 대한 수정, 삭제 버튼을 렌더링

GET /posts/new → POST /api/posts
→ PostPageController가 form.jsp를 표시하고 post-form.js가 입력을 JSON으로 전송
→ PostApiController가 PostService.create()를 호출해 필수값과 소비기한을 검증
→ PostMapper가 로그인 회원 ID와 행정동 코드로 글을 저장
→ post-form.js가 응답의 게시글 ID로 상세 화면에 이동

GET /posts/{postId}/edit → GET /api/posts/{postId} → PUT /api/posts/{postId}
→ PostPageController가 PostService.requireEditable()로 작성자와 모집 상태를 확인한 뒤 form.jsp를 표시
→ post-form.js가 PostApiController의 상세 API에서 기존 값을 가져와 폼을 채우고 수정값을 전송
→ PostService.update()가 권한과 입력값을 확인하고 PostMapper가 수정
→ post-form.js가 상세 화면에 이동

DELETE /api/posts/{postId}
→ detail.jsp의 삭제 버튼을 누르면 post-detail.js가 삭제 요청을 전송
→ PostApiController가 PostService.delete()를 호출해 작성자와 모집 상태를 확인
→ PostMapper가 deleted_at을 기록해 이후 목록과 상세 조회에서 제외
→ post-detail.js가 게시글 목록으로 이동

GET /api/me/posts
→ PostApiController가 로그인 회원을 PostService.mine()에 전달
→ PostMapper가 회원 ID로 미삭제 글을 조회하고 PostApiController가 JSON으로 반환
```
---
- [`PostBranchTest.java`](../src/test/java/com/fridegeshare/post/PostBranchTest.java) 검색, 페이지네이션, 시·군·구별 상세 접근, 수정 권한 및 논리 삭제 검증
