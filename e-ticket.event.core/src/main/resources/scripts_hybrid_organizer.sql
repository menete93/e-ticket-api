-- =============================================
-- DATABASE: e_ticket_system
-- DESCRIÇÃO: Sistema de Gestão de Eventos e Bilhetes
-- =============================================

CREATE DATABASE IF NOT EXISTS e_ticket_system;
USE e_ticket_system;

-- =============================================
-- TABELAS DE CATEGORIAS E EVENTOS
-- =============================================


-- Criar tabela de organizers
-- Criar/atualizar tabela organizers (SEM is_active)
CREATE TABLE IF NOT EXISTS organizers (
                                          id BIGSERIAL PRIMARY KEY,
                                          name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    company_name VARCHAR(200),
    nuit VARCHAR(50) NOT NULL UNIQUE,
    reference_id VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    -- Estratégia híbrida
    commission_rate NUMERIC(5,4) DEFAULT 0.0500,
    flat_fee_per_ticket NUMERIC(10,2) DEFAULT 1.50,
    trial_events_remaining INTEGER DEFAULT 3,
    trial_used_count INTEGER DEFAULT 0,

    -- Estatísticas financeiras
    account_balance NUMERIC(15,2) DEFAULT 0.00,
    total_earnings NUMERIC(15,2) DEFAULT 0.00,
    total_commission_paid NUMERIC(15,2) DEFAULT 0.00,
    total_tickets_sold INTEGER DEFAULT 0,
    total_events_created INTEGER DEFAULT 0,

    -- ⚠️ NÃO PRECISA: is_active (vem do LifeCycleState da AuditableEntity)
    -- ⚠️ NÃO PRECISA: is_verified (usa-se LifeCycleState)

    -- Campos de auditoria (já na AuditableEntity)
    life_cycle_state VARCHAR(20) DEFAULT 'ACTIVE',
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
    );

-- Índices
ADD CONSTRAINT organizers_nuit_unique UNIQUE (nuit);
CREATE INDEX idx_organizers_email ON organizers(email);
CREATE INDEX idx_organizers_life_cycle ON organizers(life_cycle_state);

-- 2. Criar tabela de cupons de desconto
CREATE TABLE discount_coupons (
                                  id BIGSERIAL PRIMARY KEY,
                                  code VARCHAR(50) NOT NULL,
                                  event_id BIGINT NOT NULL,
                                  discount_type VARCHAR(20) NOT NULL,
                                  discount_value NUMERIC(10,2) NOT NULL,
                                  max_uses INTEGER,
                                  used_count INTEGER DEFAULT 0,
                                  valid_from TIMESTAMP,
                                  valid_until TIMESTAMP,
                                  is_active BOOLEAN DEFAULT true,
                                  description VARCHAR(200),
                                  min_purchase_amount NUMERIC(10,2),
                                  is_public BOOLEAN DEFAULT true,

    -- Campos de auditoria
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  created_by VARCHAR(100) NOT NULL,
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_by VARCHAR(100),
                                  life_cycle_state INT NOT NULL DEFAULT 1,
                                  version BIGINT DEFAULT 0



                                      FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                                  UNIQUE (code, event_id)
);

