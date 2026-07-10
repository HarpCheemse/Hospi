package com.hospi.manage.features.config.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.audit.service.AuditService;
import com.hospi.manage.features.config.dto.SystemConfigForm;
import com.hospi.manage.features.config.service.SystemConfigService;
import com.hospi.manage.core.security.session.AccountPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for managing system-wide configuration settings.
 */
@Controller
@RequestMapping("/admin/configs")
@RequiredArgsConstructor
public class SystemConfigController {
    private final SystemConfigService systemConfigService;
    private final AuditService auditService;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "SYSTEM_CONFIGS");
    }

    @GetMapping
    public String config(Model model) {
        model.addAttribute("configs", systemConfigService.getConfig());
        return "config/list";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute(Attributes.FORM, systemConfigService.getForm());
        return "config/edit";
    }

    @PostMapping("/edit")
    public String updateConfigs(@Valid @ModelAttribute(Attributes.FORM) SystemConfigForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "config/edit";
        }

        systemConfigService.updateSystemConfigs(form);
        auditService.log(null, currentStaffName(), "UPDATE", "SYSTEM_CONFIG", null,
                "System configuration updated");

        redirectAttributes.addFlashAttribute(Attributes.SUCCESS, "Configs updated successfully");
        return "redirect:/admin/configs";
    }

    private String currentStaffName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountPrincipal p) {
            return p.getAccount().getFullName();
        }
        return "System";
    }
}
