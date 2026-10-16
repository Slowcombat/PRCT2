-- ================================================================
--  Script MySQL — examen2_soto
--
--  Estrategia ORM: JOINED TABLE  (InheritanceType.JOINED)
--  Una tabla por clase → sin repetición de columnas → normalizado.
--
--  Mapeo clase → tabla:
--  ┌────────────────────────────────────────────────────────────┐
--  │  Clase Java            │  Tabla MySQL       │  Relación   │
--  ├────────────────────────────────────────────────────────────┤
--  │  Titular               │  titular           │  —          │
--  │  Tarjeta  (abstracta)  │  tarjeta           │  —          │
--  │  Debito  extends Tarjeta│ tarjeta_debito    │  1:1 FK     │
--  │  Credito extends Tarjeta│ tarjeta_credito   │  1:1 FK     │
--  └────────────────────────────────────────────────────────────┘
--
--  Relaciones:
--    tarjeta.titular_id        → titular.id         (@ManyToOne  * → 1)
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
    fecha_exp   VARCHAR(7)   NOT NULL  COMMENT '@Column heredado — formato MM/AAAA',

    -- @Column(name = "estado")  true=activa | false=bloqueada
    estado      TINYINT(1)   NOT NULL
                    DEFAULT 1          COMMENT '@Column heredado — 1 activa / 0 bloqueada',

    -- @ManyToOne  @JoinColumn(name = "titular_id")
    -- Cardinalidad: N tarjetas → 1 titular  (diagrama: * → 1)
    titular_id  VARCHAR(50)  NOT NULL  COMMENT '@ManyToOne FK → titular.id',

    PRIMARY KEY (numero),

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
    ('4111111111111111', '12/2027', 1, 'T001'),
    ('5500005555555559', '06/2026', 1, 'T001'),
    ('4000056655665556', '09/2025', 0, 'T002'),
    ('5105105105105100', '03/2027', 1, 'T002');

INSERT INTO tarjeta_debito (numero, saldo) VALUES
    ('4111111111111111', 1500000.00),
    ('4000056655665556',  800000.00);

INSERT INTO tarjeta_credito (numero, limite) VALUES
    ('5500005555555559', 5000000.00),
    ('5105105105105100', 2000000.00);
