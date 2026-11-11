package com.fridegeshare.post;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fridegeshare.common.Inputs;
import com.fridegeshare.common.SessionUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=${DB_URL:jdbc:mariadb://localhost:3309/fridegeshare}")
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class PostBranchTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired ObjectMapper json;

    @Test
    void neighborhoodAndOwnerRulesApplyToCrud() throws Exception {
        var owner = member("1111051500");
        var outsider = member("1111053000");
        var otherDistrict = member("1114052000");
        String form = json.writeValueAsString(
            new PostService.PostForm("당근 나눔", "당근", "3개", Inputs.today(), "상태 좋음"));

        var result = mvc.perform(post("/api/posts").with(user(owner)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(form))
            .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).at("/data/id").asLong();

        mvc.perform(get("/api/posts").with(user(otherDistrict)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.posts").isEmpty())
            .andExpect(jsonPath("$.data.totalCount").value(0))
            .andExpect(jsonPath("$.data.totalPages").value(0))
            .andExpect(jsonPath("$.data.hasPrevious").value(false))
            .andExpect(jsonPath("$.data.hasNext").value(false));
        mvc.perform(get("/api/posts").param("keyword", "당근").with(user(outsider)))
            .andExpect(jsonPath("$.data.posts[0].id").value(id));
        mvc.perform(get("/api/posts/{id}", id).with(user(outsider)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.viewerRole").value("OTHER"));
        mvc.perform(get("/posts/{id}", id).with(user(outsider)))
            .andExpect(status().isOk());
        mvc.perform(get("/api/posts/{id}", id).with(user(otherDistrict)))
            .andExpect(status().isNotFound());
        mvc.perform(get("/posts/{id}", id).with(user(otherDistrict)))
            .andExpect(status().isNotFound());
        mvc.perform(put("/api/posts/{id}", id).with(user(outsider)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(form))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("NOT_POST_OWNER"));
        mvc.perform(put("/api/posts/{id}", id).with(user(owner)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(form.replace("3개", "4개")))
            .andExpect(status().isOk());
        mvc.perform(delete("/api/posts/{id}", id).with(user(owner)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/{id}", id).with(user(owner))).andExpect(status().isNotFound());
    }

    @Test
    void searchPagesShareDistrictAndDeletionFiltersWithTotalCount() throws Exception {
        var reader = member("1111051500");
        var sameDistrict = member("1111053000");
        var otherDistrict = member("1114052000");
        String keyword = UUID.randomUUID().toString();
        insertPost(sameDistrict, "식재료명으로 검색", keyword, false);
        for (int i = 2; i <= 11; i++) {
            insertPost(sameDistrict, keyword + " " + i, "당근", false);
        }
        insertPost(otherDistrict, keyword, "다른 시·군·구", false);
        insertPost(sameDistrict, keyword, "삭제된 글", true);
        insertPost(sameDistrict, "검색어 불일치", "당근", false);

        mvc.perform(get("/api/posts").param("keyword", keyword).with(user(reader)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(10))
            .andExpect(jsonPath("$.data.posts.length()").value(10))
            .andExpect(jsonPath("$.data.posts[0].title").value(keyword + " 11"))
            .andExpect(jsonPath("$.data.posts[9].title").value(keyword + " 2"))
            .andExpect(jsonPath("$.data.totalCount").value(11))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.hasPrevious").value(false))
            .andExpect(jsonPath("$.data.hasNext").value(true));
        mvc.perform(get("/api/posts").param("keyword", keyword).param("page", "2").with(user(reader)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.posts.length()").value(1))
            .andExpect(jsonPath("$.data.posts[0].foodName").value(keyword))
            .andExpect(jsonPath("$.data.hasPrevious").value(true))
            .andExpect(jsonPath("$.data.hasNext").value(false));
        mvc.perform(get("/api/posts").param("keyword", keyword).param("page", "3").with(user(reader)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.page").value(3))
            .andExpect(jsonPath("$.data.posts").isEmpty())
            .andExpect(jsonPath("$.data.totalCount").value(11))
            .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    void rejectsInvalidPageNumbers() throws Exception {
        var reader = member("1111051500");
        for (String invalid : new String[]{"", "0", "-1", "abc", "2147483648"}) {
            mvc.perform(get("/api/posts").param("page", invalid).with(user(reader)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
    }

    private void insertPost(SessionUser author, String title, String foodName, boolean deleted) {
        db.update("""
            INSERT INTO food_posts (author_id, title, food_name, quantity, expiry_date, description,
                                    neighborhood, created_at, deleted_at)
            VALUES (?, ?, ?, '1개', ?, '페이지 검증', ?, '2026-01-01 12:00:00',
                    CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END)
            """, author.id(), title, foodName, Inputs.today(), author.neighborhoodCode(), deleted);
    }

    private SessionUser member(String region) {
        String login = UUID.randomUUID().toString();
        db.update("INSERT INTO users(login_id, password_hash, nickname, neighborhood) VALUES (?, ?, ?, ?)",
            login, "test-fixture", "게시판 검증", region);
        long id = db.queryForObject("SELECT id FROM users WHERE login_id = ?", Long.class, login);
        return new SessionUser(id, login, null, "게시판 검증", region);
    }
}
