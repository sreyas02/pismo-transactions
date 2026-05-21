package com.pismo.transactions.repository;

import com.pismo.transactions.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence for Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> { }
