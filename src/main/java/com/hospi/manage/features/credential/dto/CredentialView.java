package com.hospi.manage.features.credential.dto;

import java.time.LocalDateTime;

public record CredentialView(
        String name,

        String email,

        String role,

        String phone,

        String status,

        LocalDateTime createdAt

) {

}