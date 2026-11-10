package com.fridegeshare.post;

import java.util.Map;

import com.fridegeshare.common.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PostApiController {
    private final PostService posts;

    PostApiController(PostService posts) { this.posts = posts; }

    @GetMapping("/api/posts")
    Map<String, Object> list(@AuthenticationPrincipal SessionUser user, @RequestParam(required = false) String keyword) {
        return Map.of("data", posts.list(user, keyword));
    }

    @GetMapping("/api/posts/{postId}")
    Map<String, Object> detail(@AuthenticationPrincipal SessionUser user, @PathVariable long postId) {
        return Map.of("data", posts.detail(user, postId));
    }

    @PostMapping("/api/posts")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> create(@AuthenticationPrincipal SessionUser user, @RequestBody PostService.PostForm form) {
        return Map.of("data", posts.create(user, form));
    }

    @PutMapping("/api/posts/{postId}")
    Map<String, Object> update(@AuthenticationPrincipal SessionUser user, @PathVariable long postId,
                               @RequestBody PostService.PostForm form) {
        return Map.of("data", posts.update(user, postId, form));
    }

    @DeleteMapping("/api/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal SessionUser user, @PathVariable long postId) {
        posts.delete(user, postId);
    }

    @GetMapping("/api/me/posts")
    Map<String, Object> mine(@AuthenticationPrincipal SessionUser user) {
        return Map.of("data", posts.mine(user));
    }
}
