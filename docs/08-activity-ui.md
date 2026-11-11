### 예약 버튼, 채팅방, 마이페이지 연동
- [`PostPageController.java`](../src/main/java/com/fridegeshare/post/PostPageController.java) 게시글 상태, 조회자 역할, 소비기한에 따라 상세 화면에 필요한 값 전달
- [`ActivityPageController.java`](../src/main/java/com/fridegeshare/chat/ActivityPageController.java) 마이페이지 표시와 참여자 권한을 확인한 채팅방 화면 연결
---
- [`detail.jsp`](../src/main/webapp/WEB-INF/views/posts/detail.jsp) 게시글 상세 내용과 신청, 취소, 완료, 채팅 버튼 표시
- [`mypage.jsp`](../src/main/webapp/WEB-INF/views/mypage.jsp) 프로필, 내 게시글, 내 예약, 이전 대화 화면
- [`room.jsp`](../src/main/webapp/WEB-INF/views/chat/room.jsp) 대화 목록, 메시지 입력, 게시글과 마이페이지 이동 링크
- [`post-detail.js`](../src/main/resources/static/js/post-detail.js) 상세 버튼을 예약, 채팅, 게시글 API에 연결
- [`mypage.js`](../src/main/resources/static/js/mypage.js) 활동 목록 표시, 예약 취소, 삭제된 글의 상세 링크 숨김
- [`chat.js`](../src/main/resources/static/js/chat.js) 새 메시지 조회와 전송, 예약 종료 후 입력 제한
---
### 예약, 채팅 화면 흐름
```
GET /posts/{postId}
→ PostPageController가 PostService.detail()을 호출해 같은 시·도와 시·군·구인지 확인
→ detail.jsp가 게시글 내용과 조회자에게 가능한 동작을 표시
→ 작성자가 아닌 회원에게는 모집 중이고 소비기한이 지나지 않은 글의 나눔 신청 버튼을 표시
→ 예약 중인 글은 작성자와 예약자에게 취소, 작성자에게 완료와 방이 없을 때 채팅 시작을 표시
→ post-detail.js가 api.js를 통해 예약 API를 호출하고 성공하면 상세 화면을 새로고침
→ 채팅 시작 시 ChatApiController에서 받은 방 ID로 채팅 화면에 이동

GET /mypage
→ ActivityPageController가 mypage.jsp를 표시
→ mypage.js가 /api/me, /api/me/posts, /api/me/reservations, /api/me/chat-rooms를 요청
→ 각 API의 결과로 내 프로필, 내 게시글, 내 예약, 이전 대화 목록을 표시
→ 취소 버튼은 ReservationApiController의 취소 API를 호출하고 성공하면 목록을 다시 조회
→ 삭제된 게시글의 상세 링크를 숨기고 이전 대화는 채팅방 링크로 제공

GET /chat-rooms/{roomId}
→ ActivityPageController가 ChatService.room()으로 참여자 권한을 확인한 뒤 room.jsp를 표시
→ chat.js가 ChatApiController의 메시지 API를 호출해 전체 대화를 받은 뒤 마지막 ID 이후의 메시지만 조회
→ 응답이 끝나고 약 3초 뒤에 다시 조회해 조회 요청이 겹치지 않게 함
→ 전송 시 같은 API에 POST 요청을 보내고, 사용자 메시지는 텍스트로 표시
→ 예약 취소, 완료로 canSend가 false이거나 전송이 CHAT_NOT_AVAILABLE 오류로 거절되면 입력창을 비활성화
```
