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
    ID              BIGINT       NOT NULL,
    UUID            VARCHAR(255) NOT NULL,
    IS_DELETED      BOOLEAN      NOT NULL,
    DELETED_DATE    TIMESTAMP,
    FIRST_NAME      VARCHAR(255) NOT NULL,
    LAST_NAME       VARCHAR(255),
    CHAT_ID         VARCHAR(255) NOT NULL,
    USERNAME        VARCHAR(255) NOT NULL,
    IS_IN_WHITELIST BOOLEAN      NOT NULL,
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
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ADVERT_ENTITY' and table_schema = 'public';
CREATE TABLE ADVERT_ENTITY
(
    ID                      BIGINT       NOT NULL,
    UUID                    VARCHAR(255) NOT NULL,
    ADVERT_ID               INTEGER      NOT NULL,
    NAME                    VARCHAR(255) NOT NULL,
    CHANGE_TIME             TIMESTAMP    NOT NULL,
    ITEM_ID                 INTEGER      NOT NULL,
    CATEGORY_ID             INTEGER      NOT NULL,
    STATUS                  VARCHAR(64)  NOT NULL,
    ORDINAL_STATUS          INTEGER      NOT NULL,
    TYPE                    VARCHAR(64)  NOT NULL,
    ORDINAL_TYPE            INTEGER      NOT NULL,
    CPM                     INTEGER      NOT NULL,
    START_CHECK_DATE_TIME   TIMESTAMP,
    NEXT_CHECK_DATE_TIME    TIMESTAMP,
    START_BUDGET_SUM        INTEGER,
    CHECK_BUDGET_SUM        INTEGER,
    LAUNCHED_BY_CLIENT_ID   BIGINT,
    IS_LOCKED               BOOLEAN      NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    IS_DELETED              BOOLEAN      NOT NULL,
    DELETED_DATE            TIMESTAMP,
    CONSTRAINT ADVERT_PKEY PRIMARY KEY (id),
    CONSTRAINT ADVERT_CLIENT_FKEY FOREIGN KEY (LAUNCHED_BY_CLIENT_ID) REFERENCES CLIENT_ENTITY (ID)
);
CREATE SEQUENCE SEQ_ADVERT_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ADVERT_ID_INDEX ON ADVERT_ENTITY (ID);
CREATE UNIQUE INDEX ADVERT_UUID_INDEX ON ADVERT_ENTITY (UUID);
CREATE UNIQUE INDEX ADVERT_ADVERT_ID_INDEX ON ADVERT_ENTITY (ADVERT_ID);
CREATE INDEX ADVERT_LAUNCHED_BY_CLIENT_ID_INDEX ON ADVERT_ENTITY (LAUNCHED_BY_CLIENT_ID);

