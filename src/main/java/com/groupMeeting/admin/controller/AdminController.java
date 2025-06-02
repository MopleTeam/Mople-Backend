package com.groupMeeting.admin.controller;

import com.groupMeeting.admin.service.AdminService;
import com.groupMeeting.dto.request.admin.AdminLoginRequest;
import com.groupMeeting.dto.response.admin.AdminActiveResponse;
import com.groupMeeting.dto.response.admin.AdminHeaderResponse;
import com.groupMeeting.dto.response.admin.AdminLoginResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@Controller
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping(value = {"", "/"})
    public String home(@CookieValue(name = "id", required = false) Long id, Model model) {
        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.home());
        model.addAttribute("active", AdminActiveResponse.home());

        String adminAccount = adminService.getAdminAccount(id);

        if (!isNull(adminAccount)) {
            model.addAttribute("nickname", adminAccount);
        }

        return "admin/home";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.home());
        model.addAttribute("active", AdminActiveResponse.home());

        model.addAttribute("adminLoginRequest", new AdminLoginRequest());
        return "admin/user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute AdminLoginRequest adminLoginRequest, BindingResult bindingResult,
                        HttpServletResponse response, Model model) {
        model.addAttribute("adminPath", "v1/admin");

        AdminLoginResponse login = adminService.loginAdmin(adminLoginRequest);

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if (isNull(login)) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "admin/user/login";
        }

        // 로그인 성공 => 쿠키 생성
        Cookie cookie = new Cookie("id", String.valueOf(login.id()));
        cookie.setMaxAge(180);  // 쿠키 유효 시간 : 30분
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);

        return "redirect:/v1/admin/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response, Model model) {
        model.addAttribute("adminPath", "v1/admin");

        Cookie cookie = new Cookie("id", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/v1/admin";
    }

    @GetMapping("/plan")
    public String getPlanList(@CookieValue(name = "id", required = false) Long id, Model model) {
        if (isNull(id)) {
            return "redirect:/v1/admin";
        }

        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.plan());
        model.addAttribute("active", AdminActiveResponse.plan());
        model.addAttribute("plans", adminService.getAllPlanReports());

        String adminAccount = adminService.getAdminAccount(id);

        if (!isNull(adminAccount)) {
            model.addAttribute("nickname", adminAccount);
        }

        return "admin/plan/list";
    }


    @GetMapping("/review")
    public String getReviewList(@CookieValue(name = "id", required = false) Long id, Model model) {
        if (isNull(id)) {
            return "redirect:/v1/admin";
        }

        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.review());
        model.addAttribute("active", AdminActiveResponse.review());
        model.addAttribute("reviews", adminService.getAllReviewReports());

        String adminAccount = adminService.getAdminAccount(id);

        if (!isNull(adminAccount)) {
            model.addAttribute("nickname", adminAccount);
        }

        return "admin/review/list";
    }

    @GetMapping("/comment")
    public String getCommentList(@CookieValue(name = "id", required = false) Long id, Model model) {
        if (isNull(id)) {
            return "redirect:/v1/admin";
        }

        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.comment());
        model.addAttribute("active", AdminActiveResponse.comment());
        model.addAttribute("comments", adminService.getAllCommentReports());

        String adminAccount = adminService.getAdminAccount(id);

        if (!isNull(adminAccount)) {
            model.addAttribute("nickname", adminAccount);
        }

        return "admin/comment/list";
    }

    @Deprecated
    @GetMapping("/user")
    public String getUserList(@CookieValue(name = "id", required = false) Long id, Model model) {
        if (isNull(id)) {
            return "redirect:/v1/admin";
        }

        model.addAttribute("adminPath", "v1/admin");
        model.addAttribute("attribute", AdminHeaderResponse.user());
        model.addAttribute("active", AdminActiveResponse.user());

        String adminAccount = adminService.getAdminAccount(id);

        if (!isNull(adminAccount)) {
            model.addAttribute("nickname", adminAccount);
        }

        return "admin/user/list";
    }
}
