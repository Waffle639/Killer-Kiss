-- Script para arreglar las foreign keys con CASCADE
USE killerkiss;

SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar la constraint que tiene NO ACTION (la que está mal)
ALTER TABLE partida_participantes DROP FOREIGN KEY FK47fptp1e0arbilnct2pr2rp69;

SET FOREIGN_KEY_CHECKS = 1;

-- Verificar que ahora todo esté correcto
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    DELETE_RULE
FROM 
    information_schema.REFERENTIAL_CONSTRAINTS
WHERE 
    CONSTRAINT_SCHEMA = 'killerkiss'
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
