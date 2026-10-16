-- ================================================================
--  Script MySQL — examen2_soto
--  Versión 2.0 — HU-01: estado de la tarjeta (Activa / Bloqueada / Cancelada)
--
--  Si ya tenía la base creada con la versión anterior, NO ejecute este
--  script: ejecute MigracionHU01.sql.
--
--  Estrategia ORM: JOINED TABLE  (InheritanceType.JOINED)
--  Una tabla por clase → sin repetición de columnas → normalizado.
--
--  Mapeo clase → tabla:
--  ┌────────────────────────────────────────────────────────────┐
--  │  Clase Java            │  Tabla MySQL       │  Relación   │
--  ├────────────────────────────────────────────────────────────┤
--  │  Titular               │  titular           │  —          │
--  │  Tarjeta.Estado (enum) │  estado_tarjeta    │  catálogo   │
--  │  Tarjeta  (abstracta)  │  tarjeta           │  —          │
--  │  Debito  extends Tarjeta│ tarjeta_debito    │  1:1 FK     │
--  │  Credito extends Tarjeta│ tarjeta_credito   │  1:1 FK     │
--  └────────────────────────────────────────────────────────────┘
--
--  Relaciones:
--    tarjeta.titular_id        → titular.id         (@ManyToOne  * → 1)
--    tarjeta.estado            → estado_tarjeta.codigo  (HU-01  * → 1)
--    tarjeta_debito.numero     → tarjeta.numero     (@OneToOne   1 → 1)
--    tarjeta_credito.numero    → tarjeta.numero     (@OneToOne   1 → 1)
-- ================================================================

CREATE DATABASE IF NOT EXISTS examen2_soto
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE examen2_soto;

-- ================================================================
-- TABLA 1 — titular
-- Clase: Titular
-- @Entity  @Table(name = "titular")
-- ================================================================
CREATE TABLE IF NOT EXISTS titular (

    -- @Id @Column(name = "id")
    id      VARCHAR(50)  NOT NULL  COMMENT '@Id',

    -- @Column(name = "nombre")
    nombre  VARCHAR(100) NOT NULL  COMMENT '@Column',

    PRIMARY KEY (id)

) ENGINE = InnoDB
  COMMENT = '@Entity Titular';

-- ================================================================
-- TABLA 1.1 — estado_tarjeta  (HU-01)
-- Enum Java anidado: Tarjeta.Estado { ACTIVA, BLOQUEADA, CANCELADA }
-- Catálogo de estados válidos. Se usa una tabla + FK (y no un ENUM)
-- porque MySQL asigna a un ENUM NOT NULL su primer valor cuando el
-- INSERT omite la columna; con la FK, una tarjeta sin estado o con
-- un estado inexistente es rechazada por la base de datos.
-- ================================================================
CREATE TABLE IF NOT EXISTS estado_tarjeta (

    codigo       VARCHAR(10)  NOT NULL  COMMENT 'Nombre de la constante del enum Tarjeta.Estado',
    descripcion  VARCHAR(100) NOT NULL  COMMENT 'Significado del estado',

    PRIMARY KEY (codigo)

) ENGINE = InnoDB
  COMMENT = 'HU-01 Catálogo de estados de tarjeta';

INSERT INTO estado_tarjeta (codigo, descripcion) VALUES
    ('ACTIVA',    'La tarjeta puede ser utilizada'),
    ('BLOQUEADA', 'Uso suspendido temporalmente; puede reactivarse'),
    ('CANCELADA', 'Baja definitiva; no puede reactivarse');

