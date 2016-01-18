CREATE TABLE PostalAddress
(
 oid BIGINT NOT NULL,
 city VARCHAR(128),
 countryCode VARCHAR(3) NOT NULL,
 houseNumber VARCHAR(128),
 poBoxNumber VARCHAR(128),
 postalCode VARCHAR(64),
 ranking INTEGER NOT NULL,
 secondaryAddressLine VARCHAR(128),
 street VARCHAR(128),
 versionNumber INTEGER,
 employeeId BIGINT NOT NULL,
 PRIMARY KEY (oid)
)

CREATE TABLE EmployeeProperties
(
 id BIGINT NOT NULL,
 name VARCHAR(128) NOT NULL,
 type VARCHAR(2) NOT NULL,
 typeModifier VARCHAR(256),
 valueNumber BIGINT,
 valueString VARCHAR(256),
 valueTimestamp TIMESTAMP,
 parentId BIGINT NOT NULL,
 PRIMARY KEY (id)
)
CREATE TABLE EmployeeDocument
(
 oid BIGINT NOT NULL,
 businessType VARCHAR(128) NOT NULL,
 documentBytes BYTEA,
 bytesSize BIGINT NOT NULL,
 mimeType VARCHAR(128) NOT NULL,
 publishingDate DATE,
 versionNumber INTEGER,
 PRIMARY KEY (oid)
)

CREATE TABLE CostCenter
(
 oid BIGINT NOT NULL,
 description VARCHAR(256) NOT NULL,
 identification VARCHAR(20) NOT NULL UNIQUE,
 versionNumber INTEGER,
 PRIMARY KEY (oid)
)

CREATE TABLE Employee
(
 oid BIGINT NOT NULL,
 dateOfBirth DATE,
 firstName VARCHAR(256) NOT NULL,
 gender VARCHAR(1) NOT NULL,
 lastName VARCHAR(256) NOT NULL,
 personnelNumber VARCHAR(20) NOT NULL UNIQUE,
 versionNumber INTEGER,
 COSTCENTER_oid BIGINT,
 PRIMARY KEY (oid)
)

ALTER TABLE PostalAddress ADD CONSTRAINT FK_PostalAddress_employeeId FOREIGN KEY (employeeId) REFERENCES Employee (oid)

ALTER TABLE EmployeeProperties ADD CONSTRAINT FK_EmployeeProperties_parentId FOREIGN KEY (parentId) REFERENCES Employee (oid)

ALTER TABLE Employee ADD CONSTRAINT FK_Employee_COSTCENTER_oid FOREIGN KEY (COSTCENTER_oid) REFERENCES CostCenter (oid)

CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT DECIMAL(38), PRIMARY KEY (SEQ_NAME))

INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SEQ_GEN', 0)
