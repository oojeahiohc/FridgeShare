package com.fridegeshare.auth;

import com.fridegeshare.region.RegionDirectory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class SignupPageController {
    private final RegionDirectory regions;

    SignupPageController(RegionDirectory regions) {
        this.regions = regions;
    }

    @GetMapping("/signup")
    String signup(Model model) {
        model.addAttribute("regions", regions.all());
        return "auth/signup";
    }
}