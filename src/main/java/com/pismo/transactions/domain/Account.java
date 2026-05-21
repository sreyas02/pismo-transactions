package com.pismo.transactions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a cardholder's account.
 *
 * SRP: pure JPA entity — holds data, nothing else.
 */
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_accounts_document_number",
        columnNames = "document_number"
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    /** Factory method — keeps construction logic in the domain. */
    public static Account of(String documentNumber) {
        Account account = new Account();
        account.documentNumber = documentNumber;
        return account;
    }
}
