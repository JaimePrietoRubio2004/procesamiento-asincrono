CREATE TABLE tarea (
    id                    UUID PRIMARY KEY,
    tipo                  VARCHAR(100) NOT NULL,
    payload               JSONB NOT NULL,
    estado                VARCHAR(20) NOT NULL,
    prioridad             VARCHAR(10) NOT NULL,
    contador_reintentos   INT NOT NULL DEFAULT 0,
    max_reintentos        INT NOT NULL,
    fecha_programada      TIMESTAMP NULL,
    expresion_cron        VARCHAR(100) NULL,
    mensaje_error         TEXT NULL,
    fecha_creacion        TIMESTAMP NOT NULL,
    fecha_actualizacion   TIMESTAMP NOT NULL,
    fecha_finalizacion    TIMESTAMP NULL
);

CREATE INDEX idx_tarea_estado ON tarea (estado);
CREATE INDEX idx_tarea_tipo ON tarea (tipo);
CREATE INDEX idx_tarea_fecha_programada ON tarea (fecha_programada);

CREATE TABLE ejecucion_tarea (
    id                UUID PRIMARY KEY,
    tarea_id          UUID NOT NULL REFERENCES tarea (id),
    numero_intentos   INT NOT NULL,
    estado            VARCHAR(30) NOT NULL,
    mensaje_error     TEXT NULL,
    fecha_inicio      TIMESTAMP NOT NULL,
    fecha_fin         TIMESTAMP NULL
);

CREATE INDEX idx_ejecucion_tarea_tarea_id_numero ON ejecucion_tarea (tarea_id, numero_intentos);