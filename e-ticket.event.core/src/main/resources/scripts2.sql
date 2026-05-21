-- =============================================
-- DATABASE: e_ticket_system
-- DESCRIÇÃO: Sistema de Gestão de Eventos e Bilhetes
-- =============================================

CREATE DATABASE IF NOT EXISTS e_ticket_system;
USE e_ticket_system;

-- =============================================
-- TABELAS DE CATEGORIAS E EVENTOS
-- =============================================

-- Tabela de Categorias de Eventos
CREATE TABLE event_categories (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL UNIQUE,
                                  description VARCHAR(500),
                                  color_code VARCHAR(7),
                                  icon_url VARCHAR(500),

    -- Auditoria
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  created_by VARCHAR(100) NOT NULL,
                                  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                  updated_by VARCHAR(100),
                                  life_cycle_state INT NOT NULL DEFAULT 1,
                                  version BIGINT DEFAULT 0,

                                  INDEX idx_category_name (name),
                                  INDEX idx_category_state (life_cycle_state)
);

-- Tabela de Eventos
CREATE TABLE events (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(200) NOT NULL,
                        description VARCHAR(2000) NOT NULL,

    -- Localização geográfica (PostGIS)
                        geographic_location GEOGRAPHY(POINT, 4326),


    -- Categoria do evento
                        category_id BIGINT NOT NULL,

    -- Datas e horários
                        event_date TIMESTAMP NOT NULL,
                        start_time TIMESTAMP NULL,
                        end_time TIMESTAMP NULL,
                        registration_deadline TIMESTAMP NULL,

    -- Imagens
                        cover_image_url VARCHAR(500),
                        banner_image_url VARCHAR(500),

    -- Capacidade
                        max_attendees INT,
                        min_attendees INT,

    -- Flags
                        is_public BOOLEAN NOT NULL DEFAULT TRUE,
                        is_featured BOOLEAN NOT NULL DEFAULT FALSE,
                        is_free BOOLEAN NOT NULL DEFAULT FALSE,

    -- Estatísticas de bilhetes
                        total_tickets INT DEFAULT 0,
                        available_tickets INT DEFAULT 0,
                        sold_tickets INT DEFAULT 0,
                        reserved_tickets INT DEFAULT 0,

    -- Auditoria
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        created_by VARCHAR(100) NOT NULL,
                        updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                        updated_by VARCHAR(100),
                        life_cycle_state INT NOT NULL DEFAULT 1,
                        version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                        FOREIGN KEY (category_id) REFERENCES event_categories(id),

    -- Índices
                        INDEX idx_event_name (name),
                        INDEX idx_event_category (category_id),
                        INDEX idx_event_date (event_date),
                        INDEX idx_event_public (is_public),
                        INDEX idx_event_featured (is_featured),
                        INDEX idx_event_state (life_cycle_state),
                        SPATIAL INDEX idx_event_location (geographic_location)
);

-- =============================================
-- TABELAS DE BILHETES E PREÇOS
-- =============================================

-- Tabela de Estratégias de Preço
CREATE TABLE pricing_strategies (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    strategy_name VARCHAR(100) NOT NULL,
                                    strategy_type VARCHAR(50) NOT NULL,
                                    event_id BIGINT NOT NULL,

    -- Preços base e limites
                                    base_price DECIMAL(15,2),
                                    min_price DECIMAL(15,2),
                                    max_price DECIMAL(15,2),

    -- Multiplicadores e percentuais
                                    demand_multiplier DECIMAL(5,2),
                                    time_based_increase_days INT,
                                    time_based_increase_percentage DECIMAL(5,2),
                                    group_size_threshold INT,
                                    group_discount_percentage DECIMAL(5,2),
                                    demand_threshold_percentage DECIMAL(5,2),
                                    price_increase_percentage DECIMAL(5,2),

    -- Controles
                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                    apply_automatically BOOLEAN NOT NULL DEFAULT FALSE,
                                    last_applied_at TIMESTAMP NULL,
                                    description VARCHAR(500),

    -- Auditoria
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(100) NOT NULL,
                                    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                    updated_by VARCHAR(100),
                                    life_cycle_state INT NOT NULL DEFAULT 1,
                                    version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                                    FOREIGN KEY (event_id) REFERENCES events(id),

    -- Índices
                                    INDEX idx_pricing_strategy_event (event_id),
                                    INDEX idx_pricing_strategy_active (is_active),
                                    INDEX idx_pricing_strategy_auto (apply_automatically),
                                    INDEX idx_pricing_strategy_state (life_cycle_state)
);

