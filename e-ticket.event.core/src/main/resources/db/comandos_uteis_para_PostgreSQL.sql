-- Verificar tamanho das tabelas
SELECT
    table_name,
    pg_size_pretty(pg_total_relation_size('"' || table_name || '"')) as total_size
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name LIKE 'payment_%'
ORDER BY pg_total_relation_size('"' || table_name || '"') DESC;

-- Verificar índices não utilizados
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE tablename LIKE 'payment_%'
ORDER BY idx_scan ASC;

-- Analisar tabelas para otimização do planner
VACUUM ANALYZE payment_transaction;
VACUUM ANALYZE payment_success;
VACUUM ANALYZE payment_failure;