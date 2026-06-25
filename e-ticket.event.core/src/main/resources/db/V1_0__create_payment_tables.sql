-- =====================================================
-- SCRIPT: V1_0__create_payment_tables.sql
-- Banco: PostgreSQL
-- Métodos: MPESA, EMOLA, CARD (SYNC) + MKASH (ASYNC)
-- =====================================================

-- 1. DESABILITAR CONSTRAINTS
SET session_replication_role = replica;

-- 2. REMOVER TABELAS EXISTENTES
DROP TABLE IF EXISTS payment_callback_queue CASCADE;
DROP TABLE IF EXISTS payment_audit_log CASCADE;
DROP TABLE IF EXISTS payment_pending CASCADE;
DROP TABLE IF EXISTS payment_failure CASCADE;
DROP TABLE IF EXISTS payment_success CASCADE;
DROP TABLE IF EXISTS ticket_reservation CASCADE;
DROP TABLE IF EXISTS user_event_reservation_control CASCADE;
DROP TABLE IF EXISTS payment_transaction CASCADE;
DROP TABLE IF EXISTS payment_method CASCADE;

-- 3. PAYMENT_METHOD
CREATE TABLE payment_method (
                                id BIGSERIAL PRIMARY KEY,
                                code VARCHAR(30) NOT NULL UNIQUE,
                                name VARCHAR(100) NOT NULL,
                                flow_type VARCHAR(20) NOT NULL DEFAULT 'SYNCHRONOUS',
                                icon_url VARCHAR(500),
                                display_order INT DEFAULT 0,
                                provider_id BIGINT,
                                requires_phone BOOLEAN DEFAULT FALSE,
                                requires_card BOOLEAN DEFAULT FALSE,
                                requires_qr_code BOOLEAN DEFAULT FALSE,
                                min_amount DECIMAL(15,2),
                                max_amount DECIMAL(15,2),
                                is_active BOOLEAN DEFAULT TRUE,
                                display_config JSONB,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(100),
                                updated_at TIMESTAMP,
                                updated_by VARCHAR(100),
                                state VARCHAR(20) DEFAULT 'ACTIVE',
                                version BIGINT DEFAULT 0
);

COMMENT ON TABLE payment_method IS 'Métodos de pagamento disponíveis';
COMMENT ON COLUMN payment_method.flow_type IS 'SYNCHRONOUS (resposta imediata) ou ASYNCHRONOUS (callback)';

-- 4. PAYMENT_TRANSACTION
CREATE TABLE payment_transaction (
                                     id BIGSERIAL PRIMARY KEY,
                                     transaction_id VARCHAR(50) NOT NULL UNIQUE,
                                     external_id VARCHAR(100),
                                     sale_id BIGINT NOT NULL,
                                     event_id BIGINT NOT NULL,
                                     user_id BIGINT,
                                     order_id VARCHAR(50),
                                     payment_method_id BIGINT NOT NULL,
                                     payment_method_code VARCHAR(30) NOT NULL,
                                     amount DECIMAL(15,2) NOT NULL,
                                     currency VARCHAR(3) DEFAULT 'MZN',
                                     status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                     status_code INT,
                                     status_message VARCHAR(500),
                                     retry_count INT DEFAULT 0,
                                     max_retries INT DEFAULT 3,
                                     success_id BIGINT,
                                     failure_id BIGINT,
                                     pending_id BIGINT,
                                     reservation_id BIGINT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(100),
                                     updated_at TIMESTAMP,
                                     updated_by VARCHAR(100),
                                     state VARCHAR(20) DEFAULT 'ACTIVE',
                                     version BIGINT DEFAULT 0,
                                     CONSTRAINT fk_pt_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_method(id)
);

COMMENT ON TABLE payment_transaction IS 'Controle do fluxo de pagamento (leve)';
COMMENT ON COLUMN payment_transaction.status IS 'PENDING, PROCESSING, SUCCESS, FAILED, EXPIRED, AWAITING_CONFIRMATION';

-- 5. PAYMENT_SUCCESS
CREATE TABLE payment_success (
                                 id BIGSERIAL PRIMARY KEY,
                                 payment_transaction_id VARCHAR(50) NOT NULL UNIQUE,
                                 provider_transaction_id VARCHAR(100),
                                 provider_result_code VARCHAR(50),
                                 provider_result_desc VARCHAR(500),
                                 confirmed_amount DECIMAL(15,2) NOT NULL,
                                 fee_amount DECIMAL(15,2),
                                 net_amount DECIMAL(15,2),
                                 currency VARCHAR(3),
                                 request_payload JSONB,
                                 response_payload JSONB,
                                 callback_payload JSONB,
                                 receipt_url VARCHAR(500),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(100),
                                 updated_at TIMESTAMP,
                                 updated_by VARCHAR(100),
                                 state VARCHAR(20) DEFAULT 'ACTIVE',
                                 version BIGINT DEFAULT 0
);

