package com.fridegeshare.auth;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
interface UserMapper {
    record UserRow(Long id, String loginId, String passwordHash, String nickname, String neighborhood) {
    }

    @Select("""
            SELECT id, login_id, password_hash, nickname, neighborhood
            FROM users WHERE login_id = #{loginId}
            """)
    UserRow byLoginId(String loginId);

    @Insert("""
            INSERT INTO users (login_id, password_hash, nickname, neighborhood)
            VALUES (#{loginId}, #{passwordHash}, #{nickname}, #{neighborhood})
            """)
    int insert(@Param("loginId") String loginId, @Param("passwordHash") String passwordHash,
            @Param("nickname") String nickname, @Param("neighborhood") String neighborhood);
}