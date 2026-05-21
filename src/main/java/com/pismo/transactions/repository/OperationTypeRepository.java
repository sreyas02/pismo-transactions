package com.pismo.transactions.repository;

import com.pismo.transactions.domain.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence for OperationType — read-only after seed.
 */
@Repository
public interface OperationTypeRepository extends JpaRepository<OperationType, Integer> { }