-- Tabela de Bilhetes do Evento
CREATE TABLE event_tickets (
                               id BIGSERIAL PRIMARY KEY,
                               event_id BIGINT NOT NULL REFERENCES events(id),
                               category VARCHAR(50) NOT NULL CHECK (
                                   category IN ('NORMAL','VIP','VVIP','EARLY_BIRD','STUDENT','GROUP',
                                                'CORPORATE','INVITATION','BACKSTAGE','MEET_GREET','TABLE_BOOKING',
                                                'PREMIUM','NORMAL')
                                   ),
                               ticket_name VARCHAR(100) NOT NULL,
                               total_quantity INT NOT NULL,
                               available_quantity INT NOT NULL,
                               reserved_quantity INT NOT NULL DEFAULT 0,
                               sold_quantity INT NOT NULL DEFAULT 0,
                               current_price DECIMAL(15,2),
                               original_price DECIMAL(15,2),
                               description TEXT,
                               benefits TEXT,
                               sales_start_date TIMESTAMP,
                               sales_end_date TIMESTAMP,
                               max_tickets_per_user INT DEFAULT 10,
                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               has_dynamic_pricing BOOLEAN NOT NULL DEFAULT FALSE,
                               pricing_strategy_id BIGINT REFERENCES pricing_strategies(id),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               created_by VARCHAR(100) NOT NULL,
                               updated_at TIMESTAMP,
                               updated_by VARCHAR(100),
                               life_cycle_state INT NOT NULL DEFAULT 1,
                               version BIGINT DEFAULT 0,
                               UNIQUE (event_id, category)
);

-- Índices adicionais
CREATE INDEX idx_ticket_event ON event_tickets(event_id);
CREATE INDEX idx_ticket_category ON event_tickets(category);
CREATE INDEX idx_ticket_active ON event_tickets(is_active);
CREATE INDEX idx_ticket_sales_dates ON event_tickets(sales_start_date, sales_end_date);
CREATE INDEX idx_ticket_pricing_strategy ON event_tickets(pricing_strategy_id);
CREATE INDEX idx_ticket_state ON event_tickets(life_cycle_state);

-- =============================================
-- TABELAS DE GESTÃO DE PREÇOS DINÂMICOS
-- =============================================

-- Tabela de Regras de Ajuste de Preço
CREATE TABLE price_adjustment_rules (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        pricing_strategy_id BIGINT NOT NULL,
                                        adjustment_type VARCHAR(50) NOT NULL,
                                        adjustment_value DECIMAL(10,2),
                                        trigger_threshold INT,
                                        apply_to_category VARCHAR(50),
                                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                        execution_order INT NOT NULL DEFAULT 1,
                                        last_triggered_at TIMESTAMP NULL,
                                        description VARCHAR(500),

    -- Auditoria
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(100) NOT NULL,
                                        updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                        updated_by VARCHAR(100),
                                        life_cycle_state INT NOT NULL DEFAULT 1,
                                        version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                                        FOREIGN KEY (pricing_strategy_id) REFERENCES pricing_strategies(id),

    -- Índices
                                        INDEX idx_adjustment_rule_strategy (pricing_strategy_id),
                                        INDEX idx_adjustment_rule_active (is_active),
                                        INDEX idx_adjustment_rule_order (execution_order),
                                        INDEX idx_adjustment_rule_state (life_cycle_state)
);

