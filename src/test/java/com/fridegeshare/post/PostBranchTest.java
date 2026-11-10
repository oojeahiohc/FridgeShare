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

@SpringBootTest
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
        String form = json.writeValueAsString(
            new PostService.PostForm("당근 나눔", "당근", "3개", Inputs.today(), "상태 좋음"));

        var result = mvc.perform(post("/api/posts").with(user(owner)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(form))
            .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).at("/data/id").asLong();

        mvc.perform(get("/api/posts").with(user(outsider)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(get("/api/posts").param("keyword", "당근").with(user(owner)))
            .andExpect(jsonPath("$.data[0].id").value(id));
        mvc.perform(get("/api/posts/{id}", id).with(user(outsider)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.viewerRole").value("OTHER"));
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

    private SessionUser member(String region) {
        String login = UUID.randomUUID().toString();
        db.update("INSERT INTO users(login_id, password_hash, nickname, neighborhood) VALUES (?, ?, ?, ?)",
            login, "test-fixture", "게시판 검증", region);
        long id = db.queryForObject("SELECT id FROM users WHERE login_id = ?", Long.class, login);
        return new SessionUser(id, login, null, "게시판 검증", region);
    }
}
