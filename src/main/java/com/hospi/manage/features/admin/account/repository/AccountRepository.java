package com.hospi.manage.features.admin.account.repository;

import com.hospi.manage.features.admin.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
