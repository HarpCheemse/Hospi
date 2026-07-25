package com.hospi.manage.common.utils;

import com.hospi.manage.core.security.session.AccountPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Static utilities for accessing the current authenticated user. */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** Return the full name of the currently authenticated staff member, or "System". */
    public static String currentStaffName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountPrincipal p) {
            return p.getAccount().getFullName();
        }
        return "System";
    }

    /** Return the ID of the currently authenticated staff member, or null if not authenticated. */
    public static Long currentStaffId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountPrincipal p) {
            return p.getAccount().getId();
        }
        return null;
    }
}
