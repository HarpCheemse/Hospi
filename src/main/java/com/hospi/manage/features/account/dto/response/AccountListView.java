package com.hospi.manage.features.account.dto.response;

import com.hospi.manage.features.account.dto.AccountView;
import com.hospi.manage.features.account.enums.Role;

import java.util.List;

/** View model for the staff account list page. */
public record AccountListView(
        List<AccountView> accounts,
        List<Role> roles,
        String activeRole,
        String filterSearch
) {}
