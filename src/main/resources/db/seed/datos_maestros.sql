-- ============================================================================
-- SIGEA - Script de carga de datos maestros (H2)
-- ----------------------------------------------------------------------------
-- Uso: ejecutar contra la base H2 recién regenerada (ddl-auto: update ya creó
-- las tablas). No incluye Rol ni Funcionalidad: el rol Superusuario y el
-- catálogo de funcionalidades/permisos se siembran automáticamente al
-- arrancar la app (FuncionalidadSeeder / SuperusuarioSeeder). Los roles
-- Director y Secretaria se crean desde la UI de administración.
--
-- Todas las entidades heredan de AuditableEntity, por lo que cada INSERT
-- incluye: version, estado, fecha_registro, fecha_modificacion. Las columnas
-- usuario_creacion_id / usuario_modificacion_id quedan en NULL (son nullable)
-- porque este script no corre en el contexto de un usuario autenticado.
--
-- Ajusta los valores de ejemplo (montos, años, grados) según lo que necesites
-- mostrar en la sustentación.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. tipo_documento
-- ----------------------------------------------------------------------------
INSERT INTO tipo_documento (cod_tipo_documento, descripcion, version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 'DNI', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Carné de Extranjería', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Pasaporte', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'RUC', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 2. nivel
-- ----------------------------------------------------------------------------
INSERT INTO nivel (cod_nivel, nombre, version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 'Inicial', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Primaria', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Secundaria', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 3. grado (nivel_id -> nivel.cod_nivel)
-- ----------------------------------------------------------------------------
INSERT INTO grado (cod_grado, nivel_id, nombre_grado, version, estado, fecha_registro, fecha_modificacion)
VALUES
-- Inicial (nivel_id = 1)
(1, 1, '3 años', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '4 años', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '5 años', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Primaria (nivel_id = 2)
(4, 2, '1°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, '2°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, '3°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 2, '4°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, '5°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 2, '6°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Secundaria (nivel_id = 3)
(10, 3, '1°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 3, '2°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 3, '3°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 3, '4°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 3, '5°', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 4. anio_academico
-- ----------------------------------------------------------------------------
INSERT INTO anio_academico (cod_anio_academico, anio, version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 2025, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 2026, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 5. tipo_concepto
-- ----------------------------------------------------------------------------
INSERT INTO tipo_concepto (cod_tipo_concepto, nombre, version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 'Matrícula', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Pensión', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Materiales Educativos', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Seguro Escolar', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 6. concepto (anio_academico_id -> anio_academico.cod_anio_academico,
--              tipo_concepto_id  -> tipo_concepto.cod_tipo_concepto)
--    tipo: FIJO | MENSUAL | OPCIONAL
-- ----------------------------------------------------------------------------
INSERT INTO concepto
(cod_concepto, anio_academico_id, tipo_concepto_id, nombre_concepto, monto, orden_pago, obligatorio, tipo,
 version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 2, 1, 'Matrícula 2026', 350.00, 1, TRUE, 'FIJO', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 2, 2, 'Pensión mensual 2026', 320.00, 2, TRUE, 'MENSUAL', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 2, 3, 'Materiales educativos 2026', 180.00, 3, FALSE, 'OPCIONAL', 0, TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       (4, 2, 4, 'Seguro escolar 2026', 60.00, 4, TRUE, 'FIJO', 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 7. parametro
--    VACANTES_MAXIMAS_DEFAULT es el único parámetro que el código lee hoy
--    (AulaServiceImpl.crear). Los demás son opcionales/documentales.
-- ----------------------------------------------------------------------------
INSERT INTO parametro (cod_parametro, clave, valor, descripcion, version, estado, fecha_registro, fecha_modificacion)
VALUES (1, 'VACANTES_MAXIMAS_DEFAULT', '30', 'Capacidad máxima por defecto de un aula al crearla', 0, TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- Fin del script. Recuerda que las tablas usan IDENTITY (auto_increment) en
-- H2; al insertar IDs explícitos H2 ajusta automáticamente el contador
-- interno, por lo que los siguientes INSERT hechos por la aplicación (por
-- ejemplo, un nuevo Nivel desde la UI) seguirán generando IDs correlativos
-- sin colisionar con los de este script.
-- ============================================================================