-- Tabela de Mudanças de Preço Agendadas
CREATE TABLE scheduled_price_changes (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         pricing_strategy_id BIGINT NOT NULL,
                                         event_ticket_id BIGINT NULL,
                                         change_type VARCHAR(50) NOT NULL,
                                         change_value DECIMAL(10,2) NOT NULL,
                                         new_price DECIMAL(15,2),
                                         scheduled_at TIMESTAMP NOT NULL,
                                         executed_at TIMESTAMP NULL,
                                         is_executed BOOLEAN NOT NULL DEFAULT FALSE,
                                         execution_result VARCHAR(500),
                                         apply_to_all_tickets BOOLEAN NOT NULL DEFAULT FALSE,
                                         description VARCHAR(500),

    -- Auditoria
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         created_by VARCHAR(100) NOT NULL,
                                         updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                         updated_by VARCHAR(100),
                                         life_cycle_state INT NOT NULL DEFAULT 1,
                                         version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                                         FOREIGN KEY (pricing_strategy_id) REFERENCES pricing_strategies(id),
                                         FOREIGN KEY (event_ticket_id) REFERENCES event_tickets(id),

    -- Índices
                                         INDEX idx_scheduled_change_strategy (pricing_strategy_id),
                                         INDEX idx_scheduled_change_ticket (event_ticket_id),
                                         INDEX idx_scheduled_change_date (scheduled_at),
                                         INDEX idx_scheduled_change_executed (is_executed),
                                         INDEX idx_scheduled_change_state (life_cycle_state)
);

-- Tabela de Histórico de Preços
CREATE TABLE ticket_price_history (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      event_ticket_id BIGINT NOT NULL,
                                      old_price DECIMAL(15,2),
                                      new_price DECIMAL(15,2),
                                      change_reason VARCHAR(500),
                                      strategyId BIGINT,
--                                       change_type VARCHAR(50),  trocado por                                        strategyId BIGINT,
                                      changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Auditoria
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      created_by VARCHAR(100) NOT NULL,
                                      updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                      updated_by VARCHAR(100),
                                      life_cycle_state INT NOT NULL DEFAULT 1,
                                      version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                                      FOREIGN KEY (event_ticket_id) REFERENCES event_tickets(id),

    -- Índices
                                      INDEX idx_price_history_ticket (event_ticket_id),
                                      INDEX idx_price_history_date (changed_at),
                                      INDEX idx_price_history_state (life_cycle_state)
);

-- =============================================
-- TABELAS DE VENDAS E RESERVAS (OPCIONAIS)
-- =============================================

-- Tabela de Reservas
CREATE TABLE ticket_reservations (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     event_ticket_id BIGINT NOT NULL,
                                     reservation_token VARCHAR(100) NOT NULL UNIQUE,
                                     quantity INT NOT NULL,
                                     expires_at TIMESTAMP NOT NULL,
                                     customer_email VARCHAR(255),
                                     customer_name VARCHAR(255),
                                     status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Auditoria
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(100) NOT NULL,
                                     updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                     updated_by VARCHAR(100),
                                     life_cycle_state INT NOT NULL DEFAULT 1,
                                     version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                                     FOREIGN KEY (event_ticket_id) REFERENCES event_tickets(id),

    -- Índices
                                     INDEX idx_reservation_ticket (event_ticket_id),
                                     INDEX idx_reservation_token (reservation_token),
                                     INDEX idx_reservation_expires (expires_at),
                                     INDEX idx_reservation_status (status),
                                     INDEX idx_reservation_state (life_cycle_state)
);

-- Tabela de Vendas
CREATE TABLE ticket_sales (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              event_ticket_id BIGINT NOT NULL,
                              reservation_id BIGINT NULL,
                              quantity INT NOT NULL,
                              unit_price DECIMAL(15,2) NOT NULL,
                              total_amount DECIMAL(15,2) NOT NULL,
                              customer_email VARCHAR(255) NOT NULL,
                              customer_name VARCHAR(255) NOT NULL,
                              payment_reference VARCHAR(100),
                              sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',

    -- Auditoria
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              created_by VARCHAR(100) NOT NULL,
                              updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                              updated_by VARCHAR(100),
                              life_cycle_state INT NOT NULL DEFAULT 1,
                              version BIGINT DEFAULT 0,

    -- Chaves estrangeiras
                              FOREIGN KEY (event_ticket_id) REFERENCES event_tickets(id),
                              FOREIGN KEY (reservation_id) REFERENCES ticket_reservations(id),

    -- Índices
                              INDEX idx_sale_ticket (event_ticket_id),
                              INDEX idx_sale_customer (customer_email),
                              INDEX idx_sale_date (sale_date),
                              INDEX idx_sale_status (status),
                              INDEX idx_sale_state (life_cycle_state)
);

