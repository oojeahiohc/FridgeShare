package com.fridegeshare.chat;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
interface ChatMapper {
    record ContextRow(Long roomId, Long reservationId, Long postId, Long authorId, Long applicantId,
                      String status, Boolean postDeleted, String foodName, String ownerNickname, String applicantNickname) {}
    record MessageRow(Long id, Long chatRoomId, Long senderId, String content, LocalDateTime createdAt) {}
    record HistoryRow(Long roomId, Long reservationId, String reservationStatus, Long postId,
                      Boolean postDeleted, String foodName, String counterpartNickname) {}

    String ROOM_CONTEXT = """
        SELECT c.id AS room_id, r.id AS reservation_id, p.id AS post_id, p.author_id, r.applicant_id,
            r.status, p.deleted_at IS NOT NULL AS post_deleted, p.food_name,
            owner.nickname AS owner_nickname, applicant.nickname AS applicant_nickname
        FROM reservations r JOIN food_posts p ON p.id = r.post_id
        JOIN users owner ON owner.id = p.author_id
        JOIN users applicant ON applicant.id = r.applicant_id
        LEFT JOIN chat_rooms c ON c.reservation_id = r.id
        """;

    // 예약 취소·완료와 방 생성·전송을 같은 예약 행에서 직렬화한다.
    @Select("SELECT status FROM reservations WHERE id = #{id} FOR UPDATE")
    String lockReservation(long id);

    @Select(ROOM_CONTEXT + "WHERE r.id = #{reservationId}")
    ContextRow byReservation(long reservationId);

    @Select(ROOM_CONTEXT + "WHERE c.id = #{roomId}")
    ContextRow byRoom(long roomId);

    @Insert("INSERT INTO chat_rooms (reservation_id) VALUES (#{reservationId})")
    int createRoom(long reservationId);

    @Select("SELECT LAST_INSERT_ID()")
    long insertedId();

    @Select("""
        SELECT id, chat_room_id, sender_id, content, created_at FROM chat_messages
        WHERE chat_room_id = #{roomId} AND id > #{afterId} ORDER BY id
        """)
    List<MessageRow> messages(@Param("roomId") long roomId, @Param("afterId") long afterId);

    @Insert("""
        INSERT INTO chat_messages (chat_room_id, sender_id, content) VALUES (#{roomId}, #{senderId}, #{content})
        """)
    int insertMessage(@Param("roomId") long roomId, @Param("senderId") long senderId, @Param("content") String content);

    @Select("SELECT id, chat_room_id, sender_id, content, created_at FROM chat_messages WHERE id = LAST_INSERT_ID()")
    MessageRow insertedMessage();

    // 게시글이 논리 삭제된 후에도 종료된 예약의 방은 이력에 남긴다.
    @Select("""
        SELECT c.id AS room_id, r.id AS reservation_id, r.status AS reservation_status, p.id AS post_id,
            p.deleted_at IS NOT NULL AS post_deleted, p.food_name,
            CASE WHEN p.author_id = #{userId} THEN applicant.nickname ELSE owner.nickname END AS counterpart_nickname
        FROM chat_rooms c JOIN reservations r ON r.id = c.reservation_id
        JOIN food_posts p ON p.id = r.post_id
        JOIN users owner ON owner.id = p.author_id
        JOIN users applicant ON applicant.id = r.applicant_id
        WHERE (p.author_id = #{userId} OR r.applicant_id = #{userId})
          AND r.status IN ('CANCELLED','COMPLETED')
        ORDER BY r.updated_at DESC, c.id DESC
        """)
    List<HistoryRow> history(long userId);
}
