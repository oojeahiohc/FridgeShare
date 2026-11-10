package com.fridegeshare.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
interface ReservationMapper {
    record PostLock(Long id, Long authorId, String neighborhood, LocalDate expiryDate,
                    String status, LocalDateTime deletedAt) {}
    record ReservationRow(Long id, Long postId, Long applicantId, String status) {}
    record HistoryRow(Long reservationId, Long postId, Boolean postDeleted, String foodName,
                      String postStatus, String reservationStatus, Long chatRoomId, LocalDateTime createdAt) {}

    // 모집 상태와 신청 조건을 한 UPDATE에서 확인해 동시에 한 명만 선점한다.
    @Update("""
        <script>
        UPDATE food_posts SET status = 'RESERVED'
        WHERE id = #{postId} AND status = 'AVAILABLE' AND deleted_at IS NULL
          AND author_id &lt;&gt; #{applicantId} AND neighborhood IN
          <foreach collection="neighborhoods" item="code" open="(" separator="," close=")">
              #{code}
          </foreach>
          AND expiry_date >= #{today}
        </script>
        """)
    int claim(@Param("postId") long postId, @Param("applicantId") long applicantId,
              @Param("neighborhoods") List<String> neighborhoods, @Param("today") LocalDate today);

    @Select("""
        SELECT id, author_id, neighborhood, expiry_date, status, deleted_at
        FROM food_posts WHERE id = #{postId} FOR UPDATE
        """)
    PostLock lockPost(long postId);

    @Insert("""
        INSERT INTO reservations (post_id, applicant_id) VALUES (#{postId}, #{applicantId})
        """)
    int insert(@Param("postId") long postId, @Param("applicantId") long applicantId);

    @Select("SELECT LAST_INSERT_ID()")
    long insertedId();

    @Select("SELECT post_id FROM reservations WHERE id = #{id}")
    Long postId(long id);

    @Select("SELECT id, post_id, applicant_id, status FROM reservations WHERE id = #{id} FOR UPDATE")
    ReservationRow lockReservation(long id);

    @Update("UPDATE reservations SET status = #{status} WHERE id = #{id} AND status = 'RESERVED'")
    int changeReservation(@Param("id") long id, @Param("status") String status);

    @Update("UPDATE food_posts SET status = #{status} WHERE id = #{id} AND status = 'RESERVED' AND deleted_at IS NULL")
    int changePost(@Param("id") long id, @Param("status") String status);

    // 논리 삭제된 글도 예약 이력에 남겨 삭제 여부를 표시할 수 있게 한다.
    @Select("""
        SELECT r.id AS reservation_id, p.id AS post_id, p.deleted_at IS NOT NULL AS post_deleted,
            p.food_name, p.status AS post_status, r.status AS reservation_status, c.id AS chat_room_id, r.created_at
        FROM reservations r JOIN food_posts p ON p.id = r.post_id
        LEFT JOIN chat_rooms c ON c.reservation_id = r.id
        WHERE r.applicant_id = #{applicantId}
        ORDER BY r.created_at DESC, r.id DESC
        """)
    List<HistoryRow> mine(long applicantId);
}
