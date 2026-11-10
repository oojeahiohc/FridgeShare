package com.fridegeshare.chat;

import java.util.Map;

import com.fridegeshare.common.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ChatApiController {
    record MessageForm(String content) {}

    private final ChatService chat;

    ChatApiController(ChatService chat) { this.chat = chat; }

    @PostMapping("/api/reservations/{reservationId}/chat-room")
    ResponseEntity<Map<String, Object>> open(@AuthenticationPrincipal SessionUser user, @PathVariable long reservationId) {
        var result = chat.open(user, reservationId);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
            .body(Map.of("data", result.room()));
    }

    @GetMapping("/api/chat-rooms/{roomId}/messages")
    ChatService.Feed messages(@AuthenticationPrincipal SessionUser user, @PathVariable long roomId,
                              @RequestParam(defaultValue = "0") long afterId) {
        return chat.messages(user, roomId, afterId);
    }

    @PostMapping("/api/chat-rooms/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Object> send(@AuthenticationPrincipal SessionUser user, @PathVariable long roomId,
                             @RequestBody MessageForm form) {
        return Map.of("data", chat.send(user, roomId, form.content()));
    }

    @GetMapping("/api/me/chat-rooms")
    Map<String, Object> history(@AuthenticationPrincipal SessionUser user) {
        return Map.of("data", chat.history(user));
    }
}