-- =============================================
-- INSERTS INICIAIS (DADOS DE EXEMPLO)
-- =============================================

-- Inserir categorias padrão
INSERT INTO event_categories (name, description, color_code, created_by, life_cycle_state) VALUES
                                                                                               ('MUSICAL', 'Eventos relacionados a música', '#FF6B6B', 'system', 1),
                                                                                               ('ESPORTIVO', 'Eventos esportivos', '#4ECDC4', 'system', 1),
                                                                                               ('CULTURAL', 'Eventos culturais e artísticos', '#45B7D1', 'system', 1),
                                                                                               ('EDUCACIONAL', 'Eventos educacionais e palestras', '#96CEB4', 'system', 1),
                                                                                               ('CORPORATIVO', 'Eventos corporativos e empresariais', '#FFEAA7', 'system', 1),
                                                                                               ('GASTRONOMICO', 'Eventos gastronômicos', '#DDA0DD', 'system', 1),
                                                                                               ('TECNOLOGIA', 'Eventos de tecnologia e inovação', '#98D8C8', 'system', 1);

-- =============================================
-- VIEWS ÚTEIS
-- =============================================

-- View para eventos ativos com informações de bilhetes
CREATE VIEW active_events_with_tickets AS
SELECT
    e.id,
    e.name,
    e.description,
    e.event_date,
    e.total_tickets,
    e.available_tickets,
    e.sold_tickets,
    ec.name as category_name,
    COUNT(et.id) as ticket_types_count
FROM events e
         JOIN event_categories ec ON e.category_id = ec.id
         LEFT JOIN event_tickets et ON e.id = et.event_id AND et.is_active = TRUE
WHERE e.life_cycle_state = 1 AND e.is_public = TRUE
GROUP BY e.id, e.name, e.description, e.event_date, e.total_tickets, e.available_tickets, e.sold_tickets, ec.name;

-- View para bilhetes disponíveis
CREATE VIEW available_tickets_view AS
SELECT
    et.*,
    e.name as event_name,
    e.event_date,
    ec.name as category_name
FROM event_tickets et
         JOIN events e ON et.event_id = e.id
         JOIN event_categories ec ON e.category_id = ec.id
WHERE et.is_active = TRUE
  AND et.available_quantity > 0
  AND (et.sales_start_date IS NULL OR et.sales_start_date <= NOW())
  AND (et.sales_end_date IS NULL OR et.sales_end_date >= NOW())
  AND e.life_cycle_state = 1
  AND e.is_public = TRUE;

-- =============================================
-- STORED PROCEDURES
-- =============================================

-- Procedure para atualizar estatísticas de eventos
DELIMITER //
CREATE PROCEDURE UpdateEventTicketStatistics(IN event_id BIGINT)
BEGIN
UPDATE events e
SET
    total_tickets = (SELECT COALESCE(SUM(total_quantity), 0) FROM event_tickets WHERE event_id = e.id AND is_active = TRUE),
    available_tickets = (SELECT COALESCE(SUM(available_quantity), 0) FROM event_tickets WHERE event_id = e.id AND is_active = TRUE),
    sold_tickets = (SELECT COALESCE(SUM(sold_quantity), 0) FROM event_tickets WHERE event_id = e.id AND is_active = TRUE),
    reserved_tickets = (SELECT COALESCE(SUM(reserved_quantity), 0) FROM event_tickets WHERE event_id = e.id AND is_active = TRUE)
WHERE e.id = event_id;
END //
DELIMITER ;

