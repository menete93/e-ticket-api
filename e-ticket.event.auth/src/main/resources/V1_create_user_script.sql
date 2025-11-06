-- script_initial_data.sql
-- Execute: psql -U postgres -d eticket -f script_initial_data.sql

BEGIN;

-- 1. Inserir Permissões Básicas
INSERT INTO permissions (id, name, description, created_at, updated_at) VALUES
(gen_random_uuid(), 'USER_READ', 'Ler informações de usuário', NOW(), NOW()),
(gen_random_uuid(), 'USER_WRITE', 'Criar/editar usuários', NOW(), NOW()),
(gen_random_uuid(), 'USER_DELETE', 'Deletar usuários', NOW(), NOW()),
(gen_random_uuid(), 'ROLE_READ', 'Ler funções', NOW(), NOW()),
(gen_random_uuid(), 'ROLE_WRITE', 'Criar/editar funções', NOW(), NOW()),
(gen_random_uuid(), 'EVENT_READ', 'Ler eventos', NOW(), NOW()),
(gen_random_uuid(), 'EVENT_WRITE', 'Criar/editar eventos', NOW(), NOW()),
(gen_random_uuid(), 'EVENT_DELETE', 'Deletar eventos', NOW(), NOW()),
(gen_random_uuid(), 'TICKET_READ', 'Ler tickets', NOW(), NOW()),
(gen_random_uuid(), 'TICKET_WRITE', 'Criar/editar tickets', NOW(), NOW()),
(gen_random_uuid(), 'ADMIN_ACCESS', 'Acesso administrativo completo', NOW(), NOW())
    ON CONFLICT (name) DO NOTHING;

-- 2. Criar Role de Administrador
WITH admin_role AS (
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'ADMIN', 'Administrador do sistema', NOW(), NOW())
ON CONFLICT (name) DO NOTHING
    RETURNING id
    )
-- 3. Associar todas as permissões ao role ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    admin_role.id,
    p.id
FROM admin_role, permissions p
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = admin_role.id AND rp.permission_id = p.id
);

-- 4. Criar Role de Usuário Normal
WITH user_role AS (
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'USER', 'Usuário normal do sistema', NOW(), NOW())
ON CONFLICT (name) DO NOTHING
    RETURNING id
    )
-- 5. Associar permissões básicas ao role USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    user_role.id,
    p.id
FROM user_role, permissions p
WHERE p.name IN ('USER_READ', 'EVENT_READ', 'TICKET_READ', 'TICKET_WRITE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = user_role.id AND rp.permission_id = p.id
);

-- 6. Criar Usuário Administrador
-- Senha: "admin123" encrypted with BCrypt
INSERT INTO users (
    id,
    username,
    email,
    password,
    first_name,
    last_name,
    role_id,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'admin',
    'admin@eticket.com',
    '$2a$12$8r3Fq3p2p5s6N7d8V9g0Hu1W2x3y4z5A6B7C8D9E0F1G2H3I4J5K6L', -- admin123
    'Administrador',
    'do Sistema',
    r.id,
    true,
    true,
    true,
    true,
    NOW(),
    NOW()
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM users u WHERE u.username = 'admin' OR u.email = 'admin@eticket.com'
);

-- 7. Criar Usuário Normal de Exemplo
INSERT INTO users (
    id,
    username,
    email,
    password,
    first_name,
    last_name,
    role_id,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'joao.silva',
    'joao.silva@email.com',
    '$2a$12$1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z7', -- password123
    'João',
    'Silva',
    r.id,
    true,
    true,
    true,
    true,
    NOW(),
    NOW()
FROM roles r
WHERE r.name = 'USER'
  AND NOT EXISTS (
    SELECT 1 FROM users u WHERE u.username = 'joao.silva' OR u.email = 'joao.silva@email.com'
);

COMMIT;

-- 8. Verificar dados inseridos
SELECT '=== PERMISSIONS ===' as info;
SELECT name, description FROM permissions;

SELECT '=== ROLES ===' as info;
SELECT r.name, COUNT(rp.permission_id) as permission_count
FROM roles r
         LEFT JOIN role_permissions rp ON r.id = rp.role_id
GROUP BY r.name;

SELECT '=== USERS ===' as info;
SELECT u.username, u.email, u.first_name, u.last_name, r.name as role
FROM users u JOIN roles r ON u.role_id = r.id;