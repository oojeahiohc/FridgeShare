package com.fridegeshare.post;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.Inputs;
import com.fridegeshare.common.SessionUser;
import com.fridegeshare.region.RegionDirectory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
class PostPageController {
    private final PostService posts;
    private final RegionDirectory regions;

    PostPageController(PostService posts, RegionDirectory regions) {
        this.posts = posts;
        this.regions = regions;
    }

    @GetMapping("/")
    String home() { return "redirect:/posts"; }

    @GetMapping("/posts")
    String list(@AuthenticationPrincipal SessionUser user, Model model) {
        model.addAttribute("neighborhood", regions.label(user.neighborhoodCode()));
        return "posts/list";
    }

    @GetMapping("/posts/{postId}")
    String detail(@AuthenticationPrincipal SessionUser user, @PathVariable long postId, Model model) {
        var detail = posts.detail(user, postId);
        model.addAttribute("post", detail.post());
        model.addAttribute("editable", "OWNER".equals(detail.viewerRole()) && "AVAILABLE".equals(detail.post().status()));
        return "posts/detail";
    }

    @GetMapping("/posts/new")
    String create(@AuthenticationPrincipal SessionUser user, Model model) {
        model.addAttribute("neighborhood", regions.label(user.neighborhoodCode()));
        model.addAttribute("today", Inputs.today());
        return "posts/form";
    }

    @GetMapping("/posts/{postId}/edit")
    String edit(@AuthenticationPrincipal SessionUser user, @PathVariable long postId, Model model) {
        posts.requireEditable(user, postId);
        model.addAttribute("postId", postId);
        return create(user, model);
    }

    @ExceptionHandler(ApiException.class)
    ModelAndView error(ApiException exception) {
        var page = new ModelAndView("shared/error");
        page.setStatus(exception.status());
        page.addObject("message", exception.getMessage());
        return page;
    }
}
