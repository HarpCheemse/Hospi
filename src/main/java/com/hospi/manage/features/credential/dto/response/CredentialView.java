package com.hospi.manage.features.credential.dto.response;

import java.time.LocalDateTime;

/** View model for displaying credential information. */
public record CredentialView(
        String name,

        String email,

        String role,

        String phone,

        String status,

        LocalDateTime createdAt

) {

}