-- 6. PAYMENT_FAILURE
CREATE TABLE payment_failure (
                                 id BIGSERIAL PRIMARY KEY,
                                 payment_transaction_id VARCHAR(50) NOT NULL,
                                 attempt_number INT NOT NULL DEFAULT 1,
                                 provider_result_code VARCHAR(50),
                                 provider_result_desc VARCHAR(500),
                                 http_status_code INT,
                                 failure_category VARCHAR(50),
                                 failure_code VARCHAR(50),
                                 failure_message VARCHAR(500),
                                 failure_details TEXT,
                                 can_retry BOOLEAN DEFAULT TRUE,
                                 retry_recommended BOOLEAN DEFAULT TRUE,
                                 resolved BOOLEAN DEFAULT FALSE,
                                 resolution_action VARCHAR(50),
                                 resolved_by VARCHAR(100),
                                 resolution_notes TEXT,
                                 request_payload JSONB,
                                 response_payload JSONB,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(100),
                                 updated_at TIMESTAMP,
                                 updated_by VARCHAR(100),
                                 state VARCHAR(20) DEFAULT 'ACTIVE',
                                 version BIGINT DEFAULT 0
);

-- 7. PAYMENT_PENDING (para métodos assíncronos como MKASH)
CREATE TABLE payment_pending (
                                 id BIGSERIAL PRIMARY KEY,
                                 payment_transaction_id VARCHAR(50) NOT NULL UNIQUE,
                                 pending_type VARCHAR(30) NOT NULL,
                                 payment_url VARCHAR(500),
                                 qr_code_url VARCHAR(500),
                                 qr_code_base64 TEXT,
                                 instructions TEXT,
                                 expires_at TIMESTAMP NOT NULL,
                                 status VARCHAR(20) DEFAULT 'ACTIVE',
                                 request_payload JSONB,
                                 response_payload JSONB,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by VARCHAR(100),
                                 updated_at TIMESTAMP,
                                 updated_by VARCHAR(100),
                                 state VARCHAR(20) DEFAULT 'ACTIVE',
                                 version BIGINT DEFAULT 0
);

-- 8. TICKET_RESERVATION
CREATE TABLE ticket_reservation (
                                    id BIGSERIAL PRIMARY KEY,
                                    payment_transaction_id VARCHAR(50) NOT NULL,
                                    sale_id BIGINT NOT NULL,
                                    event_id BIGINT NOT NULL,
                                    ticket_id BIGINT NOT NULL,
                                    user_id BIGINT NOT NULL,
                                    quantity INT NOT NULL,
                                    expires_at TIMESTAMP NOT NULL,
                                    confirmed_at TIMESTAMP,
                                    confirmed_by VARCHAR(100),
                                    cancelled_at TIMESTAMP,
                                    cancelled_by VARCHAR(100),
                                    cancel_reason VARCHAR(255),
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(100),
                                    updated_at TIMESTAMP,
                                    updated_by VARCHAR(100),
                                    state VARCHAR(20) DEFAULT 'ACTIVE',
                                    version BIGINT DEFAULT 0
);

-- 9. USER_EVENT_RESERVATION_CONTROL
CREATE TABLE user_event_reservation_control (
                                                id BIGSERIAL PRIMARY KEY,
                                                user_id BIGINT NOT NULL,
                                                event_id BIGINT NOT NULL,
                                                total_attempts INT DEFAULT 0,
                                                active_reservations INT DEFAULT 0,
                                                completed_purchases INT DEFAULT 0,
                                                failed_attempts INT DEFAULT 0,
                                                is_blocked BOOLEAN DEFAULT FALSE,
                                                block_reason VARCHAR(255),
                                                blocked_at TIMESTAMP,
                                                first_attempt_at TIMESTAMP,
                                                last_attempt_at TIMESTAMP,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                created_by VARCHAR(100),
                                                updated_at TIMESTAMP,
                                                updated_by VARCHAR(100),
                                                state VARCHAR(20) DEFAULT 'ACTIVE',
                                                version BIGINT DEFAULT 0,
                                                CONSTRAINT uk_user_event UNIQUE (user_id, event_id)
);

