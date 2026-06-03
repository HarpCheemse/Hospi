package com.hospi.manage.features.admin.config.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.admin.config.dto.SystemConfigForm;
import com.hospi.manage.features.admin.config.service.SystemConfigService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configs")
public class AdminConfigController {
    private final SystemConfigService systemConfigService;

    AdminConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @ModelAttribute
    void addCommonAttributues(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "SYSTEM_CONFIGS");
    }

    @GetMapping
    public String config(Model model) {


        model.addAttribute(
                "configs",
                systemConfigService.getConfig()
        );

        return "admin/config/list";
    }

    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute(Attributes.FORM, systemConfigService.getForm());

        return "admin/config/edit";
    }

    @PostMapping("/edit")
    public String updateConfigs(@Valid @ModelAttribute(Attributes.FORM) SystemConfigForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/config/edit";
        }

        systemConfigService.updateSystemConfigs(form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Configs updated successfully"
        );
        return "redirect:/admin/configs";
    }
}
