package com.fridegeshare.chat;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.SessionUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
class ActivityPageController {
    private final ChatService chat;

    ActivityPageController(ChatService chat) { this.chat = chat; }

    @GetMapping("/mypage")
    String mypage() { return "mypage"; }

    @GetMapping("/chat-rooms/{roomId}")
    String room(@AuthenticationPrincipal SessionUser user, @PathVariable long roomId, Model model) {
        // 채팅 서비스에서 참여자 권한을 확인한 뒤에만 화면 데이터를 전달한다.
        model.addAttribute("room", chat.room(user, roomId));
        model.addAttribute("myId", user.id());
        return "chat/room";
    }

    @ExceptionHandler(ApiException.class)
    ModelAndView error(ApiException exception) {
        var page = new ModelAndView("shared/error");
        page.setStatus(exception.status());
        page.addObject("message", exception.getMessage());
        return page;
    }
}
