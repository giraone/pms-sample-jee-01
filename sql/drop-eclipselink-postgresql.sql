ALTER TABLE PostalAddress DROP CONSTRAINT FK_PostalAddress_employeeId
ALTER TABLE EmployeeProperties DROP CONSTRAINT FK_EmployeeProperties_parentId
ALTER TABLE Employee DROP CONSTRAINT FK_Employee_COSTCENTER_oid

DROP TABLE PostalAddress CASCADE
DROP TABLE EmployeeProperties CASCADE
DROP TABLE EmployeeDocument CASCADE
DROP TABLE CostCenter CASCADE
DROP TABLE Employee CASCADE

DELETE FROM SEQUENCE WHERE SEQ_NAME = 'SEQ_GEN'