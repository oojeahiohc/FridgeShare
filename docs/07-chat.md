### 예약별 채팅과 읽기 전용 이력
- [`ChatMapper.java`](../src/main/java/com/fridegeshare/chat/ChatMapper.java) 예약 행 잠금, 채팅방 생성과 조회, 메시지 저장과 새 메시지 조회, 종료된 대화 이력 SQL
- [`ChatService.java`](../src/main/java/com/fridegeshare/chat/ChatService.java) 작성자와 예약자 권한 확인, 중복 방 생성 방지, 예약 종료 후 전송 제한
- [`ChatApiController.java`](../src/main/java/com/fridegeshare/chat/ChatApiController.java) 채팅방 시작, 메시지 조회와 전송 및 이전 대화 조회 JSON API
---
### 채팅 흐름
```
POST /api/reservations/{reservationId}/chat-room
→ ChatApiController가 로그인 회원과 예약 ID를 ChatService.open()에 전달
→ ChatMapper가 예약 행을 잠그고 게시글 작성자와 기존 방을 조회
→ ChatService가 작성자이며 예약이 진행 중인지 확인
→ ChatMapper가 방을 생성하면 ChatApiController가 201을 반환
→ 이미 방이 있으면 같은 방 ID를 200 응답으로 반환

GET /api/chat-rooms/{roomId}/messages
→ ChatApiController가 방 ID와 afterId를 ChatService.messages()에 전달
→ ChatService가 ChatMapper의 조회 결과로 게시글 작성자 또는 예약자인지 확인
→ ChatMapper가 afterId보다 큰 메시지를 ID 순으로 조회
→ ChatApiController가 메시지 목록과 canSend를 반환
→ 최초 요청은 afterId가 0이어서 전체 메시지를 받고, 다음부터 마지막 메시지 ID 이후만 조회
→ 새 메시지가 없어도 취소, 완료된 예약은 canSend가 false

POST /api/chat-rooms/{roomId}/messages
→ ChatApiController가 메시지 내용을 ChatService.send()에 전달
→ ChatService가 참여자 권한을 확인하고 ChatMapper로 예약 행을 잠금
→ 예약이 진행 중이면 메시지를 검증하고 ChatMapper가 저장
→ 이미 취소, 완료된 예약이면 ChatService가 409 오류를 반환

GET /api/me/chat-rooms
→ ChatApiController가 로그인 회원을 ChatService.history()에 전달
→ ChatMapper가 작성자 또는 예약자로 참여한 취소, 완료 대화를 조회
→ ChatApiController가 예약별 방 정보와 게시글 논리 삭제 여부를 JSON으로 반환
→ 같은 글이 재예약되어도 방은 예약별로 구분되고 이전 대화는 이력에 남음
```
