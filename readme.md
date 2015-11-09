# Kickstart 1 for JEE (JAX-RS/EJB/JPA) Web Application #

This project is indented to show the basic modules of a modern REST-based web application with a ***back-end based on core JEE technology***, like JPA, EJB and JAX-RS and a ***front-end based on HTML5/JS/CSS3*** using the "main stream frameworks" *Google Angular 2* and *Twitter Bootstrap*. From a functional perspective, it is designed to be a CRUD application with some more features, than a typical TODO list application normally has. The starter project should be easily understandable for everybody and comes with only two entities

- Cost center
- Employee

building an owner-member relationship. An employee entity is related to 0 or 1 cost center. From the perspective of the cost center it is a one-to-may relationship. 

**Back-end:** This GitHub projects is the back-end application. It exposes a REST based API under `/api/costcenters` and `api/employees` with GET, POST, PUT and DELETE HTTP verbs. But it also hold a `webapp` folder, which comes with a release build of the **Front-end:** (see below), so it can be tested together with a browser front-end.

**Front-end:** The front-end is a separate [GitHub project giraone/pms-sample-jee-01-ajs1](https://github.com/giraone/pms-sample-jee-01-ajs1)

The complete solution is hosted on [RedHat's OPENSHIFT](https://www.openshift.com) under the URL [http://pmssamplejee01-giraone.rhcloud.com/PmsSample].

**Hint:** Parts of the code are from the [JBoss Forge](http://forge.jboss.org/) scaffolding, which is a nice tool to get JEE applications "*kick started*".

## Goals of this project ##

- Definition of entities and one-to-may relationships using *JPA annotations*.
- Usage of constraints in JPA, e.g. definition of unique columns and the handling of duplicate value exceptions.
- The usage of *JPA static meta models* and *JPA Criteria API* in the entity definition and the Java code of the *"business logic"*. JPA criteria API is perhaps not easy to learn, but it is more robust, due to being type safe and more flexible in code refactorings.
- The usage of *optimistic locking* support from JPA. 
- Creation of JAX-RS end-points using JAX-RS annotations.
- The usage of *Data Transfer Objects* (*DTOs*) to decouple the "*Boundary*" code from the "*Entity*" code in terms of the *Boundary Control Entity* model (See [http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8](http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8)).
- Testing the REST end-points using [REST-assured](https://github.com/jayway/rest-assured) and its "fluent" API.
- The creation of a HTML5/JS/CSS3 user interface based on *Angular 2* and *Boostrap 3*.
- The usage of the REST API from the Angular 2 JavaScript code.

## Prerequisites to develop and run the project ##
- Maven 3
- An Eclipse based IDE (I used JBoss Dev Studio 9.0)
- A JEE6 compliant application server. In detail, we need:
  - JPA 2.0
  - EJB 3.2
  - JAX-RS 2.0
- I used *JBoss EAP 6.4.0* and *JBoss Wildfly 9.1* with [Resteasy](http://resteasy.jboss.org/) for JAX-RS and [Hibernate](http://hibernate.org/orm/) for JPA. But it shouldrun on any other JEE6 application server - the only dependencies in the source code for JBoss are in [blob/master/pom.xml] and [persistence.xml](blob/master/src/main/resources/META-INF/persistence.xml).
- A relational database supported by the JPA implementation. I used *Apache Derby Network Server 10.X* and *PostgresQL 9.4*. Others may work also with slight adoptions to [persistence.xml](blob/master/src/main/resources/META-INF/persistence.xml).


----------


## Conventions and design decisions in the project ##

### Entity ID generation (surrogate keys) ###
In the project we use surrogate keys generated with the default JPA method by simply annotating ```@GeneratedValue```. All surrogate keys are named ```oid``` and of the type ```Long```.

### Meta Models and SQL Naming ###
To be prepared for refactoring, we use [JPA static meta models](http://docs.oracle.com/javaee/6/tutorial/doc/gjiup.html). We avoid also the direct usage of strings in JPA annotations for SQL table and SQL column naming. These SQL names are also defined within the meta model classes as Java constants. So a typical string attribute *myWellDefinedName* in a class *MyWellDefinedClass* should look like:

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

### Optimistic locking detection ###
For detecting parallel changes of entities by users of the API, we use plain vanilla JPA optimistic locking columns named *versionNumber*. The column definitions are unique in all entities:

```

    @Version
	@Column(name = AbstractEntity_.NAME_versionNumber)
	@NotNull
	private int versionNumber;
```

These version numbers are also exposed at the REST APIs!

### Transaction management type ###
Despite the fact, that the standard JEE method is to use *container based transaction management* (`TransactionManagementType.CONTAINER`), we use `TransactionManagementType.BEAN` in the project. The reason for this, is the usage of the mentioned **database constraints** in the projects. Constraints on a database level are robust restrictions and cannot by-passed by tools or batch programs operating directly on the database. They are a **MUST-HAVE**! But with container transaction management, we cannot catch exceptions like `java.sql.SQLIntegrityConstraintViolationException`, thrown on `tx.commit()` operations called by the container, e.g. when a new cost center is created with an already existing *identification*. Therefore we do our own transaction management using

```

    @Resource
    UserTransaction tx;
```

If somebody has a better idea to solve this, let us know!

### Field Validation ###
Field validation at the JPA level is based on [Java Bean Validation (JSR 303)](http://beanvalidation.org/) with Java annotations on the attribute level like this one

```

    @NotNull
	@Size(min = 1, max = 20)
	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed") 
```


----------


## Open issues and TODOs for the current goals ##

- The unit tests with *REST-assured* aren't currently real unit tests - they need a server with a well known configuration. See [TestPmsCoreApi](blob/master/src/com/giraone/samples/pmspoc1/boundary/test/TestPmsCoreApi.java) in the source to see, what I mean.

- Bean validation together with I18N. Currently we use sth. like:

```

	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed")

```

This is not a good idea together with I18N! Best solution would be to separate the field validation definitions together with their messages in separate configurations.

- Usage of `@XmlRootElement` vs. `@JsonSerialize` to serialize the JAX-RS DTOs. This includes some decisions, e.g.
  - How to serialize date and time values: ISO strings vs. long values?

## Future goals of the project ##

- Using **CDI** instead of EJB (*let's see how much it helps!*)
- Storing **BLOBS**, e.g. an image for each *employee*.
- Storing attributes of entities, which are not used in queries or joins in ***key value tables***, to get a more stable data model without the need of schema changes, when new attributes are needed.
- Additional relationship types for the entities.
- Usage of **Swagger** (or RAML) for the API definition to have a **contract-first approach**.
- Usage of [swagger-js-codegen](https://github.com/wcandillon/swagger-js-codegen) or similar to generate JavaScript/Angular code from the Swagger definition.
- Usage of [swagger-codegen](https://github.com/swagger-api/swagger-codegen) to generate JAX-RS skeletons and the Java DTO classes from the Swagger definition.
- Extension of the REST API by **OData** conventions like $filter, $select and $expand.
- Catalog tables for offering typically **lookup tables**, like ISO country codes, ZIP codes, ... 