-- =============================================
-- TRIGGERS
-- =============================================

-- Trigger para atualizar estatísticas quando bilhetes são modificados
DELIMITER //
CREATE TRIGGER after_event_ticket_update
    AFTER UPDATE ON event_tickets
    FOR EACH ROW
BEGIN
    CALL UpdateEventTicketStatistics(NEW.event_id);
END //
DELIMITER ;

-- Trigger para atualizar estatísticas quando bilhetes são inseridos
DELIMITER //
CREATE TRIGGER after_event_ticket_insert
    AFTER INSERT ON event_tickets
    FOR EACH ROW
BEGIN
    CALL UpdateEventTicketStatistics(NEW.event_id);
END //
DELIMITER ;

-- =============================================
-- COMENTÁRIOS DAS TABELAS
-- =============================================

ALTER TABLE event_categories COMMENT = 'Tabela de categorias de eventos';
ALTER TABLE events COMMENT = 'Tabela principal de eventos';
ALTER TABLE event_tickets COMMENT = 'Tabela de bilhetes para eventos';
ALTER TABLE pricing_strategies COMMENT = 'Estratégias de precificação dinâmica';
ALTER TABLE price_adjustment_rules COMMENT = 'Regras de ajuste de preço';
ALTER TABLE scheduled_price_changes COMMENT = 'Mudanças de preço agendadas';
ALTER TABLE ticket_price_history COMMENT = 'Histórico de alterações de preço';
ALTER TABLE ticket_reservations COMMENT = 'Reservas de bilhetes';
ALTER TABLE ticket_sales COMMENT = 'Vendas de bilhetes confirmadas';




---------------------------------CONSTRAINT PARA VERIFICAR STATE------------------

DO $$
DECLARE r RECORD;
BEGIN
FOR r IN
SELECT table_name
FROM information_schema.columns
WHERE column_name = 'state'
  AND table_schema = 'e_ticket'
    LOOP
        -- remove se existir constraint antiga (evita erro)
        EXECUTE format(
            'ALTER TABLE e_ticket.%I DROP CONSTRAINT IF EXISTS chk_%I_state',
            r.table_name,
            r.table_name
        );

-- recria constraint nova
EXECUTE format(
        'ALTER TABLE e_ticket.%I
         ADD CONSTRAINT chk_%I_state
         CHECK (state IN (''ACTIVE'',''INACTIVE'',''DELETED'',''BLOCKED'',''BANNED''))',
        r.table_name,
        r.table_name
        );
END LOOP;
END $$;




-------MPESA----------------




-- ==================== SEQUÊNCIAS ====================
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_PROVIDER_CONFIG_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_METHOD_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_TRANSACTION_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_ATTEMPT_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_REFUND_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.PAYMENT_WEBHOOK_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS e_ticket.USER_PAYMENT_METHOD_SEQ START WITH 1 INCREMENT BY 1;

-- ==================== TABELA DE PROVEDORES ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_PROVIDER_CONFIG (
                                                                ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_PROVIDER_CONFIG_SEQ'),
    CODE VARCHAR(30) NOT NULL UNIQUE,
    NAME VARCHAR(100) NOT NULL,
    DESCRIPTION VARCHAR(255),
    BASE_URL VARCHAR(255),
    API_KEY VARCHAR(255),
    API_SECRET TEXT,
    PARTNER_CODE VARCHAR(50),
    CALLBACK_URL VARCHAR(255),
    TIMEOUT_URL VARCHAR(255),
    PRIORITY INT DEFAULT 0,
    MIN_AMOUNT DECIMAL(10,2),
    MAX_AMOUNT DECIMAL(10,2),
    FEE_PERCENTAGE DECIMAL(5,2) DEFAULT 0,
    FEE_FIXED DECIMAL(10,2) DEFAULT 0,
    SETTLEMENT_DAYS INT DEFAULT 0,
    IS_INSTANT BOOLEAN DEFAULT FALSE,
    SMSCONTENT TEXT,
    EXTRA_CONFIG JSON,

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT UQ_PAYMENT_PROVIDER_CONFIG_01 UNIQUE (CODE),
    CONSTRAINT UQ_PAYMENT_PROVIDER_CONFIG_02 UNIQUE (PARTNER_CODE)
    );