-- ================================================================
-- TABLA 2 — tarjeta
-- Clase: Tarjeta (abstracta — @MappedSuperclass con @Inheritance JOINED)
-- Almacena TODOS los campos comunes de la jerarquía (sin repetirlos).
-- @Entity  @Inheritance(strategy = InheritanceType.JOINED)
-- @Table(name = "tarjeta")
-- ================================================================
CREATE TABLE IF NOT EXISTS tarjeta (

    -- @Id @Column(name = "numero")
    numero      VARCHAR(20)  NOT NULL  COMMENT '@Id — PK compartida con subclases',

    -- @Column(name = "fecha_exp")
    fecha_exp   VARCHAR(10)  NOT NULL  COMMENT '@Column heredado — formato dd/MM/yyyy',

    -- HU-01 — @Enumerated(EnumType.STRING) @Column(name = "estado", nullable = false)
    -- Obligatorio: NOT NULL y SIN valor por defecto → el INSERT debe enviarlo.
    estado      VARCHAR(10)  NOT NULL  COMMENT '@Column heredado — HU-01 FK → estado_tarjeta.codigo',

    -- @ManyToOne  @JoinColumn(name = "titular_id")
    -- Cardinalidad: N tarjetas → 1 titular  (diagrama: * → 1)
    titular_id  VARCHAR(50)  NOT NULL  COMMENT '@ManyToOne FK → titular.id',

    PRIMARY KEY (numero),

    -- HU-01: no se permite guardar una tarjeta sin un estado válido
    CONSTRAINT fk_tarjeta_estado
        FOREIGN KEY (estado)
        REFERENCES  estado_tarjeta (codigo)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_tarjeta_titular
        FOREIGN KEY (titular_id)
        REFERENCES  titular (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE = InnoDB
  COMMENT = '@Entity Tarjeta — tabla base de la herencia JOINED';

-- ================================================================
-- TABLA 3 — tarjeta_debito
-- Clase: Debito extends Tarjeta
-- Solo almacena el campo PROPIO de la subclase (saldo).
-- La FK "numero" es PK y a la vez apunta a tarjeta.numero (1:1).
--
-- @Entity  @Table(name = "tarjeta_debito")
-- @PrimaryKeyJoinColumn(name = "numero")
-- ================================================================
CREATE TABLE IF NOT EXISTS tarjeta_debito (

    -- @PrimaryKeyJoinColumn(name = "numero")
    -- PK de esta tabla = FK @OneToOne → tarjeta.numero
    numero  VARCHAR(20) NOT NULL  COMMENT '@PrimaryKeyJoinColumn — FK 1:1 → tarjeta.numero',

    -- @Column(name = "saldo")  campo propio de Debito
    saldo   DOUBLE      NOT NULL
                DEFAULT 0.0       COMMENT '@Column propio Debito — saldo disponible',

    PRIMARY KEY (numero),

    CONSTRAINT fk_debito_tarjeta
        FOREIGN KEY (numero)
        REFERENCES  tarjeta (numero)
        ON UPDATE CASCADE
        ON DELETE CASCADE

) ENGINE = InnoDB
  COMMENT = '@Entity Debito extends Tarjeta — JOINED';

-- ================================================================
-- TABLA 4 — tarjeta_credito
-- Clase: Credito extends Tarjeta
-- Solo almacena el campo PROPIO de la subclase (limite).
-- La FK "numero" es PK y a la vez apunta a tarjeta.numero (1:1).
--
-- @Entity  @Table(name = "tarjeta_credito")
-- @PrimaryKeyJoinColumn(name = "numero")
-- ================================================================
CREATE TABLE IF NOT EXISTS tarjeta_credito (

    -- @PrimaryKeyJoinColumn(name = "numero")
    numero  VARCHAR(20) NOT NULL  COMMENT '@PrimaryKeyJoinColumn — FK 1:1 → tarjeta.numero',

    -- @Column(name = "limite")  campo propio de Credito
    limite  DOUBLE      NOT NULL
                DEFAULT 0.0       COMMENT '@Column propio Credito — límite de crédito',

    PRIMARY KEY (numero),

    CONSTRAINT fk_credito_tarjeta
        FOREIGN KEY (numero)
        REFERENCES  tarjeta (numero)
        ON UPDATE CASCADE
        ON DELETE CASCADE

) ENGINE = InnoDB
  COMMENT = '@Entity Credito extends Tarjeta — JOINED';

-- ================================================================
-- Datos de prueba (insertar respetando el orden de FKs)
-- ================================================================

INSERT INTO titular (id, nombre) VALUES
    ('T001', 'Juan Soto'),
    ('T002', 'María López');

INSERT INTO tarjeta (numero, fecha_exp, estado, titular_id) VALUES
    ('4111111111111111', '01/12/2024', 'ACTIVA',    'T001'),
    ('5500005555555559', '15/06/2023', 'ACTIVA',    'T001'),
    ('4000056655665556', '20/09/2022', 'BLOQUEADA', 'T002'),
    ('5105105105105100', '10/03/2024', 'CANCELADA', 'T002');

INSERT INTO tarjeta_debito (numero, saldo) VALUES
    ('4111111111111111', 1500000.00),
    ('4000056655665556',  800000.00);

INSERT INTO tarjeta_credito (numero, limite) VALUES
    ('5500005555555559', 5000000.00),
    ('5105105105105100', 2000000.00);
