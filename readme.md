# Kickstart 1 for JEE (JAX-RS/EJB/JPA) Web Application #

This project is indented to show the basic parts of a modern REST-based web application with a ***back-end based on core JEE technology***, like JPA, EJB and JAX-RS and a ***front-end based on HTML5/JS/CSS3*** using the "main stream frameworks" *Google Angular 2* and *Twitter Bootstrap*. From a functional perspective, it is designed to be a CRUD application with some more features, than a typical todo list application normally has. The starter project should be easily understandable for everybody and comes with only four entities

- Cost centers
- Employees
- Postal addresses of an employee
- Documents (BLOBs) of an employee

An employee entity is related to 0 or 1 cost center. An employee may have multiple postal addresses. Addresses cannot live without an employee and are deleted together with the employee. At the database level, the employee data is stored in two tables. One table holds the basic and important attributes in columns, the other tables is a key value store. See *Storage of data attributes* section below for more information. The document entity is mainly to show how BLOBs can be handled using JAX/RS and JPA.

**Back-end:** This GitHub projects is the main back-end service module. It exposes a REST API under `/api/costcenters` and `api/employees` with GET, POST, PUT and DELETE HTTP verbs. But it also holds a `webapp` folder, which comes with a release build of the **Front-end:** (see below), so it can be deployed as a single war file. Additionally, there is a 2nd back-end micro service for providing catalog data (**lookup tables**) to the front-end. See [GitHub project giraone/catalog-jee-01](https://github.com/giraone/catalog-jee-01) for details. At the moment this service is optional and delivers only a list of ISO country codes and the corresponding country names in English and German language.

**Front-end:** The front-end is a separate [GitHub project giraone/pms-sample-jee-01-ajs1](https://github.com/giraone/pms-sample-jee-01-ajs1)

**Test project** There is an additional test project which uses [REST-assured](https://github.com/jayway/rest-assured) to test the backe-end's REST API.


The running solution is hosted currently on 3 PaaS provider sites:

- [IBM's BLUEMIX PaaS](https://www.bluemix.net) under the URL [http://pmssamplejee1.eu-gb.mybluemix.net/].
- [Pivotal's Web Services PaaS](https://run.pivotal.io) under the URL [http://pmssamplejee1.cfapps.io/].
- [RedHat's OPENSHIFT PaaS](https://www.openshift.com) under the URL [http://pmssamplejee01-giraone.rhcloud.com/PmsSample] - Not the latest version!

## Goals of this project ##

- Definition of entities and one-to-may relationships using *JPA annotations*.
- Usage of constraints in JPA, e.g. definition of unique columns and the handling of duplicate value exceptions.
- The usage of *JPA static meta models* and *JPA Criteria API* in the entity definition and the Java code of the *"business logic"*. JPA criteria API is perhaps not easy to learn, but it is more robust, due to being type safe and more flexible in code refactorings.
- The usage of *optimistic locking* support from JPA. 
- Creation and implementation of JAX-RS end-points using JAX-RS annotations.
- A general internal project separation (package naming conventions) based on the *Boundary Control Entity* model (See [http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8](http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8)).
- The usage of [OData style query options](http://www.odata.org/documentation/odata-version-2-0/uri-conventions/) for filtering and sorting data in JAX-RS endpoints for list data.
- The usage of *Data Transfer Objects* (*DTOs*) to decouple the "*Boundary*" code from the "*Entity*" code.
- The usage of *DTO* from/to *Entity* mapping techniques to simplify this transfer.
- The creation of a HTML5/JS/CSS3 user interface based on *AngularJS 1* and *Boostrap 3*.
- The usage of REST API from the *AngularJS 1* JavaScript code.

## Prerequisites to develop locally and run the project ##
- Maven 3
- An Eclipse based IDE
- A JEE7 compliant application server. In detail, you need:
  - JPA 2.0
  - EJB 3.2
  - JAX-RS 2.0
  For the local development *JBoss Wildfly* and *WebSphere Liberty* was used. But the application should run on any other JEE6/JEE7 application server - the only implementation specific dependencies in the source code are in [pom.xml](pom.xml) and [persistence.xml](src/main/resources/META-INF/persistence.xml).
- A relational database supported by the JPA implementation. For the local development *Apache Derby Network Server* and *PostgresQL* was used. Others databases may work also with adoptions to [persistence.xml](src/main/resources/META-INF/persistence.xml).
- In the hosting environment the following components are used:

|               | OPENSHIFT           | BLUEMIX                       | PIVOTAL                       |
|:--------------|:-------------------:|:-----------------------------:|:-----------------------------:|
| JEE container | JBoss Wildfly 9.0.1 | IBM WebSphere Liberty 8.5.5.8 | IBM WebSphere Liberty 8.5.5.7 |
| Database      | PostgreSQL 9.2      | IBM DB2                       | ElephantSQL (PostgreSQL 9.4.4)|
| JPA provider  | Hibernate           | EclipseLink                   | EclipseLink                   |
| JAX-RS/JSON   | Resteasy/Jersey     | CXF/Jackson                   | CXF/Jackson                   |
  
- In local development environments the code was tested with:
  - JEE container:
    - JBoss EAP 6.4 (snippets from standalone.xml see .jboss folder)
    - JBoss Wildfly 9.0.1 (snippets from standalone.xml see .jboss folder)
    - IBM WLP Full Profile 8.5.5.7 (server.xml see .wlp folder)
  - Database:
    - Derby Network Server 10
    - PostgreSQL 9.4.5


## Conventions and design decisions in the project ##

### Logging and logging framework ###
In the project [Apache Log4j 2](http://logging.apache.org/log4j/2.x/) is used as the logging framework. Despite the fact, that Java has a basic logging implementation in `java.util.logging`, most developers are used to the *log4J* or *slf4j* style logging API syntax. Log4j2 is a modern framework (concurrency features, possibilities for asynchronous logging, markers) and therefore this it is our chosen logging API. The loggers are always injected by CDI (`@Inject`). In the log statement *Markers* are used, to be able to filter the logs lines later. Additionally interceptors (`@AroundInvoke`) are used to achieve method logging without boilerplate code at certains layers, e.g. for all REST end points.

### Entity ID generation (surrogate keys) ###
In the project surrogate keys are generated for all entities with the default JPA method by simply annotating ```@GeneratedValue```. All used surrogate keys are named ```oid``` and are of the Java type ```Long```.

### Meta Models and SQL Naming ###
To be prepared for refactoring, [JPA static meta models](http://docs.oracle.com/javaee/6/tutorial/doc/gjiup.html) are used. The direct usage of strings in JPA annotations for SQL table and SQL column naming is also avoided. These SQL names are defined also within the meta model classes as Java constants. So a typical string attribute *myWellDefinedName* in a class *MyWellDefinedClass* should look like:

```
	@Entity
	@Table(name = MyWellDefinedClass_.SQLNAME)
	public class MyWellDefinedClass
	{ 
		@Column(name = MyWellDefinedClass_.SQLNAME_myWellDefinedName)
		private String myWellDefinedName;
		...
	}
```

The corresponding ***static meta model class*** *MyWellDefinedClass_* is

```
	@Static Metamodel(MyWellDefinedClass.class)
	public class MyWellDefinedClass_
	{ 
		public static volatile SingularAttribute<MyWellDefinedClass, String> myWellDefinedName;
		...
		public static final String SQLNAME = "MyWellDefinedClass";
		...
		public static final String SQLNAME_myWellDefinedName = "myWellDefinedName";
		...
	}
```

### Optimistic lock detection ###
For detecting parallel changes of entities by users of the API, I used plain vanilla JPA optimistic locking columns named *versionNumber*. The column definitions are unique in all entities:

```
    @Version
	@Column(name = AbstractEntity_.NAME_versionNumber)
	@NotNull
	private int versionNumber;
```

These version numbers are also exposed at the REST APIs!

### Transaction management type ###
Despite the fact, that the standard JEE method is to use *container based transaction management* (`TransactionManagementType.CONTAINER`), I use `TransactionManagementType.BEAN` in the project. The reason for this, is the usage of the mentioned **database constraints** in the projects. Constraints on a database level are robust restrictions and cannot by-passed by tools or batch programs operating directly on the database. They are a **MUST-HAVE**! But with container transaction management, one cannot catch exceptions like `java.sql.SQLIntegrityConstraintViolationException`, thrown on `tx.commit()` operations called by the container, e.g. when a new cost center is created with an already existing *identification*. Therefore I do my own transaction management using user transactions. To avoid boilerplate code, I used `@UserTransactional` annotations (implementation details see `com.giraone.samples.common.entity.UserTransactionInterceptor`) for methods, that need transaction handling and constraint violation detection.

If somebody has a better idea to solve this, let me know!

### Field Validation ###
Field validation at the JPA level is based on [Java Bean Validation (JSR 303)](http://beanvalidation.org/) with Java annotations on the attribute level like this one

```
    @NotNull
	@Size(min = 1, max = 20)
	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed") 
```

### Storage of data attributes ###
To have the flexibility of schema-less noSQL database also in relational storage, I use two different storage approaches for attributes of entities:

- The important attributes, like primary keys, foreign keys, important query parameters and import sort attributes are store as normal SQL columns.
- All other attributes are stored in ***key value tables***. There is one key value table per entity. If the name of
the table is Xyz, the key value table is named `MyWellDefined`, the key value table is named `MyWellDefinedProperty`. the key value table is type safety to some extend. It distinguishes between numeric data, temporal data (date/time) and string data, to use the databases sort and comparison operators for these data types. This approach is cloned from the approaches used by [Salesforce](https://developer.salesforce.com/page/Multi_Tenant_Architecture) and [Workday](http://www.dbms2.com/2010/08/22/workday-technology-stack/), but it is not yet such revolutionary - it uses a normal database layout for the key attributes and for relationships. But generally this leads to a more stable data model without the need of schema changes, when new attributes are needed.

Currently this approach is used only for the "Employee" entity.

### BLOB storage and BLOB handling within the REST API ###

The ***EmployeeDocument*** entity is defined using JPA. But for storing and retrieving the BLOBs lower level JDBC methods are used. The reason for this is the lack of a good BLOB vendor-independent streaming support in JPA. As far as I was able to find out, there are only proprietary solutions for Hibernate and OpenJPA available. Therefore I decided to go with a JDBC based streaming solution. See `com.giraone.samples.pmspoc1.boundary.blobs.BlobManager` for the code.

Within the REST API, BLOB content must be uploaded without ***multipart/form-data*** support. A document resource is created using its meta data and a POST request. The BLOB content must then be uploaded using the location URL from the POST request by a plain PUT request with a binary content type. 

### DTOs and Mapping ###

Despite the fact, that this is currently a CRUD application, JPA entities are not used at the REST interfaces. I use Data Transfer Objects (DTOs). These DTOs are currently transformed using the brand new [MapStruct](http://mapstruct.org/) code generator and its Maven plugin. This approach looks much more promising, than using reflection based bean mappers like [Dozer](http://dozer.sourceforge.net/). 

### REST APIs ###

The base for the REST services is plain vanilla *JAX-RS*. Currently with an annotation first approach - no contract first approach yet! For cross-cutting features at the REST layer, like injecting a `"Access-Control-Allow-Origin"` header, I use the JAX-RS 2.0 feature `@Provider`.

### OData style query options for filtering and sorting ###

The base for the implementation of OData $filter and $orderby query options in REST APIs for lists is [Apache Olingo](https://olingo.apache.org/) - currently the only useful Java-based OData implementation. For the current basic requirements OData V2.0 seems to be *good enough*.

----------

## Open issues and TODOs for the current goals ##

- Performance checks for the ID generation are needed. It may be better to use a specific kind of generation, e.g.
```
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "Alloc100")
	@TableGenerator(name = "Alloc100", allocationSize = 100)
```

- Currently the solution must use *olingo-odata2-core 2.0.6-SNAPSHOT* in *JBoss environments*, because of this bug: [OLINGO-761](https://issues.apache.org/jira/browse/OLINGO-761?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel)

- OData filtering (lt, gt) for Date fields is not yet implemented

- Handling of NULL values (e.g. null vs. 0 for nrOfChildren in employee data) is not correct.

- Bean validation together with I18N is not yet perfect. Currently the code uses sth. like:

```
	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed")
```

This is not a good idea together with I18N! Best solution would be to separate the field validation definitions together with their messages in separate configurations.

- Usage of `@XmlRootElement` vs. `@JsonSerialize` to serialize the JAX-RS DTOs is an open issues. This includes some decisions, e.g. how to serialize date and time values: ISO strings vs. long values? This should be addressed together with the contract-first approach.

- Logging
  - There is only a simple Log4J 2 configuration yet.
  
- Build/Deploy improvements
  - For the different environments, Maven profiles should be used in the [pom.xml](pom.xml).

- Test improvement
  - There are no unit tests for the OData implementation yet. There is only a test page `webapp/test/index.html`.
  
## Future goals of the project ##

- Extension of the REST API by using **OData** `$expand`. E.g. an expand parameter for employee to show the cost center's description.
- Additional relationship types for the entities.
- Usage of **Swagger** (or **RAML**) for the REST API definition to have a **contract-first approach**.
- Usage of [swagger-js-codegen](https://github.com/wcandillon/swagger-js-codegen) or similar to generate JavaScript/Angular code from the Swagger definition.
- Usage of [swagger-codegen](https://github.com/swagger-api/swagger-codegen) to generate JAX-RS skeletons and the Java DTO classes from the Swagger definition.
- Improved use of catalog tables (**lookup tables**) in the address form for fetching cities, when their postal codes are entered.