-- ==================== TABELA DE MÉTODOS DE PAGAMENTO ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_METHOD (
                                                       ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_METHOD_SEQ'),
    CODE VARCHAR(30) NOT NULL UNIQUE,
    NAME VARCHAR(100) NOT NULL,
    ICON_URL VARCHAR(255),
    DISPLAY_ORDER INT DEFAULT 0,
    PROVIDER_ID BIGINT,
    REQUIRES_PHONE BOOLEAN DEFAULT FALSE,
    REQUIRES_CARD BOOLEAN DEFAULT FALSE,
    REQUIRES_QR_CODE BOOLEAN DEFAULT FALSE,
    REQUIRES_DOCUMENT BOOLEAN DEFAULT FALSE,
    MIN_AMOUNT DECIMAL(10,2),
    MAX_AMOUNT DECIMAL(10,2),
    DISPLAY_CONFIG JSON,

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT UQ_PAYMENT_METHOD_01 UNIQUE (CODE),

    CONSTRAINT FK_PAYMENT_METHOD_PROVIDER
    FOREIGN KEY (PROVIDER_ID)
    REFERENCES e_ticket.PAYMENT_PROVIDER_CONFIG(ID)
    );

-- ==================== TABELA DE TRANSAÇÕES DE PAGAMENTO ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_TRANSACTION (
                                                            ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_TRANSACTION_SEQ'),
    TRANSACTION_ID VARCHAR(50) NOT NULL UNIQUE,
    EXTERNAL_ID VARCHAR(100),
    SALE_ID BIGINT NOT NULL,
    EVENT_ID BIGINT NOT NULL,
    USER_ID BIGINT,
    ORDER_ID VARCHAR(50),
    METHOD_ID BIGINT,
    PROVIDER_ID BIGINT,

    AMOUNT DECIMAL(10,2) NOT NULL,
    FEE_AMOUNT DECIMAL(10,2) DEFAULT 0,
    DISCOUNT_AMOUNT DECIMAL(10,2) DEFAULT 0,
    NET_AMOUNT DECIMAL(10,2),

    CURRENCY VARCHAR(3) DEFAULT 'MZN',

    PAYER_NAME VARCHAR(100),
    PAYER_EMAIL VARCHAR(100),
    PAYER_PHONE VARCHAR(20),
    PAYER_DOCUMENT VARCHAR(50),

    STATUS VARCHAR(30) NOT NULL,
    STATUS_CODE INT,
    STATUS_MESSAGE VARCHAR(255),

    PROVIDER_CHECKOUT_ID VARCHAR(100),
    PROVIDER_MERCHANT_ID VARCHAR(100),
    PROVIDER_TRANSACTION_ID VARCHAR(100),
    PROVIDER_RESULT_CODE INT,
    PROVIDER_RESULT_DESC VARCHAR(255),

    REQUEST_PAYLOAD JSON,
    RESPONSE_PAYLOAD JSON,
    CALLBACK_PAYLOAD JSON,

    RETRY_COUNT INT DEFAULT 0,
    MAX_RETRIES INT DEFAULT 3,

    PROCESSED_AT TIMESTAMP,
    COMPLETED_AT TIMESTAMP,
    EXPIRES_AT TIMESTAMP,

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT FK_PAYMENT_TRANSACTION_METHOD
    FOREIGN KEY (METHOD_ID)
    REFERENCES e_ticket.PAYMENT_METHOD(ID),

    CONSTRAINT FK_PAYMENT_TRANSACTION_PROVIDER
    FOREIGN KEY (PROVIDER_ID)
    REFERENCES e_ticket.PAYMENT_PROVIDER_CONFIG(ID)
    );

