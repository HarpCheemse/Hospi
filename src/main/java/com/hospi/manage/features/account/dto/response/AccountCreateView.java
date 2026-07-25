package com.hospi.manage.features.account.dto.response;

import com.hospi.manage.features.account.enums.Role;

import java.util.List;

/** View model for the account creation page. */
public record AccountCreateView(
        List<Role> roles
) {}
