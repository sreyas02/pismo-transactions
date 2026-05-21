package com.pismo.transactions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * Represents a predefined category of financial operation.
 *
 * SRP: pure domain entity — no HTTP, no service logic.
 * The sign-rule (debit vs credit) is encoded here, in the domain, not in the service.
 */
@Entity
@Table(name = "operation_types")
@Getter
@Setter
@NoArgsConstructor
public class OperationType {

    /** Operation type IDs that require a negative (debit) amount. */
    private static final Set<Integer> DEBIT_TYPE_IDS = Set.of(1, 2, 3);

    @Id
    @Column(name = "operation_type_id")
    private Integer operationTypeId;

    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Returns true if transactions of this type must carry a negative amount.
     * OCP: adding a new type only requires updating the DEBIT_TYPE_IDS set.
     */
    public boolean isDebit() {
        return DEBIT_TYPE_IDS.contains(this.operationTypeId);
    }
}
