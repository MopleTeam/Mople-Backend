package com.groupMeeting.admin.controller;

import com.groupMeeting.admin.service.PolicyService;
import com.groupMeeting.entity.version.ApiVersionPolicy;
import com.groupMeeting.entity.version.ForceUpdatePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/v1/admin/policy")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping("/force")
    public String getForeUpdatePolicyList(Model model) {
        model.addAttribute("policies", policyService.getAllForceUpdatePolicy());
        return "admin/policy/force/list";
    }

    @GetMapping("/force/add")
    public String addForeUpdatePolicy(Model model) {
        model.addAttribute("policy", new ForceUpdatePolicy());
        return "admin/policy/force/add";
    }

    @PostMapping("/force/add")
    public String addForeUpdatePolicy(@ModelAttribute ForceUpdatePolicy policy) {
        policyService.saveForceUpdatePolicy(policy);
        return "redirect:/v1/admin/policy/force";
    }

    @GetMapping("/force/edit/{id}")
    public String updateForeUpdatePolicy(@PathVariable Long id, Model model) {
        ForceUpdatePolicy policy = policyService.findForceUpdatePolicyById(id);
        model.addAttribute("policy", policy);
        return "admin/policy/force/edit";
    }

    @PostMapping("/force/edit/{id}")
    public String updateForeUpdatePolicy(@PathVariable Long id, @ModelAttribute ForceUpdatePolicy updatedPolicy) {
        policyService.updateForceUpdatePolicy(id, updatedPolicy);
        return "redirect:/v1/admin/policy/force";
    }

    @PostMapping("/force/delete/{id}")
    public String deleteForeUpdatePolicy(@PathVariable Long id) {
        policyService.deleteForceUpdatePolicy(id);
        return "redirect:/v1/admin/policy/force";
    }

    @GetMapping("/api")
    public String getApiVersionPolicyList(Model model) {
        model.addAttribute("policies", policyService.getAllApiVersionPolicy());
        return "admin/policy/api/list";
    }

    @GetMapping("/api/add")
    public String addApiVersionPolicy(Model model) {
        model.addAttribute("policy", new ApiVersionPolicy());
        return "admin/policy/api/add";
    }

    @PostMapping("/api/add")
    public String addApiVersionPolicy(@ModelAttribute ApiVersionPolicy policy) {
        policyService.saveApiVersionPolicy(policy);
        return "redirect:/v1/admin/policy/api";
    }

    @GetMapping("/api/edit/{id}")
    public String updateApiVersionPolicy(@PathVariable Long id, Model model) {
        ApiVersionPolicy policy = policyService.findApiVersionPolicyById(id);
        model.addAttribute("policy", policy);
        return "admin/policy/api/edit";
    }

    @PostMapping("/api/edit/{id}")
    public String updateApiVersionPolicy(@PathVariable Long id, @ModelAttribute ApiVersionPolicy updatedPolicy) {
        policyService.updateApiVersionPolicy(id, updatedPolicy);
        return "redirect:/v1/admin/policy/api";
    }

    @PostMapping("/api/delete/{id}")
    public String deleteApiVersionPolicy(@PathVariable Long id) {
        policyService.deleteApiVersionPolicy(id);
        return "redirect:/v1/admin/policy/api";
    }
}
