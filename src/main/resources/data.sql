-- Seed operation types — runs after Hibernate creates the schema
INSERT INTO operation_types (operation_type_id, description) VALUES (1, 'Normal Purchase');
INSERT INTO operation_types (operation_type_id, description) VALUES (2, 'Purchase with installments');
INSERT INTO operation_types (operation_type_id, description) VALUES (3, 'Withdrawal');
INSERT INTO operation_types (operation_type_id, description) VALUES (4, 'Credit Voucher');
