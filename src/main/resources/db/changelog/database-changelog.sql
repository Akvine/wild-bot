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
    USERNAME                    VARCHAR(255) NOT NULL,
    IS_IN_WHITELIST             BOOLEAN      NOT NULL,
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