### 선착순 예약과 취소, 완료
- [`ReservationMapper.java`](../src/main/java/com/fridegeshare/reservation/ReservationMapper.java) 예약 선점, 행 잠금, 상태 전이와 내 예약 이력 SQL
- [`ReservationService.java`](../src/main/java/com/fridegeshare/reservation/ReservationService.java) 지역, 권한, 소비기한 검증과 예약 트랜잭션
- [`ReservationApiController.java`](../src/main/java/com/fridegeshare/reservation/ReservationApiController.java) 예약 신청, 취소, 완료 및 내 예약 조회 JSON API
---
### 예약 흐름
```
POST /api/posts/{postId}/reservations
→ ReservationApiController가 로그인 회원과 게시글 ID를 ReservationService.claim()에 전달
→ ReservationService가 RegionDirectory에서 회원과 같은 시·도 및 시·군·구의 행정동 코드 목록을 가져옴
→ ReservationMapper가 지역, 작성자, 삭제 여부, 모집 상태, 서울 기준 소비기한을 확인하는 조건부 UPDATE로 게시글을 선점
→ 선점에 성공하면 ReservationMapper가 같은 트랜잭션에서 예약 기록을 저장
→ ReservationApiController가 예약 ID와 상태를 201 응답으로 반환

PATCH /api/reservations/{reservationId}/cancel
→ ReservationApiController가 ReservationService.cancel()을 호출
→ ReservationService가 ReservationMapper를 통해 게시글, 예약 순으로 잠그고 요청자가 작성자 또는 신청자인지 확인
→ ReservationMapper가 예약을 취소하고 게시글을 모집 중으로 복구
→ ReservationApiController가 변경된 예약 상태를 JSON으로 반환

PATCH /api/reservations/{reservationId}/complete
→ ReservationApiController가 ReservationService.complete()를 호출
→ ReservationService가 ReservationMapper를 통해 게시글, 예약 순으로 잠그고 요청자가 게시글 작성자인지 확인
→ ReservationMapper가 예약과 게시글을 완료 상태로 변경
→ ReservationApiController가 변경된 예약 상태를 JSON으로 반환

GET /api/me/reservations
→ ReservationApiController가 로그인 회원을 ReservationService.mine()에 전달
→ ReservationMapper가 회원 ID로 예약 이력을 조회하고 게시글의 논리 삭제 여부도 가져옴
→ ReservationService가 조회 결과를 응답 형식으로 변환하고 ReservationApiController가 JSON으로 반환
```
