-- =====================================================
-- MIGRAÇÃO: Índices otimizados para performance
-- Versão: 1.2
-- Banco: PostgreSQL
-- =====================================================

-- Índices compostos para queries comuns
CREATE INDEX IF NOT EXISTS idx_payment_transaction_status_method
    ON payment_transaction(status, payment_method_code, created_at);

CREATE INDEX IF NOT EXISTS idx_reservation_user_event_status
    ON ticket_reservation(user_id, event_id, status, expires_at);

CREATE INDEX IF NOT EXISTS idx_payment_success_settlement
    ON payment_success(settlement_status, payment_confirmed_at);

CREATE INDEX IF NOT EXISTS idx_payment_failure_resolved
    ON payment_failure(resolved, failed_at);

-- Índices parciais (exclusivos do PostgreSQL - muito eficientes)
-- Apenas para reservas ativas
CREATE INDEX IF NOT EXISTS idx_reservation_active_expires
    ON ticket_reservation(expires_at)
    WHERE status = 'ACTIVE';

-- Apenas para transações pendentes/processando
CREATE INDEX IF NOT EXISTS idx_transaction_pending_created
    ON payment_transaction(created_at)
    WHERE status IN ('PENDING', 'PROCESSING', 'AWAITING_CONFIRMATION');

-- Apenas para callbacks pendentes
CREATE INDEX IF NOT EXISTS idx_callback_pending_retry
    ON payment_callback_queue(retry_count, received_at)
    WHERE status = 'PENDING';

-- Índice para buscas JSONB (GIN index para consultas eficientes em JSON)
CREATE INDEX IF NOT EXISTS idx_payment_success_response_payload
    ON payment_success USING GIN (response_payload);

CREATE INDEX IF NOT EXISTS idx_payment_failure_response_payload
    ON payment_failure USING GIN (response_payload);

-- Índice para busca por texto (usando pg_trgm)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_payment_transaction_message_trgm
    ON payment_transaction USING GIN (status_message gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_payment_failure_message_trgm
    ON payment_failure USING GIN (failure_message gin_trgm_ops);

-- Índice para ordenação por data
CREATE INDEX IF NOT EXISTS idx_payment_transaction_completed
    ON payment_transaction(completed_at)
    WHERE status = 'SUCCESS';

CREATE INDEX IF NOT EXISTS idx_payment_transaction_failed
    ON payment_transaction(processed_at)
    WHERE status = 'FAILED';

-- Estatísticas para otimização do planner
ANALYZE payment_transaction;
ANALYZE payment_success;
ANALYZE payment_failure;
ANALYZE payment_pending;
ANALYZE ticket_reservation;
ANALYZE user_event_reservation_control;