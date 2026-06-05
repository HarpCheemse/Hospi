package com.hospi.manage.core.security.hash;

public interface HashingService {
    String hash(String raw);
    boolean matches(String raw, String hashed);
}
