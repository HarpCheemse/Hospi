package com.hospi.manage.features.account.dto;

import com.hospi.manage.features.account.enums.Role;

/** View model for displaying an account in a list. */
public record AccountView(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role
) {
}
