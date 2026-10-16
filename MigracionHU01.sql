-- ================================================================
--  Migración HU-01 — estado de la tarjeta
--  Para bases examen2_soto creadas con la versión ANTERIOR del script,
--  donde tarjeta.estado era TINYINT(1) (1 = activa, 0 = bloqueada).
--
--  Crea el catálogo estado_tarjeta y convierte tarjeta.estado en una
--  FK obligatoria, conservando los datos:  1 → ACTIVA   0 → BLOQUEADA
--  Ejecutar UNA sola vez.
-- ================================================================

USE examen2_soto;

-- 1. Catálogo de estados
CREATE TABLE IF NOT EXISTS estado_tarjeta (
    codigo       VARCHAR(10)  NOT NULL  COMMENT 'Nombre de la constante del enum EstadoTarjeta',
    descripcion  VARCHAR(100) NOT NULL  COMMENT 'Significado del estado',
    PRIMARY KEY (codigo)
) ENGINE = InnoDB
  COMMENT = 'HU-01 Catálogo de estados de tarjeta';

INSERT INTO estado_tarjeta (codigo, descripcion) VALUES
    ('ACTIVA',    'La tarjeta puede ser utilizada'),
    ('BLOQUEADA', 'Uso suspendido temporalmente; puede reactivarse'),
    ('CANCELADA', 'Baja definitiva; no puede reactivarse');

-- 2. Columna temporal con el nuevo tipo y traducción de valores
ALTER TABLE tarjeta ADD COLUMN estado_nuevo VARCHAR(10) NULL AFTER estado;

UPDATE tarjeta
   SET estado_nuevo = CASE WHEN estado = 1 THEN 'ACTIVA' ELSE 'BLOQUEADA' END;

-- 3. Reemplazar la columna vieja (obligatoria y sin valor por defecto)
ALTER TABLE tarjeta DROP COLUMN estado;
ALTER TABLE tarjeta
    CHANGE COLUMN estado_nuevo estado VARCHAR(10) NOT NULL
        COMMENT '@Column heredado — HU-01 FK → estado_tarjeta.codigo';

-- 4. Llave foránea: solo se aceptan estados del catálogo
ALTER TABLE tarjeta
    ADD CONSTRAINT fk_tarjeta_estado
        FOREIGN KEY (estado) REFERENCES estado_tarjeta (codigo)
        ON UPDATE CASCADE ON DELETE RESTRICT;

-- 5. Ampliar fecha_exp: la aplicación guarda dd/MM/yyyy (10 caracteres)
ALTER TABLE tarjeta MODIFY fecha_exp VARCHAR(10) NOT NULL;