-- ==================== TABELA DE TENTATIVAS DE PAGAMENTO ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_ATTEMPT (
                                                        ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_ATTEMPT_SEQ'),
    TRANSACTION_ID VARCHAR(50) NOT NULL,
    ATTEMPT_NUMBER INT NOT NULL,
    STATUS VARCHAR(30),

    REQUEST_PAYLOAD JSON,
    RESPONSE_PAYLOAD JSON,

    ERROR_MESSAGE TEXT,
    DURATION_MS BIGINT,

    PROCESSED_AT TIMESTAMP,

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT FK_PAYMENT_ATTEMPT_TRANSACTION
    FOREIGN KEY (TRANSACTION_ID)
    REFERENCES e_ticket.PAYMENT_TRANSACTION(TRANSACTION_ID)
    );

-- ==================== TABELA DE REEMBOLSOS ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_REFUND (
                                                       ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_REFUND_SEQ'),
    TRANSACTION_ID VARCHAR(50) NOT NULL,
    REFUND_TRANSACTION_ID VARCHAR(50) UNIQUE,

    AMOUNT DECIMAL(10,2) NOT NULL,
    REASON VARCHAR(255),
    STATUS VARCHAR(30),

    REQUESTED_BY VARCHAR(100),
    APPROVED_BY VARCHAR(100),

    APPROVED_AT TIMESTAMP,
    COMPLETED_AT TIMESTAMP,

    EXTERNAL_REFUND_ID VARCHAR(100),

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT FK_PAYMENT_REFUND_TRANSACTION
    FOREIGN KEY (TRANSACTION_ID)
    REFERENCES e_ticket.PAYMENT_TRANSACTION(TRANSACTION_ID)
    );

-- ==================== TABELA DE WEBHOOKS ====================
CREATE TABLE IF NOT EXISTS e_ticket.PAYMENT_WEBHOOK (
                                                        ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.PAYMENT_WEBHOOK_SEQ'),
    WEBHOOK_ID VARCHAR(50) UNIQUE,
    PROVIDER_CODE VARCHAR(30),

    PAYLOAD JSON,
    HEADERS JSON,

    SIGNATURE VARCHAR(255),
    STATUS VARCHAR(20),

    PROCESSED_AT TIMESTAMP,
    ERROR_MESSAGE TEXT,

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0
    );

-- ==================== TABELA DE MÉTODOS DE PAGAMENTO DO USUÁRIO ====================
CREATE TABLE IF NOT EXISTS e_ticket.USER_PAYMENT_METHOD (
                                                            ID BIGINT PRIMARY KEY DEFAULT nextval('e_ticket.USER_PAYMENT_METHOD_SEQ'),

    USER_ID BIGINT NOT NULL,
    METHOD_ID BIGINT NOT NULL,

    TOKEN VARCHAR(255),
    LAST_FOUR VARCHAR(4),
    CARD_BRAND VARCHAR(20),

    EXPIRY_MONTH INT,
    EXPIRY_YEAR INT,

    IS_DEFAULT BOOLEAN DEFAULT FALSE,

    PAYER_NAME VARCHAR(100),
    PAYER_EMAIL VARCHAR(100),
    PAYER_PHONE VARCHAR(20),

    -- Campos herdados de AuditableEntity
    STATE VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT TIMESTAMP,
    UPDATED_BY VARCHAR(100),
    VERSION BIGINT DEFAULT 0,

    CONSTRAINT FK_USER_PAYMENT_METHOD
    FOREIGN KEY (METHOD_ID)
    REFERENCES e_ticket.PAYMENT_METHOD(ID)
    );

-- ==================== ÍNDICES ====================