--changeset akvine:TG-BOT-1-7
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CARD_ENTITY' and table_schema = 'public';
CREATE TABLE CARD_ENTITY
(
    ID                      BIGINT       NOT NULL,
    UUID                    VARCHAR(255) NOT NULL,
    ITEM_ID                 INTEGER      NOT NULL,
    ITEM_TITLE              VARCHAR(255) NOT NULL,
    CATEGORY_ID             INTEGER      NOT NULL,
    CATEGORY_TITLE          VARCHAR(255) NOT NULL,
    BARCODE                 VARCHAR(255) NOT NULL,
    CREATED_DATE            TIMESTAMP    NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    IS_DELETED              BOOLEAN      NOT NULL,
    DELETED_DATE            TIMESTAMP,
    CONSTRAINT CARD_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_CARD_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CARD_ID_INDEX ON CARD_ENTITY (ID);
CREATE UNIQUE INDEX CARD_UUID_INDEX ON CARD_ENTITY (UUID);
CREATE INDEX CARD_ITEM_ID_INDEX ON CARD_ENTITY (ITEM_ID);
CREATE INDEX CARD_CATEGORY_ID_INDEX ON CARD_ENTITY (CATEGORY_ID);

--changeset akvine:TG-BOT-1-8
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'CARD_PHOTO_ENTITY' and table_schema = 'public';
CREATE TABLE CARD_PHOTO_ENTITY
(
    ID                      BIGINT          NOT NULL,
    BIG_URL                 VARCHAR(255),
    CARD_ID                 BIGINT          NOT NULL,
    CREATED_DATE            TIMESTAMP       NOT NULL,
    UPDATED_DATE            TIMESTAMP,
    CONSTRAINT CARD_PHOTO_PKEY PRIMARY KEY (id),
    CONSTRAINT CARD_PHOTO_CARD_FKEY FOREIGN KEY (CARD_ID) REFERENCES CARD_ENTITY (ID)
);
CREATE SEQUENCE SEQ_CARD_PHOTO_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX CARD_PHOTO_ID_INDEX ON CARD_PHOTO_ENTITY (ID);
CREATE INDEX CARD_PHOTO_CARD_ID_INDEX ON CARD_PHOTO_ENTITY (CARD_ID);

--changeset akvine:TG-BOT-1-9
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'BLOCKED_CREDENTIALS_ENTITY' and table_schema = 'public';
CREATE TABLE BLOCKED_CREDENTIALS_ENTITY
(
    ID               BIGINT                              NOT NULL,
    UUID             VARCHAR(255)                         NOT NULL,
    BLOCK_START_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    BLOCK_END_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT BLOCKED_CREDENTIALS_PK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_BLOCKED_CREDENTIALS_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX BLOCKED_CREDENTIALS_UUID_INDEX ON BLOCKED_CREDENTIALS_ENTITY (UUID);

--changeset akvine:TG-BOT-1-10
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and table_schema = 'public';
CREATE TABLE ADVERT_STATISTIC_ENTITY
(
    ID                      BIGINT       NOT NULL,
    VIEWS                   VARCHAR(255) NOT NULL,
    CLICKS                  VARCHAR(255) NOT NULL,
    CTR                     VARCHAR(255) NOT NULL,
    CPC                     VARCHAR(255) NOT NULL,
    SUM                     VARCHAR(255) NOT NULL,
    ATBS                    VARCHAR(255) NOT NULL,
    ORDERS                  VARCHAR(255) NOT NULL,
    CR                      VARCHAR(255) NOT NULL,
    SHKS                    VARCHAR(255) NOT NULL,
    SUM_PRICE               VARCHAR(255) NOT NULL,
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
--precondition-sql-check expectedResult:1 select count(*) from information_schema.tables where upper(table_name) = 'CARD_PHOTO_ENTITY' and table_schema = 'public';
DROP TABLE CARD_PHOTO_ENTITY;
DROP SEQUENCE SEQ_CARD_PHOTO_ENTITY;
--rollback not required

--changeset akvine:TG-BOT-1-12
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' AND upper(column_name) = 'VIEWS' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN VIEWS DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-13
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' AND upper(column_name) = 'CLICKS' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN CLICKS DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-14
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' AND upper(column_name) = 'CTR' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN CTR DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-15
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' AND upper(column_name) = 'CPC' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN CPC DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-16
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'SUM' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN SUM DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-17
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'ATBS' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN ATBS DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-18
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'ORDERS' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN ORDERS DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-19
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'CR' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN CR DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-20
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'SHKS' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN SHKS DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-21
--precondition-sql-check expectedResult:1 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'SUM_PRICE' AND is_nullable = 'NO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ALTER COLUMN SUM_PRICE DROP NOT NULL;
--rollback not required

--changeset akvine:TG-BOT-1-22
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(table_name) = 'ADVERT_STATISTIC_ENTITY' and upper(column_name) = 'PHOTO';
ALTER TABLE ADVERT_STATISTIC_ENTITY ADD PHOTO BYTEA;
--rollback not required

--changeset akvine:TG-BOT-1-23
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.tables where upper(table_name) = 'ITERATION_COUNTER_ENTITY' and table_schema = 'public';
CREATE TABLE ITERATION_COUNTER_ENTITY
(
    ID                      BIGINT       NOT NULL,
    ADVERT_ID               INTEGER      NOT NULL,
    COUNTER                 INTEGER      NOT NULL,
    CONSTRAINT ITERATION_COUNTER_PKEY PRIMARY KEY (id)
);
CREATE SEQUENCE SEQ_ITERATION_COUNTER_ENTITY START WITH 1 INCREMENT BY 1000;
CREATE UNIQUE INDEX ITERATION_COUNTER_ID_INDEX ON ITERATION_COUNTER_ENTITY (ID);
CREATE UNIQUE INDEX ITERATION_COUNTER_ADVERT_ID_INDEX ON ITERATION_COUNTER_ENTITY (ADVERT_ID);

--changeset akvine:TG-BOT-1-24
--preconditions onFail:MARK_RAN onError:HALT onUpdateSQL:FAIL
--precondition-sql-check expectedResult:0 select count(*) from information_schema.columns where upper(table_name) = 'CLIENT_ENTITY' and upper(column_name) = 'AVAILABLE_TESTS_COUNT';
ALTER TABLE CLIENT_ENTITY ADD COLUMN AVAILABLE_TESTS_COUNT INTEGER DEFAULT 0;