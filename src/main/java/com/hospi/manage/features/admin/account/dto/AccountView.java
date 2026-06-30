package com.hospi.manage.features.admin.account.dto;

import com.hospi.manage.features.admin.account.enums.AccountStatus;
import com.hospi.manage.features.admin.account.enums.Role;

/** View model for displaying an account in a list. */
public record AccountView(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        AccountStatus status
) {
}