-- PAYMENT_TRANSACTION
CREATE INDEX IF NOT EXISTS IDX_PAYMENT_TRANSACTION_01
    ON e_ticket.PAYMENT_TRANSACTION(TRANSACTION_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_TRANSACTION_02
    ON e_ticket.PAYMENT_TRANSACTION(SALE_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_TRANSACTION_03
    ON e_ticket.PAYMENT_TRANSACTION(STATUS);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_TRANSACTION_04
    ON e_ticket.PAYMENT_TRANSACTION(PROVIDER_CHECKOUT_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_TRANSACTION_05
    ON e_ticket.PAYMENT_TRANSACTION(CREATED_AT);

-- PAYMENT_ATTEMPT
CREATE INDEX IF NOT EXISTS IDX_PAYMENT_ATTEMPT_01
    ON e_ticket.PAYMENT_ATTEMPT(TRANSACTION_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_ATTEMPT_02
    ON e_ticket.PAYMENT_ATTEMPT(CREATED_AT);

-- PAYMENT_REFUND
CREATE INDEX IF NOT EXISTS IDX_PAYMENT_REFUND_01
    ON e_ticket.PAYMENT_REFUND(TRANSACTION_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_REFUND_02
    ON e_ticket.PAYMENT_REFUND(REFUND_TRANSACTION_ID);

-- PAYMENT_WEBHOOK
CREATE INDEX IF NOT EXISTS IDX_PAYMENT_WEBHOOK_01
    ON e_ticket.PAYMENT_WEBHOOK(WEBHOOK_ID);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_WEBHOOK_02
    ON e_ticket.PAYMENT_WEBHOOK(PROVIDER_CODE);

CREATE INDEX IF NOT EXISTS IDX_PAYMENT_WEBHOOK_03
    ON e_ticket.PAYMENT_WEBHOOK(STATUS);

-- USER_PAYMENT_METHOD
CREATE INDEX IF NOT EXISTS IDX_USER_PAYMENT_METHOD_01
    ON e_ticket.USER_PAYMENT_METHOD(USER_ID);

CREATE INDEX IF NOT EXISTS IDX_USER_PAYMENT_METHOD_02
    ON e_ticket.USER_PAYMENT_METHOD(TOKEN);

-- ==================== DADOS INICIAIS ====================

-- Inserir provedores
INSERT INTO e_ticket.PAYMENT_PROVIDER_CONFIG (
    CODE,
    NAME,
    PRIORITY,
    STATE,
    CREATED_BY,
    CREATED_AT
)
VALUES
    (
        'MPESA',
        'M-Pesa',
        1,
        'ACTIVE',
        'SYSTEM',
        CURRENT_TIMESTAMP
    ),
    (
        'EMOLA',
        'E-Mola',
        2,
        'ACTIVE',
        'SYSTEM',
        CURRENT_TIMESTAMP
    ),
    (
        'VISA',
        'Visa/Mastercard',
        3,
        'ACTIVE',
        'SYSTEM',
        CURRENT_TIMESTAMP
    );

-- Inserir métodos de pagamento
INSERT INTO e_ticket.PAYMENT_METHOD (
    CODE,
    NAME,
    PROVIDER_ID,
    REQUIRES_PHONE,
    STATE,
    CREATED_BY,
    CREATED_AT
)
SELECT
    'MPESA',
    'M-Pesa',
    ID,
    TRUE,
    'ACTIVE',
    'SYSTEM',
    CURRENT_TIMESTAMP
FROM e_ticket.PAYMENT_PROVIDER_CONFIG
WHERE CODE = 'MPESA';

INSERT INTO e_ticket.PAYMENT_METHOD (
    CODE,
    NAME,
    PROVIDER_ID,
    REQUIRES_PHONE,
    STATE,
    CREATED_BY,
    CREATED_AT
)
SELECT
    'EMOLA',
    'E-Mola',
    ID,
    TRUE,
    'ACTIVE',
    'SYSTEM',
    CURRENT_TIMESTAMP
FROM e_ticket.PAYMENT_PROVIDER_CONFIG
WHERE CODE = 'EMOLA';

INSERT INTO e_ticket.PAYMENT_METHOD (
    CODE,
    NAME,
    PROVIDER_ID,
    REQUIRES_CARD,
    STATE,
    CREATED_BY,
    CREATED_AT
)
SELECT
    'CARD',
    'Cartão de Crédito/Débito',
    ID,
    TRUE,
    'ACTIVE',
    'SYSTEM',
    CURRENT_TIMESTAMP
FROM e_ticket.PAYMENT_PROVIDER_CONFIG
WHERE CODE = 'VISA';

-- ==================== COMMIT ====================
COMMIT;