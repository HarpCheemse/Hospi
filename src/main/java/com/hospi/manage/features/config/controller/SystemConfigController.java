package com.hospi.manage.features.config.controller;

import com.hospi.manage.common.constant.Attributes;
import com.hospi.manage.features.config.dto.SystemConfigForm;
import com.hospi.manage.features.config.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /**
     * Set the active sidebar highlight for this feature.
     */
    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "SYSTEM_CONFIGS");
    }

    /**
     * Show the system configuration listing page.
     */
    @GetMapping
    public String config(Model model) {


        model.addAttribute(
                "configs",
                systemConfigService.getConfig()
        );

        return "config/list";
    }

    /**
     * Show the configuration edit form pre-populated with current values.
     */
    @GetMapping("/edit")
    public String edit(Model model) {
        model.addAttribute(Attributes.FORM, systemConfigService.getForm());

        return "config/edit";
    }

    /**
     * Update system configuration values. Validates input, persists changes, then
     * redirects to the config listing.
     */
    @PostMapping("/edit")
    public String updateConfigs(@Valid @ModelAttribute(Attributes.FORM) SystemConfigForm form,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "config/edit";
        }

        systemConfigService.updateSystemConfigs(form);

        redirectAttributes.addFlashAttribute(
                Attributes.SUCCESS,
                "Configs updated successfully"
        );
        return "redirect:/admin/configs";
    }
}
