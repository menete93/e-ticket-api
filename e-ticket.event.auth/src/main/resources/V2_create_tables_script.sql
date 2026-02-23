-- create_tables.sql
-- Execute: psql -U postgres -d eticket -f create_tables.sql

BEGIN;

-- 1. Tabela Base DomainEntity (se não existir)
CREATE TABLE IF NOT EXISTS domain_entity (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );

-- 2. Tabela Permissions
CREATE TABLE IF NOT EXISTS permissions (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
    );

-- 3. Tabela Roles
CREATE TABLE IF NOT EXISTS roles (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
    );

-- 4. Tabela de Junção Role_Permissions
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id UUID NOT NULL,
                                                permission_id UUID NOT NULL,
                                                created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
    );

-- 5. Tabela Users
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_organizer BOOLEAN DEFAULT FALSE,
    organizer_reference_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role_id UUID NOT NULL,
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

-- 6. Tabela User_Sessions
CREATE TABLE IF NOT EXISTS user_sessions (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    user_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    active BOOLEAN DEFAULT true,
    CONSTRAINT fk_user_sessions_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- 7. Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_permissions_name ON permissions(name);
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- 8. Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a todas as tabelas
CREATE TRIGGER update_permissions_updated_at
    BEFORE UPDATE ON permissions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_sessions_updated_at
    BEFORE UPDATE ON user_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMIT;

-- 9. Verificar criação das tabelas
SELECT '=== TABELAS CRIADAS ===' as info;
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;



//*******************novo script************************//


-- =============================
-- 1️⃣ Criar Permissões
-- =============================
INSERT INTO permission (name, description) VALUES
('USER_READ', 'Ler informações de usuário'),
('USER_WRITE', 'Criar/editar usuários'),
('USER_DELETE', 'Deletar usuários'),
('ROLE_READ', 'Ler funções'),
('ROLE_WRITE', 'Criar/editar funções'),
('EVENT_READ', 'Ler eventos'),
('EVENT_WRITE', 'Criar/editar eventos'),
('EVENT_DELETE', 'Deletar eventos'),
('TICKET_READ', 'Ler tickets'),
('TICKET_WRITE', 'Criar/editar tickets'),
('ADMIN_ACCESS', 'Acesso administrativo completo');

-- =============================
-- 2️⃣ Criar Roles
-- =============================
INSERT INTO role (name, description) VALUES
                                         ('ADMIN', 'Administrador do sistema'),
                                         ('USER', 'Usuário normal do sistema');

-- =============================
-- 3️⃣ Associar Permissões às Roles
-- =============================

-- Admin recebe todas as permissões
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.name = 'ADMIN';

-- Usuário normal recebe permissões limitadas
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role r
         JOIN permission p ON p.name IN ('USER_READ', 'EVENT_READ', 'TICKET_READ', 'TICKET_WRITE')
WHERE r.name = 'USER';

-- =============================
-- 4️⃣ Criar Usuários
-- =============================
-- Senhas aqui devem ser hash (BCrypt). Exemplo abaixo usa 'admin123' e 'password123' com hash gerado no Java.
INSERT INTO "user" (username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_by)
VALUES
    ('admin', 'admin@eticket.com', '$2a$10$EXEMPLOHASHADMIN', 'Administrador', 'do Sistema', true, true, true, true, 'system'),
    ('joao.silva', 'joao.silva@email.com', '$2a$10$EXEMPLOHASHSIM', 'João', 'Silva', true, true, true, true, 'system');

-- =============================
-- 5️⃣ Associar Roles aos Usuários
-- =============================
-- Admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM "user" u, role r
WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- Usuário normal
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM "user" u, role r
WHERE u.username = 'joao.silva' AND r.name = 'USER';

INSERT INTO e_ticket.user_roles (user_id, role_id) VALUES (1, 1);

//**************************//
INSERT INTO e_ticket.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM e_ticket.users u, e_ticket.roles r
WHERE u.username = 'joao.silva' AND r.name = 'USER';

SELECT * FROM e_ticket.users;

SELECT * FROM user_roles WHERE user_id = 1;

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);


SELECT * FROM role_permissions WHERE role_id = 1;

SELECT u.id, u.username, r.id as role_id, r.name as role_name,
       p.id as perm_id, p.name as perm_name
FROM e_ticket.users u
         LEFT JOIN user_roles ur ON u.id = ur.user_id
         LEFT JOIN e_ticket.roles r ON ur.role_id = r.id
         LEFT JOIN e_ticket.role_permissions rp ON r.id = rp.role_id
         LEFT JOIN e_ticket.permissions p ON rp.permission_id = p.id
WHERE u.username = 'admin';