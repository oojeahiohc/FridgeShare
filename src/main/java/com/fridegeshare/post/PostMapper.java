package com.fridegeshare.post;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
interface PostMapper {
    record PostRow(Long id, Long authorId, String authorNickname, String title, String foodName,
                   String quantity, java.time.LocalDate expiryDate, String description,
                   String neighborhoodCode, String status, LocalDateTime createdAt,
                   Long reservationId, Long applicantId, Long chatRoomId) {}

    record MineRow(Long id, String title, String status, LocalDateTime createdAt) {}

    String POST_FIELDS = """
        p.id, p.author_id, u.nickname AS author_nickname, p.title, p.food_name, p.quantity,
        p.expiry_date, p.description, p.neighborhood AS neighborhood_code, p.status, p.created_at
        """;

    // 게시글 목록과 전체 건수에 같은 지역 및 검색 조건을 적용한다.
    String LIST_FILTER = """
        WHERE p.deleted_at IS NULL AND p.neighborhood LIKE CONCAT(#{districtPrefix}, '%')
          AND (#{keyword} IS NULL OR LOCATE(#{keyword}, p.title) > 0 OR LOCATE(#{keyword}, p.food_name) > 0)
        """;

    @Select("SELECT " + POST_FIELDS + """
        , NULL AS reservation_id, NULL AS applicant_id, NULL AS chat_room_id
        FROM food_posts p JOIN users u ON u.id = p.author_id
        """ + LIST_FILTER + """
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """)
    List<PostRow> list(@Param("districtPrefix") String districtPrefix, @Param("keyword") String keyword,
                       @Param("limit") int limit, @Param("offset") long offset);

    @Select("SELECT COUNT(*) FROM food_posts p " + LIST_FILTER)
    long count(@Param("districtPrefix") String districtPrefix, @Param("keyword") String keyword);

    @Select("SELECT " + POST_FIELDS + """
        , r.id AS reservation_id, r.applicant_id, c.id AS chat_room_id
        FROM food_posts p JOIN users u ON u.id = p.author_id
        LEFT JOIN reservations r ON r.post_id = p.id AND r.status IN ('RESERVED','COMPLETED')
        LEFT JOIN chat_rooms c ON c.reservation_id = r.id
        WHERE p.id = #{id} AND p.deleted_at IS NULL
        """)
    PostRow byId(long id);

    @Select("""
        SELECT id, title, status, created_at
        FROM food_posts WHERE author_id = #{authorId} AND deleted_at IS NULL
        ORDER BY created_at DESC, id DESC
        """)
    List<MineRow> mine(long authorId);

    @Insert("""
        INSERT INTO food_posts (author_id, title, food_name, quantity, expiry_date, description, neighborhood)
        VALUES (#{authorId}, #{form.title}, #{form.foodName}, #{form.quantity},
                #{form.expiryDate}, #{form.description}, #{neighborhood})
        """)
    int insert(@Param("authorId") long authorId, @Param("neighborhood") String neighborhood,
               @Param("form") PostService.PostForm form);

    @Select("SELECT LAST_INSERT_ID()")
    long insertedId();

    @Update("""
        UPDATE food_posts
        SET title = #{form.title}, food_name = #{form.foodName}, quantity = #{form.quantity},
            expiry_date = #{form.expiryDate}, description = #{form.description}
        WHERE id = #{id} AND author_id = #{authorId} AND status = 'AVAILABLE' AND deleted_at IS NULL
        """)
    int update(@Param("id") long id, @Param("authorId") long authorId, @Param("form") PostService.PostForm form);

    @Update("""
        UPDATE food_posts SET deleted_at = CURRENT_TIMESTAMP
        WHERE id = #{id} AND author_id = #{authorId} AND status = 'AVAILABLE' AND deleted_at IS NULL
        """)
    int delete(@Param("id") long id, @Param("authorId") long authorId);
}
