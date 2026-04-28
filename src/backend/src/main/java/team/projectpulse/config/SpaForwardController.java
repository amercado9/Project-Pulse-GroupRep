package team.projectpulse.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/login",
            "/register",
            "/forget-password",
            "/reset-password",
            "/join",
            "/home",
            "/war",
            "/peer-evaluations",
            "/peer-evaluation-report",
            "/sections",
            "/teams",
            "/instructors",
            "/students",
            "/rubrics",
            "/user",
            "/403",
            "/sections/**",
            "/teams/**",
            "/instructors/**",
            "/students/**",
            "/user/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