-- 3. Criar tabela de vendas
CREATE TABLE ticket_sales (
                              id BIGSERIAL PRIMARY KEY,
                              transaction_id VARCHAR(50) NOT NULL UNIQUE,
                              event_id BIGINT NOT NULL,
                              ticket_id BIGINT NOT NULL,
                              organizer_id BIGINT NOT NULL,
                              coupon_id BIGINT,
                              quantity INTEGER NOT NULL DEFAULT 1,
                              unit_price NUMERIC(10,2) NOT NULL,
                              subtotal NUMERIC(15,2) NOT NULL,
                              discount_amount NUMERIC(15,2) DEFAULT 0.00,
                              total_amount NUMERIC(15,2) NOT NULL,
                              commission_rate NUMERIC(5,4),
                              commission_amount NUMERIC(15,2) DEFAULT 0.00,
                              organizer_payout NUMERIC(15,2) NOT NULL,
                              buyer_email VARCHAR(100),
                              buyer_name VARCHAR(200),
                              buyer_phone VARCHAR(20),
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              payment_method VARCHAR(50),
                              payment_reference VARCHAR(100),
                              is_trial_event BOOLEAN DEFAULT false,

    -- Campos de auditoria
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              created_by VARCHAR(100) NOT NULL,
                              updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_by VARCHAR(100),
                              life_cycle_state INT NOT NULL DEFAULT 1,
                              version BIGINT DEFAULT 0,


                              FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
                              FOREIGN KEY (ticket_id) REFERENCES event_tickets(id) ON DELETE CASCADE,
                              FOREIGN KEY (organizer_id) REFERENCES organizers(id) ON DELETE CASCADE,
                              FOREIGN KEY (coupon_id) REFERENCES discount_coupons(id) ON DELETE SET NULL
);

-- Criar índices para performance
CREATE INDEX idx_ticket_sales_event_id ON ticket_sales(event_id);
CREATE INDEX idx_ticket_sales_organizer_id ON ticket_sales(organizer_id);
CREATE INDEX idx_ticket_sales_transaction_id ON ticket_sales(transaction_id);
CREATE INDEX idx_discount_coupons_event_id ON discount_coupons(event_id);
CREATE INDEX idx_discount_coupons_code ON discount_coupons(code);
CREATE INDEX idx_organizers_email ON organizers(email);

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
                                  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
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
                        organizer_id BIGINT NOT NULL,
                         event_commission_rate NUMERIC(5,4),
                          event_flat_fee NUMERIC(10,2),
                          is_trial_event BOOLEAN DEFAULT false,
                          total_commission NUMERIC(15,2) DEFAULT 0.00,
                          total_organizer_payout NUMERIC(15,2) DEFAULT 0.00,
                          total_sales NUMERIC(15,2) DEFAULT 0.00;
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
                        updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_by VARCHAR(100),
                        life_cycle_state INT NOT NULL DEFAULT 1,
                        version BIGINT DEFAULT 0,


                                                                                                         -- Chaves estrangeiras
                        FOREIGN KEY (category_id) REFERENCES event_categories(id),
                        FOREIGN KEY (organizer_id) REFERENCES organizers(id) ON DELETE CASCADE;

    -- Índices
                        INDEX idx_event_name (name),
                        INDEX idx_event_category (category_id),
                        INDEX idx_event_date (event_date),
                        INDEX idx_event_public (is_public),
                        INDEX idx_event_featured (is_featured),
                        INDEX idx_event_state (life_cycle_state),
                        INDEX idx_events_organizer_id ON events(organizer_id);
                        INDEX idx_event_location (geographic_location));

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
                                    auto_apply BOOLEAN NOT NULL DEFAULT FALSE,
                                    last_applied_at TIMESTAMP NULL,
                                    description VARCHAR(500),

    -- Auditoria
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    created_by VARCHAR(100) NOT NULL,
                                    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
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


CREATE TABLE strategy_bundle_tickets (
                                         strategy_id BIGINT NOT NULL,
                                         ticket_id BIGINT NOT NULL,
                                         PRIMARY KEY (strategy_id, ticket_id),
                                         FOREIGN KEY (strategy_id) REFERENCES pricing_strategies(id),
                                         FOREIGN KEY (ticket_id) REFERENCES event_tickets(id)
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
                                        execution_order INT NOT NULL DEFAULT 1,
                                        last_triggered_at TIMESTAMP NULL,
                                        description VARCHAR(500),

    -- Auditoria
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(100) NOT NULL,
                                        updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
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
                                         updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
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
    -- Auditoria
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      created_by VARCHAR(100) NOT NULL,
                                      updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
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
                                     updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
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
                              updated_at TIMESTAMP DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
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