-- 10. PAYMENT_CALLBACK_QUEUE (para MKASH)
CREATE TABLE payment_callback_queue (
                                        id BIGSERIAL PRIMARY KEY,
                                        payment_transaction_id VARCHAR(50),
                                        provider_code VARCHAR(30) NOT NULL,
                                        callback_type VARCHAR(30) NOT NULL,
                                        payload JSONB NOT NULL,
                                        headers JSONB,
                                        signature VARCHAR(255),
                                        status VARCHAR(20) DEFAULT 'PENDING',
                                        retry_count INT DEFAULT 0,
                                        error_message TEXT,
                                        received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        processed_at TIMESTAMP
);

-- 11. PAYMENT_AUDIT_LOG
CREATE TABLE payment_audit_log (
                                   id BIGSERIAL PRIMARY KEY,
                                   payment_transaction_id VARCHAR(50) NOT NULL,
                                   action VARCHAR(30) NOT NULL,
                                   old_status VARCHAR(30),
                                   new_status VARCHAR(30),
                                   details JSONB,
                                   ip_address VARCHAR(45),
                                   user_agent VARCHAR(255),
                                   performed_by VARCHAR(100),
                                   performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. ÍNDICES
CREATE INDEX idx_pt_transaction_id ON payment_transaction(transaction_id);
CREATE INDEX idx_pt_sale_id ON payment_transaction(sale_id);
CREATE INDEX idx_pt_status ON payment_transaction(status);
CREATE INDEX idx_pt_payment_method ON payment_transaction(payment_method_id);
CREATE INDEX idx_pt_created_at ON payment_transaction(created_at);

CREATE INDEX idx_ps_payment_transaction ON payment_success(payment_transaction_id);
CREATE INDEX idx_ps_provider_transaction ON payment_success(provider_transaction_id);

CREATE INDEX idx_pf_payment_transaction ON payment_failure(payment_transaction_id);
CREATE INDEX idx_pf_category ON payment_failure(failure_category);

CREATE INDEX idx_pp_payment_transaction ON payment_pending(payment_transaction_id);
CREATE INDEX idx_pp_expires_at ON payment_pending(expires_at);

CREATE INDEX idx_tr_payment_transaction ON ticket_reservation(payment_transaction_id);
CREATE INDEX idx_tr_sale_id ON ticket_reservation(sale_id);
CREATE INDEX idx_tr_user_event ON ticket_reservation(user_id, event_id);
CREATE INDEX idx_tr_state_expires ON ticket_reservation(state, expires_at);

CREATE INDEX idx_uerc_user_id ON user_event_reservation_control(user_id);
CREATE INDEX idx_uerc_event_id ON user_event_reservation_control(event_id);

CREATE INDEX idx_pcq_status ON payment_callback_queue(status);
CREATE INDEX idx_pcq_received_at ON payment_callback_queue(received_at);

CREATE INDEX idx_pal_payment_transaction ON payment_audit_log(payment_transaction_id);
CREATE INDEX idx_pal_performed_at ON payment_audit_log(performed_at);

-- 13. TRIGGERS
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_payment_transaction_updated_at
    BEFORE UPDATE ON payment_transaction FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ticket_reservation_updated_at
    BEFORE UPDATE ON ticket_reservation FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_event_reservation_control_updated_at
    BEFORE UPDATE ON user_event_reservation_control FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 14. DADOS INICIAIS
INSERT INTO payment_method (code, name, flow_type, display_order, requires_phone, requires_card, requires_qr_code, is_active) VALUES
                                                                                                                                  ('MPESA', 'M-Pesa', 'SYNCHRONOUS', 1, TRUE, FALSE, FALSE, TRUE),
                                                                                                                                  ('EMOLA', 'E-mola', 'SYNCHRONOUS', 2, TRUE, FALSE, FALSE, TRUE),
                                                                                                                                  ('CARD', 'Cartão de Crédito', 'SYNCHRONOUS', 3, FALSE, TRUE, FALSE, TRUE),
                                                                                                                                  ('MKASH', 'M-Kash', 'ASYNCHRONOUS', 4, TRUE, FALSE, TRUE, TRUE)
    ON CONFLICT (code) DO NOTHING;

-- 15. REATIVAR CONSTRAINTS
SET session_replication_role = DEFAULT;

-- 16. VERIFICAR
DO $$
DECLARE
method_count INT;
BEGIN
SELECT COUNT(*) INTO method_count FROM payment_method;
RAISE NOTICE '✅ Métodos cadastrados: %', method_count;
END $$;