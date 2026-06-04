package com.hospi.manage.core.security;

public interface HashingService {
    String hash(String raw);
    boolean matches(String raw, String hashed);
}
