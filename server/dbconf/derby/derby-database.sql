CREATE TABLE SCHEMA_INFO
	(VERSION INTEGER);

CREATE TABLE EVENT
	(ID INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1) NOT NULL PRIMARY KEY,
	DATE_CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	NAME CLOB NOT NULL,
	EVENT_LEVEL VARCHAR(40) NOT NULL,
	OUTCOME VARCHAR(40),
	ATTRIBUTES CLOB,
	USER_ID INTEGER NOT NULL,
	IP_ADDRESS VARCHAR(40));
	
CREATE TABLE CHANNEL
	(ID VARCHAR(36) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	DESCRIPTION CLOB,
	IS_ENABLED SMALLINT,
	VERSION VARCHAR(40),
	REVISION INTEGER,
	LAST_MODIFIED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	SOURCE_CONNECTOR CLOB,
	DESTINATION_CONNECTORS CLOB,
	PROPERTIES CLOB,
	PREPROCESSING_SCRIPT CLOB,
	POSTPROCESSING_SCRIPT CLOB,
	DEPLOY_SCRIPT CLOB,
	SHUTDOWN_SCRIPT CLOB);

CREATE TABLE CHANNEL_STATISTICS
	(SERVER_ID VARCHAR(36) NOT NULL,
	CHANNEL_ID VARCHAR(36) NOT NULL,
	RECEIVED INTEGER,
	FILTERED INTEGER,
	SENT INTEGER,
	ERROR INTEGER,
	QUEUED INTEGER,
	ALERTED INTEGER,
	PRIMARY KEY(SERVER_ID, CHANNEL_ID),
	CONSTRAINT CHANNEL_STATS_ID_FK FOREIGN KEY(CHANNEL_ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);

CREATE TABLE CHANNEL_TAGS (
	CHANNEL_ID CHAR(36) NOT NULL,
	TAG VARCHAR(255) NOT NULL,
	CONSTRAINT CHANNEL_TAGS_PKEY PRIMARY KEY (channel_id, tag)
);

CREATE TABLE SCRIPT
	(GROUP_ID VARCHAR(40) NOT NULL,
	ID VARCHAR(40) NOT NULL,
	SCRIPT CLOB);
	
ALTER TABLE SCRIPT ADD PRIMARY KEY (GROUP_ID, ID);	

CREATE TABLE TEMPLATE
	(GROUP_ID VARCHAR(255) NOT NULL,
	ID VARCHAR(255) NOT NULL,
	TEMPLATE CLOB);

ALTER TABLE TEMPLATE ADD PRIMARY KEY (GROUP_ID, ID);
	
CREATE TABLE PERSON
	(ID INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1) NOT NULL PRIMARY KEY,
	USERNAME VARCHAR(40) NOT NULL,
	FIRSTNAME VARCHAR(40),
	LASTNAME VARCHAR(40),
	ORGANIZATION VARCHAR(255),
	EMAIL VARCHAR(255),
	PHONENUMBER VARCHAR(40),
	DESCRIPTION VARCHAR(255),
	LAST_LOGIN TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	GRACE_PERIOD_START TIMESTAMP DEFAULT NULL,
	LOGGED_IN SMALLINT NOT NULL);

CREATE TABLE PERSON_PASSWORD
	(PERSON_ID INTEGER NOT NULL,
	PASSWORD VARCHAR(256) NOT NULL,
	PASSWORD_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT PERSON_ID_PP_FK FOREIGN KEY(PERSON_ID) REFERENCES PERSON(ID) ON DELETE CASCADE);

CREATE TABLE ALERT
	(ID VARCHAR(36) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	IS_ENABLED SMALLINT NOT NULL,
	EXPRESSION CLOB,
	TEMPLATE CLOB,
	SUBJECT VARCHAR(998));

CREATE TABLE CODE_TEMPLATE
	(ID VARCHAR(255) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	CODE_SCOPE VARCHAR(40) NOT NULL,
	CODE_TYPE VARCHAR(40) NOT NULL,
	TOOLTIP VARCHAR(255),
	CODE CLOB);	
	
CREATE TABLE CHANNEL_ALERT
	(CHANNEL_ID VARCHAR(36) NOT NULL,
	ALERT_ID VARCHAR(36) NOT NULL,
	CONSTRAINT ALERT_ID_CA_FK FOREIGN KEY(ALERT_ID) REFERENCES ALERT(ID) ON DELETE CASCADE);

CREATE TABLE ALERT_EMAIL
	(ALERT_ID VARCHAR(36) NOT NULL,
	EMAIL VARCHAR(255) NOT NULL,
	CONSTRAINT ALERT_ID_AE_FK FOREIGN KEY(ALERT_ID) REFERENCES ALERT(ID) ON DELETE CASCADE);

CREATE TABLE CONFIGURATION
	(CATEGORY VARCHAR(255) NOT NULL,
	NAME VARCHAR(255) NOT NULL,
	VALUE CLOB);

CREATE DERBY AGGREGATE GROUP_CONCAT FOR VARCHAR(1024) EXTERNAL NAME 'com.mirth.connect.server.util.DerbyGroupConcat';

INSERT INTO PERSON (USERNAME, LOGGED_IN) VALUES('admin', 0);

INSERT INTO PERSON_PASSWORD (PERSON_ID, PASSWORD) VALUES(1, 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==');

INSERT INTO SCHEMA_INFO (VERSION) VALUES (9);

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'update.url', 'http://updates.mirthcorp.com');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'update.enabled', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'stats.enabled', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'firstlogin', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'server.resetglobalvariables', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.timeout', '5000');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.auth', '0');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.secure', '0');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'server.maxqueuesize', '0');