package com.pismo.transactions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a single financial event associated with an account.
 *
 * SRP: pure JPA entity — no business logic beyond field declarations.
 * BigDecimal used throughout (never double/float) for financial precision.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    /**
     * FK to accounts — stored as plain Long to avoid N+1 issues
     * and to keep this entity a lightweight data carrier.
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_type_id", nullable = false)
    private OperationType operationType;

    /**
     * Positive for credit (voucher); negative for debit (purchase/withdrawal).
     * Sign is enforced by the service layer before persistence.
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    /** Factory method — assigns eventDate at creation time (SRP: domain decides). */
    public static Transaction of(Long accountId, OperationType operationType,
                                 BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.accountId     = accountId;
        tx.operationType = operationType;
        tx.amount        = amount;
        tx.eventDate     = LocalDateTime.now();
        return tx;
    }
}
