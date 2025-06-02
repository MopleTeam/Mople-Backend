package com.groupMeeting.admin.service;

import com.groupMeeting.admin.repository.AdminRepository;
import com.groupMeeting.dto.request.admin.AdminLoginRequest;
import com.groupMeeting.dto.response.admin.AdminCommentResponse;
import com.groupMeeting.dto.response.admin.AdminLoginResponse;
import com.groupMeeting.dto.response.admin.AdminPlanResponse;
import com.groupMeeting.dto.response.admin.AdminReviewResponse;
import com.groupMeeting.entity.user.Admin;
import com.groupMeeting.meet.repository.comment.CommentReportRepository;
import com.groupMeeting.meet.repository.plan.PlanReportRepository;
import com.groupMeeting.meet.repository.review.ReviewReportRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PlanReportRepository planReportRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final CommentReportRepository commentReportRepository;

    public AdminLoginResponse loginAdmin(AdminLoginRequest request) {
        Optional<Admin> admin = adminRepository.findByNameAndPw(request.getName(), request.getPw());
        return admin.map(AdminLoginResponse::new).orElse(null);
    }

    public String getAdminAccount(Long adminId) {
        if(isNull(adminId)) {
            return null;
        }

        Optional<Admin> admin = adminRepository.findById(adminId);

        return admin.map(Admin::getName).orElse(null);
    }

    public List<AdminPlanResponse> getAllPlanReports() {
        return planReportRepository.allPlanReport().stream().map(AdminPlanResponse::new).toList();
    }

    public List<?> getAllCommentReports() {
        return commentReportRepository.allCommentReport().stream().map(AdminCommentResponse::new).toList();
    }

    public List<AdminReviewResponse> getAllReviewReports() {
        return reviewReportRepository.allReviewReport().stream().map(AdminReviewResponse::new).toList();
    }

//    public List<?> getAllBlackUsers() {
//        return commentReportRepository.allReport();
//    }
}
