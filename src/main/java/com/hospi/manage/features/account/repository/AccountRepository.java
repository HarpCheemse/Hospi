package com.hospi.manage.features.account.repository;

import com.hospi.manage.features.account.entity.Account;
import com.hospi.manage.features.account.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repository for {@link Account} entity — provides staff account lookup and uniqueness checks. */
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Account> findByRole(Role role);
}
