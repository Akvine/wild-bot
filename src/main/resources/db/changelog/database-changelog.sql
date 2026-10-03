--liquibase formatted sql logicalFilePath:db/changelog/database-changelog.sql

--changeset akvine:TG-BOT-1-1
--preconditions onFail:MARK_RAN onError:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'DB_LOCK'
CREATE TABLE DB_LOCK
(
    LOCK_ID      VARCHAR(200)                        NOT NULL,
    PROCESS_ID   VARCHAR(36)                         NOT NULL,
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX DB_LOCK_ID_INDX ON DB_LOCK (LOCK_ID);
--rollback not required

--changeset akvine:TG-BOT-1-2
--preconditions onFail:MARK_RAN onError:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'DB_LOCK_KEEPALIVE'
CREATE TABLE DB_LOCK_KEEPALIVE
(
    PROCESS_ID   VARCHAR(36)                         NOT NULL,
    EXPIRY_DATE  TIMESTAMP                           NOT NULL,
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE UNIQUE INDEX DB_LOCK_KEEP_INDX ON DB_LOCK_KEEPALIVE (PROCESS_ID);
--rollback not required

--changeset akvine:TG-BOT-1-3
--preconditions onFail:MARK_RAN onError:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ASYNC_DB_LOCKS'
CREATE TABLE ASYNC_DB_LOCKS
(
    LOCK_ID    VARCHAR(200) NOT NULL,
    EXPIRE     DECIMAL(22)  NOT NULL,
    CREATOR_ID VARCHAR(36)  NOT NULL,
    LOCK_STATE VARCHAR(50)  NOT NULL
);
CREATE UNIQUE INDEX ASYNC_DB_LOCKS_INDX ON ASYNC_DB_LOCKS (LOCK_ID);
--rollback not required

--changeset akvine:TG-BOT-1-4
--preconditions onFail:MARK_RAN onError:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'XDUAL'
CREATE TABLE XDUAL
(
    DUMMY VARCHAR(1)
);
INSERT INTO XDUAL (DUMMY)
VALUES ('X');
--rollback not required

--changeset akvine:TG-BOT-1-5
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CLIENT_ENTITY' and table_schema = 'public';
CREATE TABLE CLIENT_ENTITY
(
    ID                          BIGINT       NOT NULL,
    UUID                        VARCHAR(255) NOT NULL,
    IS_DELETED                  BOOLEAN      NOT NULL,
    DELETED_DATE                TIMESTAMP,
    FIRST_NAME                  VARCHAR(255) NOT NULL,
    LAST_NAME                   VARCHAR(255),
    CHAT_ID                     VARCHAR(255) NOT NULL,
    USERNAME                    VARCHAR(255),
    IS_IN_WHITELIST             BOOLEAN      NOT NULL,
    BOT_TYPE                        VARCHAR(32)  NOT NULL,
    AVAILABLE_TESTS_COUNT       INTEGER DEFAULT 0,
    CREATED_DATE    TIMESTAMP    NOT NULL,
    UPDATED_DATE    TIMESTAMP,
    CONSTRAINT CLIENT_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_CLIENT_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CLIENT_ID_INDEX ON CLIENT_ENTITY (ID);
CREATE UNIQUE INDEX CLIENT_UUID_INDEX ON CLIENT_ENTITY (UUID);
CREATE UNIQUE INDEX CLIENT_CHAT_ID_INDEX ON CLIENT_ENTITY (CHAT_ID);
CREATE UNIQUE INDEX CLIENT_USERNAME_INDEX ON CLIENT_ENTITY (USERNAME);

--changeset akvine:TG-BOT-1-6
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CARD_TYPE_ENTITY' and table_schema = 'public';
CREATE TABLE CARD_TYPE_ENTITY (
    ID                  BIGINT              NOT NULL,
    TYPE                VARCHAR(128)        NOT NULL,
    CREATED_DATE        TIMESTAMP           NOT NULL,
    UPDATED_DATE        TIMESTAMP,
    CONSTRAINT CARD_TYPE_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_CARD_TYPE_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CARD_TYPE_ID_INDEX ON CARD_TYPE_ENTITY (ID);

--changeset akvine:TG-BOT-1-7
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CARD_ENTITY' and table_schema = 'public';
CREATE TABLE CARD_ENTITY
(
    ID                      BIGINT       NOT NULL,
    UUID                    VARCHAR(255) NOT NULL,
    EXTERNAL_ID             INTEGER      NOT NULL,
    EXTERNAL_TITLE          VARCHAR(255) NOT NULL,
    CATEGORY_ID             INTEGER      NOT NULL,
    CATEGORY_TITLE          VARCHAR(255) NOT NULL,
    BARCODE                 VARCHAR(255) NOT NULL,
    CARD_TYPE_ID            BIGINT       NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    IS_DELETED              BOOLEAN      NOT NULL,
    DELETED_DATE            TIMESTAMP,
    CONSTRAINT CARD_PKEY PRIMARY KEY (id),
    CONSTRAINT CARD_TYPE_FKEY FOREIGN KEY (CARD_TYPE_ID) REFERENCES CARD_TYPE_ENTITY (ID)
);
CREATE SEQUENCE SEQ_CARD_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CARD_ID_INDEX ON CARD_ENTITY (ID);
CREATE UNIQUE INDEX CARD_UUID_INDEX ON CARD_ENTITY (UUID);
CREATE INDEX CARD_EXTERNAL_ID_INDEX ON CARD_ENTITY (EXTERNAL_ID);
CREATE INDEX CARD_CATEGORY_ID_INDEX ON CARD_ENTITY (CATEGORY_ID);

--changeset akvine:TG-BOT-1-8
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ADVERT_ENTITY' and table_schema = 'public';
CREATE TABLE ADVERT_ENTITY
(
    ID                      BIGINT       NOT NULL,
    UUID                    VARCHAR(255) NOT NULL,
    EXTERNAL_ID             INTEGER      NOT NULL,
    EXTERNAL_TITLE          VARCHAR(255) NOT NULL,
    CHANGE_TIME             TIMESTAMP    NOT NULL,
    STATUS                  VARCHAR(64)  NOT NULL,
    ORDINAL_STATUS          INTEGER      NOT NULL,
    TYPE                    VARCHAR(64)  NOT NULL,
    ORDINAL_TYPE            INTEGER      NOT NULL,
    CPM                     INTEGER      NOT NULL,
    START_CHECK_DATE_TIME   TIMESTAMP,
    NEXT_CHECK_DATE_TIME    TIMESTAMP,
    START_BUDGET_SUM        INTEGER,
    CHECK_BUDGET_SUM        INTEGER,
    AVAILABLE_FOR_START    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAUNCHED_BY_CLIENT_ID   BIGINT,
    IS_LOCKED               BOOLEAN      NOT NULL,
    CARD_ID                 BIGINT       NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    IS_DELETED              BOOLEAN      NOT NULL,
    DELETED_DATE            TIMESTAMP,
    CONSTRAINT ADVERT_PKEY PRIMARY KEY (id),
    CONSTRAINT ADVERT_CARD_FKEY FOREIGN KEY (CARD_ID) REFERENCES CARD_ENTITY (ID),
    CONSTRAINT ADVERT_CLIENT_FKEY FOREIGN KEY (LAUNCHED_BY_CLIENT_ID) REFERENCES CLIENT_ENTITY (ID)
);
CREATE SEQUENCE SEQ_ADVERT_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ADVERT_ID_INDEX ON ADVERT_ENTITY (ID);
CREATE UNIQUE INDEX ADVERT_UUID_INDEX ON ADVERT_ENTITY (UUID);
CREATE UNIQUE INDEX ADVERT_EXTERNAL_ID_INDEX ON ADVERT_ENTITY (EXTERNAL_ID);
CREATE INDEX ADVERT_LAUNCHED_BY_CLIENT_ID_INDEX ON ADVERT_ENTITY (LAUNCHED_BY_CLIENT_ID);

--changeset akvine:TG-BOT-1-9
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CLIENT_BLOCKED_CREDENTIALS_ENTITY' and table_schema = 'public';
CREATE TABLE CLIENT_BLOCKED_CREDENTIALS_ENTITY
(
    ID               BIGINT                              NOT NULL,
    CHAT_ID          VARCHAR(255)                        NOT NULL,
    BLOCK_START_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    BLOCK_END_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT CLIENT_BLOCKED_CREDENTIALS_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_CLIENT_BLOCKED_CREDENTIALS_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CLIENT_BLOCKED_CREDENTIALS_CHAT_ID_INDEX ON CLIENT_BLOCKED_CREDENTIALS_ENTITY (CHAT_ID);

--changeset akvine:TG-BOT-1-10
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and table_schema = 'public';
CREATE TABLE ADVERT_STATISTIC_ENTITY
(
    ID                      BIGINT       NOT NULL,
    VIEWS                   VARCHAR(255),
    CLICKS                  VARCHAR(255),
    CTR                     VARCHAR(255),
    CPC                     VARCHAR(255),
    SUM                     VARCHAR(255),
    ATBS                    VARCHAR(255),
    ORDERS                  VARCHAR(255),
    CR                      VARCHAR(255),
    SHKS                    VARCHAR(255),
    SUM_PRICE               VARCHAR(255),
    PHOTO                   BYTEA,
    IS_ACTIVE               BOOLEAN DEFAULT FALSE,
    CLIENT_ID               BIGINT       NOT NULL,
    ADVERT_ID               BIGINT       NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    IS_DELETED              BOOLEAN      NOT NULL,
    DELETED_DATE            TIMESTAMP,
    CONSTRAINT ADVERT_STATISTIC_PKEY PRIMARY KEY (id),
    CONSTRAINT ADVERT_STATISTIC_CLIENT_FKEY FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT_ENTITY (ID),
    CONSTRAINT ADVERT_STATISTIC_ADVERT_FKEY FOREIGN KEY (ADVERT_ID) REFERENCES ADVERT_ENTITY (ID)
);
CREATE SEQUENCE SEQ_ADVERT_STATISTIC_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ADVERT_STATISTIC_ID_INDEX ON ADVERT_STATISTIC_ENTITY (ID);
CREATE INDEX ADVERT_STATISTIC_CLIENT_ID_INDEX ON ADVERT_STATISTIC_ENTITY (CLIENT_ID);

--changeset akvine:TG-BOT-1-11
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ITERATION_COUNTER_ENTITY' and table_schema = 'public';
CREATE TABLE ITERATION_COUNTER_ENTITY
(
    ID                      BIGINT       NOT NULL,
    ADVERT_ID               INTEGER      NOT NULL,
    COUNT                   INTEGER      NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    CONSTRAINT ITERATION_COUNTER_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_ITERATION_COUNTER_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ITERATION_COUNTER_ID_INDEX ON ITERATION_COUNTER_ENTITY (ID);
CREATE UNIQUE INDEX ITERATION_COUNTER_ADVERT_ID_INDEX ON ITERATION_COUNTER_ENTITY (ADVERT_ID);

--changeset akvine:TG-BOT-1-12
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CLIENT_SESSION_DATA_ENTITY' and table_schema = 'public';
CREATE TABLE CLIENT_SESSION_DATA_ENTITY
(
    ID                                      BIGINT       NOT NULL,
    CHAT_ID                                 VARCHAR(255) NOT NULL,
    SELECTED_CARD_TYPE                      VARCHAR(64),
    SELECTED_CATEGORY_ID                    INTEGER      NOT NULL,
    UPLOADED_CARD_PHOTO                     BYTEA,
    IS_INPUT_NEW_CARD_PRICE_AND_DISCOUNT    BOOLEAN      NOT NULL,
    NEW_CARD_PRICE                          INTEGER,
    NEW_CARD_DISCOUNT                       INTEGER,
    LOCKED_ADVERT_ID                        INTEGER,
    CREATED_DATE                            TIMESTAMP    NOT NULL,
    UPDATED_DATE                            TIMESTAMP,
    CONSTRAINT CLIENT_SESSION_DATA_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_CLIENT_SESSION_DATA_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CLIENT_SESSION_DATA_ID_INDEX ON CLIENT_SESSION_DATA_ENTITY (ID);
CREATE UNIQUE INDEX CLIENT_SESSION_DATA_CHAT_ID_INDEX ON CLIENT_SESSION_DATA_ENTITY (CHAT_ID);

--changeset akvine:TG-BOT-1-13
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'SUBSCRIPTION_ENTITY' and table_schema = 'public';
CREATE TABLE SUBSCRIPTION_ENTITY
(
    ID                          BIGINT       NOT NULL,
    CLIENT_ID                   BIGINT       NOT NULL,
    EXPIRES_AT                  TIMESTAMP    NOT NULL,
    IS_NOTIFIED_THAT_EXPIRES    BOOLEAN      DEFAULT FALSE NOT NULL,
    CREATED_DATE                TIMESTAMP    NOT NULL,
    UPDATED_DATE                TIMESTAMP,
    CONSTRAINT SUBSCRIPTION_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_SUBSCRIPTION_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX SUBSCRIPTION_ID_INDEX ON SUBSCRIPTION_ENTITY (ID);
CREATE UNIQUE INDEX SUBSCRIPTION_CLIENT_ID_INDEX ON SUBSCRIPTION_ENTITY (CLIENT_ID);

--changeset akvine:TG-BOT-1-14
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'SPRING_SESSION' and table_schema = 'public';
CREATE TABLE SPRING_SESSION
(
    PRIMARY_ID            VARCHAR(36)    NOT NULL,
    SESSION_ID            VARCHAR(36),
    CREATION_TIME         NUMERIC(19, 0) NOT NULL,
    LAST_ACCESS_TIME      NUMERIC(19, 0) NOT NULL,
    MAX_INACTIVE_INTERVAL NUMERIC(10, 0) NOT NULL,
    EXPIRY_TIME           NUMERIC(19, 0) NOT NULL,
    PRINCIPAL_NAME        VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);
CREATE INDEX SPRING_SESSION_INDX ON SPRING_SESSION(LAST_ACCESS_TIME);
--rollback not required

--changeset akvine:TG-BOT-1-15
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'SPRING_SESSION_ATTRIBUTES' and table_schema = 'public';
CREATE TABLE SPRING_SESSION_ATTRIBUTES
(
    SESSION_PRIMARY_ID VARCHAR(36),
    ATTRIBUTE_NAME     VARCHAR(200),
    ATTRIBUTE_BYTES    BYTEA,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
);
CREATE INDEX SPRING_SESSION_ATTRIBUTES_INDX on SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID);
--rollback not required

--changeset akvine:TG-BOT-1-16
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'AUTH_ACTION_ENTITY' and table_schema = 'public';
CREATE TABLE AUTH_ACTION_ENTITY
(
    ID                        BIGINT                              NOT NULL,
    SESSION_ID                VARCHAR(144)                        NOT NULL,
    LOGIN                     VARCHAR(64)                         NOT NULL,
    STARTED_DATE              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ACTION_EXPIRED_AT         TIMESTAMP                           NOT NULL,
    PWD_INVALID_ATTEMPTS_LEFT INTEGER                             NOT NULL,
    OTP_COUNT_LEFT            INTEGER                             NOT NULL,
    OTP_NUMBER                INTEGER,
    OTP_LAST_UPDATE           TIMESTAMP,
    OTP_EXPIRED_AT            TIMESTAMP,
    OTP_INVALID_ATTEMPTS_LEFT INTEGER                             NOT NULL,
    OTP_VALUE                 VARCHAR(32),
    CONSTRAINT AUTH_ACTION_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_AUTH_ACTION_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX AUTH_ACTION_INDX on AUTH_ACTION_ENTITY (LOGIN);
CREATE INDEX AUTH_ACTION_EXP_INDX on AUTH_ACTION_ENTITY (ACTION_EXPIRED_AT);
--rollback not required

--changeset akvine:TG-BOT-1-17
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'REGISTRATION_ACTION_ENTITY' and table_schema = 'public';
CREATE TABLE REGISTRATION_ACTION_ENTITY
(
    ID                        BIGINT                              NOT NULL,
    SESSION_ID                VARCHAR(144)                        NOT NULL,
    LOGIN                     VARCHAR(64)                         NOT NULL,
    STARTED_DATE              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ACTION_EXPIRED_AT         TIMESTAMP                           NOT NULL,
    STATE                     VARCHAR(32)                         NOT NULL,
    OTP_COUNT_LEFT            INTEGER                             NOT NULL,
    OTP_NUMBER                INTEGER,
    OTP_LAST_UPDATE           TIMESTAMP,
    OTP_EXPIRED_AT            TIMESTAMP,
    OTP_INVALID_ATTEMPTS_LEFT INTEGER                             NOT NULL,
    OTP_VALUE                 VARCHAR(32),
    CONSTRAINT REGISTRATION_ACTION_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_REGISTRATION_ACTION_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX REGISTRATION_ACTION_LOGIN_INDX ON REGISTRATION_ACTION_ENTITY (LOGIN);
CREATE INDEX REGISTRATION_ACTION_AEA_INDX ON REGISTRATION_ACTION_ENTITY (ACTION_EXPIRED_AT);
--rollback not required

--changeset akvine:TG-BOT-1-18
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'OTP_COUNTER_ENTITY' and table_schema = 'public';
CREATE TABLE OTP_COUNTER_ENTITY
(
    ID           BIGINT                              NOT NULL,
    LOGIN        VARCHAR(64)                         NOT NULL,
    LAST_UPDATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    OTP_VALUE    BIGINT    DEFAULT 1                 NOT NULL,
    CONSTRAINT OTP_COUNTER_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_OTP_COUNTER_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX OTP_COUNTER_LOGIN_INDX ON OTP_COUNTER_ENTITY (LOGIN);
--rollback not required

--changeset akvine:TG-BOT-1-19
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ACCESS_RESTORE_ACTION_ENTITY' and table_schema = 'public';
CREATE TABLE ACCESS_RESTORE_ACTION_ENTITY
(
    ID                        BIGINT                              NOT NULL,
    SESSION_ID                VARCHAR(144)                        NOT NULL,
    LOGIN                     VARCHAR(64)                         NOT NULL,
    STARTED_DATE              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ACTION_EXPIRED_AT         TIMESTAMP                           NOT NULL,
    STATE                     VARCHAR(32)                         NOT NULL,
    OTP_COUNT_LEFT            INTEGER                             NOT NULL,
    OTP_NUMBER                INTEGER,
    OTP_LAST_UPDATE           TIMESTAMP,
    OTP_EXPIRED_AT            TIMESTAMP,
    OTP_INVALID_ATTEMPTS_LEFT INTEGER                             NOT NULL,
    OTP_VALUE                 VARCHAR(32),
    CONSTRAINT ACCESS_RESTORE_ACTION_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_ACCESS_RESTORE_ACTION_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ACCESS_RESTORE_LOGIN_INDX on ACCESS_RESTORE_ACTION_ENTITY (LOGIN);
CREATE INDEX ACCESS_RESTORE_EXP_INDX on ACCESS_RESTORE_ACTION_ENTITY (ACTION_EXPIRED_AT);
--rollback not required

--changeset akvine:TG-BOT-1-20
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'BLOCKED_CREDENTIALS_ENTITY' and table_schema = 'public';
CREATE TABLE BLOCKED_CREDENTIALS_ENTITY
(
    ID               BIGINT                              NOT NULL,
    LOGIN            VARCHAR(64)                         NOT NULL,
    BLOCK_START_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    BLOCK_END_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT BLOCKED_CREDENTIALS_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_BLOCKED_CREDENTIALS_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX BLOCKED_CREDENTIALS_LOGIN_INDX ON BLOCKED_CREDENTIALS_ENTITY (LOGIN);
--rollback not required

--changeset akvine:TG-BOT-1-21
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'SUPPORT_USER_ENTITY' and table_schema = 'public';
CREATE TABLE SUPPORT_USER_ENTITY
(
    ID                          BIGINT       NOT NULL,
    UUID                        VARCHAR(255) NOT NULL,
    EMAIL                       VARCHAR(255) NOT NULL,
    HASH                        VARCHAR(255) NOT NULL,
    CREATED_DATE    TIMESTAMP    NOT NULL,
    UPDATED_DATE    TIMESTAMP,
    CONSTRAINT SUPPORT_USER_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_SUPPORT_USER_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX SUPPORT_USER_ID_INDEX ON SUPPORT_USER_ENTITY (ID);
CREATE UNIQUE INDEX SUPPORT_USER_UUID_INDEX ON SUPPORT_USER_ENTITY (UUID);
CREATE UNIQUE INDEX SUPPORT_USER_EMAIL_INDEX ON SUPPORT_USER_ENTITY (EMAIL);

--changeset akvine:TG-BOT-1-22
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(column_name) = 'BOT_TYPE' and upper(table_name) = 'CLIENT_BLOCKED_CREDENTIALS_ENTITY';
ALTER TABLE CLIENT_BLOCKED_CREDENTIALS_ENTITY ADD BOT_TYPE VARCHAR(32) NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-23
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(column_name) = 'TOKEN' and upper(table_name) = 'CLIENT_ENTITY';
ALTER TABLE CLIENT_ENTITY ADD TOKEN VARCHAR(255);
--rollback not required

--changeset akvine:TG-BOT-1-24
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(column_name) = 'WAREHOUSE_ID' and upper(table_name) = 'CLIENT_ENTITY';
ALTER TABLE CLIENT_ENTITY ADD WAREHOUSE_ID INTEGER;
--rollback not required

--changeset akvine:TG-BOT-1-25
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(column_name) = 'CLIENT_ID' and upper(table_name) = 'CARD_ENTITY';
ALTER TABLE CARD_ENTITY ADD CLIENT_ID BIGINT NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-26
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(column_name) = 'LAUNCHED_BY_CLIENT_ID' and upper(table_name) = 'ADVERT_ENTITY';
ALTER TABLE ADVERT_ENTITY DROP COLUMN LAUNCHED_BY_CLIENT_ID;
--rollback not required

--changeset akvine:TG-BOT-1-27
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CLIENT_STATES_ENTITY' and table_schema = 'public';
CREATE TABLE CLIENT_STATES_ENTITY
(
    ID                          BIGINT       NOT NULL,
    IDENTIFIER                  VARCHAR(255) NOT NULL,
    STATES                      TEXT NOT NULL,
    CREATED_DATE                TIMESTAMP    NOT NULL,
    UPDATED_DATE                TIMESTAMP,
    CONSTRAINT CLIENT_STATES_PKEY PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_CLIENT_STATES_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CLIENT_STATES_ENTITY_ID_INDEX ON CLIENT_STATES_ENTITY (ID);
CREATE UNIQUE INDEX CLIENT_STATES_ENTITY_IDENTIFIER_INDEX ON CLIENT_STATES_ENTITY (IDENTIFIER);
--rollback not required

--changeset akvine:TG-BOT-1-28
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(column_name) = 'BOT_TYPE' and upper(table_name) = 'CLIENT_SESSION_DATA_ENTITY';
ALTER TABLE CLIENT_SESSION_DATA_ENTITY ADD BOT_TYPE VARCHAR(32) NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-29
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(column_name) = 'IS_LOCKED' and upper(table_name) = 'ADVERT_ENTITY';
ALTER TABLE ADVERT_ENTITY DROP COLUMN IS_LOCKED;
--rollback not required

--changeset akvine:TG-BOT-1-30
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns WHERE upper(table_name) = 'CLIENT_SESSION_DATA_ENTITY' AND upper(column_name) = 'LOCKED_ADVERT_ID';
ALTER TABLE CLIENT_SESSION_DATA_ENTITY RENAME COLUMN LOCKED_ADVERT_ID TO ADVERT_ID_TO_START;
--rollback not required

--changeset akvine:TG-BOT-1-31
--preconditions onFail:MARK_RAN onError:HALT onUpdateSql:FAIL
--precondition-sql-check expectedResult:1 select count(*) from pg_indexes where schemaname = current_schema() and indexname = 'client_username_index';
 DROP INDEX CLIENT_USERNAME_INDEX;
 --rollback not required

--changeset akvine:TG-BOT-1-32
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'IDEMPOTENCY_KEY_ENTITY' and table_schema = 'public';
CREATE TABLE IDEMPOTENCY_KEY_ENTITY
(
    IDEMPOTENCY_KEY VARCHAR(255) NOT NULL,
    FINGERPRINT     VARCHAR(255),
    STATUS          VARCHAR(32)  NOT NULL,
    PAYLOAD         TEXT,
    EXPIRES_AT      TIMESTAMP    NOT NULL,
    CREATED_DATE    TIMESTAMP    NOT NULL,
    UPDATED_DATE    TIMESTAMP,
    CONSTRAINT IDEMPOTENCY_KEY_PKEY PRIMARY KEY (IDEMPOTENCY_KEY)
);
CREATE INDEX IDEMPOTENCY_KEY_EXPIRES_AT_INDEX ON IDEMPOTENCY_KEY_ENTITY (EXPIRES_AT);
--rollback not required

--changeset akvine:TG-BOT-1-33
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'OUTBOX_MESSAGE_ENTITY' and table_schema = 'public';
CREATE TABLE OUTBOX_MESSAGE_ENTITY
(
    ID              BIGINT       NOT NULL,
    MESSAGE_TYPE    VARCHAR(64)  NOT NULL,
    PAYLOAD         TEXT         NOT NULL,
    DEDUP_KEY       VARCHAR(255),
    STATUS          VARCHAR(16)  NOT NULL,
    ATTEMPTS        INTEGER      NOT NULL,
    NEXT_ATTEMPT_AT TIMESTAMP    NOT NULL,
    LOCKED_UNTIL    TIMESTAMP,
    LAST_ERROR      TEXT,
    SENT_DATE       TIMESTAMP,
    CREATED_DATE    TIMESTAMP    NOT NULL,
    UPDATED_DATE    TIMESTAMP,
    CONSTRAINT OUTBOX_MESSAGE_PKEY PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_OUTBOX_MESSAGE_ENTITY START WITH 1 INCREMENT BY 1;
CREATE UNIQUE INDEX OUTBOX_MESSAGE_DEDUP_KEY_INDEX ON OUTBOX_MESSAGE_ENTITY (DEDUP_KEY);
CREATE INDEX OUTBOX_MESSAGE_STATUS_NEXT_ATTEMPT_INDEX ON OUTBOX_MESSAGE_ENTITY (STATUS, NEXT_ATTEMPT_AT);
--rollback not required

--changeset akvine:TG-BOT-1-34
--preconditions onFail:HALT onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from card_entity ce where not exists (select 1 from client_entity c where c.id = ce.client_id)
ALTER TABLE CARD_ENTITY ADD CONSTRAINT CARD_CLIENT_FKEY FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT_ENTITY (ID);
--rollback ALTER TABLE CARD_ENTITY DROP CONSTRAINT CARD_CLIENT_FKEY;

--changeset akvine:TG-BOT-1-35
--preconditions onFail:HALT onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from subscription_entity se where not exists (select 1 from client_entity c where c.id = se.client_id)
ALTER TABLE SUBSCRIPTION_ENTITY ADD CONSTRAINT SUBSCRIPTION_CLIENT_FKEY FOREIGN KEY (CLIENT_ID) REFERENCES CLIENT_ENTITY (ID);
--rollback ALTER TABLE SUBSCRIPTION_ENTITY DROP CONSTRAINT SUBSCRIPTION_CLIENT_FKEY;

--changeset akvine:TG-BOT-1-36
CREATE INDEX IF NOT EXISTS CARD_CLIENT_ID_INDEX ON CARD_ENTITY (CLIENT_ID);
CREATE INDEX IF NOT EXISTS CARD_CARD_TYPE_ID_INDEX ON CARD_ENTITY (CARD_TYPE_ID);
CREATE INDEX IF NOT EXISTS ADVERT_CARD_ID_INDEX ON ADVERT_ENTITY (CARD_ID);
CREATE INDEX IF NOT EXISTS ADVERT_STATISTIC_ADVERT_ID_INDEX ON ADVERT_STATISTIC_ENTITY (ADVERT_ID);
--rollback DROP INDEX IF EXISTS CARD_CLIENT_ID_INDEX;
--rollback DROP INDEX IF EXISTS CARD_CARD_TYPE_ID_INDEX;
--rollback DROP INDEX IF EXISTS ADVERT_CARD_ID_INDEX;
--rollback DROP INDEX IF EXISTS ADVERT_STATISTIC_ADVERT_ID_INDEX;

--changeset akvine:TG-BOT-1-37
UPDATE CLIENT_ENTITY SET DELETED_DATE = NULL WHERE IS_DELETED = FALSE AND DELETED_DATE IS NOT NULL;
UPDATE CLIENT_ENTITY SET DELETED_DATE = COALESCE(UPDATED_DATE, CREATED_DATE) WHERE IS_DELETED = TRUE AND DELETED_DATE IS NULL;
UPDATE CARD_ENTITY SET DELETED_DATE = NULL WHERE IS_DELETED = FALSE AND DELETED_DATE IS NOT NULL;
UPDATE CARD_ENTITY SET DELETED_DATE = COALESCE(UPDATED_DATE, CREATED_DATE) WHERE IS_DELETED = TRUE AND DELETED_DATE IS NULL;
UPDATE ADVERT_ENTITY SET DELETED_DATE = NULL WHERE IS_DELETED = FALSE AND DELETED_DATE IS NOT NULL;
UPDATE ADVERT_ENTITY SET DELETED_DATE = COALESCE(UPDATED_DATE, CREATED_DATE) WHERE IS_DELETED = TRUE AND DELETED_DATE IS NULL;
UPDATE ADVERT_STATISTIC_ENTITY SET DELETED_DATE = NULL WHERE IS_DELETED = FALSE AND DELETED_DATE IS NOT NULL;
UPDATE ADVERT_STATISTIC_ENTITY SET DELETED_DATE = COALESCE(UPDATED_DATE, CREATED_DATE) WHERE IS_DELETED = TRUE AND DELETED_DATE IS NULL;
ALTER TABLE CLIENT_ENTITY ADD CONSTRAINT CLIENT_SOFT_DELETE_CHECK CHECK (IS_DELETED = (DELETED_DATE IS NOT NULL));
ALTER TABLE CARD_ENTITY ADD CONSTRAINT CARD_SOFT_DELETE_CHECK CHECK (IS_DELETED = (DELETED_DATE IS NOT NULL));
ALTER TABLE ADVERT_ENTITY ADD CONSTRAINT ADVERT_SOFT_DELETE_CHECK CHECK (IS_DELETED = (DELETED_DATE IS NOT NULL));
ALTER TABLE ADVERT_STATISTIC_ENTITY ADD CONSTRAINT ADVERT_STATISTIC_SOFT_DELETE_CHECK CHECK (IS_DELETED = (DELETED_DATE IS NOT NULL));
--rollback ALTER TABLE ADVERT_STATISTIC_ENTITY DROP CONSTRAINT ADVERT_STATISTIC_SOFT_DELETE_CHECK;
--rollback ALTER TABLE ADVERT_ENTITY DROP CONSTRAINT ADVERT_SOFT_DELETE_CHECK;
--rollback ALTER TABLE CARD_ENTITY DROP CONSTRAINT CARD_SOFT_DELETE_CHECK;
--rollback ALTER TABLE CLIENT_ENTITY DROP CONSTRAINT CLIENT_SOFT_DELETE_CHECK;