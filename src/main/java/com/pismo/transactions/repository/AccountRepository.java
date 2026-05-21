package com.pismo.transactions.repository;

import com.pismo.transactions.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence for Account entities.
 * DIP: service depends on this interface — Spring Data generates the implementation.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByDocumentNumber(String documentNumber);
    Optional<Account> findByDocumentNumber(String documentNumber);
}
