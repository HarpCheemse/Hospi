package com.hospi.manage.features.payment.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for PayPal REST API (client ID, secret, and mode). */
@ConfigurationProperties(prefix = "paypal")
public record PayPalProperties(
        String clientId,
        String clientSecret,
        String mode
) {}
