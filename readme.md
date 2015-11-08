# Kickstart 1 for JEE (JAX-RS/EJB/JPA) Web Application #

This project is indented to show the basic modules of a modern REST-based web application with a ***back-end based on core JEE technology***, like JPA, EJB and JAX-RS and a ***front-end based on HTML5/JS/CSS3*** using the "main stream frameworks" *Google Angular 2* and *Twitter Bootstrap*. From a functional perspective, it is designed to be a CRUD application with some more features, than a typical TODO list application with a database at the back-end normally has. The starter project should be easily understandable for everybody and comes with two entities

- Cost center
- Employee

building an owner-member relationship: An employee may have 0 or 1 cost center. A cost center may habe multiple employees. 

**Front-end:** The project comes as a REST-back-end, but the `webapp` folder comes with a release build of the [giraone/pms-sample-jee-01-ajs1](https://github.com/giraone/pms-sample-jee-01-ajs1) GutHub project, so it can be tested together with a browser frontend. The complete solution is hosted [OPENSHIFT](https://www.openshift.com) under [http://pmssamplejee01-giraone.rhcloud.com/].

**Hint:** Parts of the original code are from the [JBoss Forge](http://forge.jboss.org/) scaffolding, which is a nice tool to get JEE applications "*kick started*".

## Goals ##

- Definition of entities and one-to-may relationships using JPA annotations.
- Usage of constraints in JPA, e.g. columns that must be unique, and the handling of duplicate value exceptions.
- The usage of JPA static meta models and criteria API in the entity definition and the Java code of the "business logic". JPA criteria API is perhaps not easy to learn, but it is more robust, due to being type safety and more flexible in code refactoring.
- The usage of *optimistic locking* support from JPA. 
- Creation of JAX-RS endpoints using JAX-RS annotations.
- The usage of *Data Transfer Objects* (*DTOs*) to decouple the "*Boundary*" code from the "*Entity*" code in terms of the *Boundary Control Entity* model (See [http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8](http://www.adam-bien.com/roller/abien/entry/java_ee_7_java_8)).
- Testing the REST endpoints using [REST-assured](https://github.com/jayway/rest-assured) and its "fluent" API.
- The creation of a HTML5/JS/CSS3 user interface based on *Angular 2* and *Boostrap 3*.
- The usage of the REST API from the Angular 2 JavaScript code.

## Prerequisites to develop and run ##
- Maven 3
- An Eclipse based IDE (I used JBoss Dev Studio 9.0)
- A JEE6 compliant application server. In detail, we need:
  - JPA 2.0
  - EJB 3.2
  - JAX-RS 2.0
- I used JBoss EAP 6.4.0. The means in detail:
  - Resteasy for JAX-RS
  - Hibernate for JPA 
- *Apache Derby Network Server 10.X* or *PostgresQL 9.X* as the two tested databases. Others may work also with slight adaptions of the `persistence.xml`.

## Conventions and design decisions of the project ##

### Entity ID generation (surrogate keys) ###
In the sample we use surrogate keys generated with the default JPA method by simply annotating ```@GeneratedValue```. All surrogate keys are named ```oid``` and of type ```Long```.

### Meta Models and SQL Naming ###
To be prepared for refactoring, we use [static meta models](http://docs.oracle.com/javaee/6/tutorial/doc/gjiup.html) and avoid the direct usage of strings in JPA annotations for SQL table and SQL column naming. These SQL names are defined also within the meta model classes as constants. So a typical attribute *myWellDefinedName* in a class *MyWellDefinedClass* looks like:

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

The corresponding static meta model class is

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

### Optimistic locking columns ###
For detecting parallel changes of entities by the users, we use optimistic locking. The column definition are all unique:

```

    @Version
	@Column(name = AbstractEntity_.NAME_versionNumber)
	@NotNull
	private int versionNumber;
```

### Transaction management type ###
Despite the fact, that the standard JEE method is to use container based transaction management (`TransactionManagementType.CONTAINER`), we use `TransactionManagementType.BEAN` in the project. The reason for this, is the usage of **database constraints** in the projects. Constraints on a database level are robust restrictions and cannot by-passed by tools or batch programs operating directly on the database. But with container transaction management, we cannot catch exceptions like `java.sql.SQLIntegrityConstraintViolationException`, which are thrown on `tx.commit()` operations called by the container, e.g. when a new cost center is created with an already existing *identification*. Therefore we do our own transaction management using

```

    @Resource
    UserTransaction tx;
```

### Field Validation ###
Based on *Java Bean Validation* with annotations like this one

```

    @NotNull
	@Size(min = 1, max = 20)
	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed") 
```


## Open issues and TODOs for the current goals ##

- Bean validation together with I18N. Currently we use:
```

@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed")
```

This is not a good idea! Best solution would be to separate the field validation definitions together with their messages.

- Usage of `@XmlRootElement` vs. `@JsonSerialize` to serialize the DTOs. This includes some decisions, e.g.
  - How to serialize date and time values: ISO strings vs. long values?

## Future goals ##

- Using **CDI** instead of EJB (*let's see how much it helps!*)
- Storing **BLOBS**
- Storing attributes of entities, which are not used in queries or joins in *key value tables*, to get a more stable data model without the need of schema changes, when new attributes are needed.
- Additional relationship types for the entities.
- Usage of **Swagger** (or RAML) for the API definition to have a **contract-first approach**.
- Usage of [swagger-js-codegen](https://github.com/wcandillon/swagger-js-codegen) or similar to generate JavaScript/Angular code from the Swagger definition.
- Usage of [swagger-codegen](https://github.com/swagger-api/swagger-codegen) to generate JAX-RS skeletons and the Java DTO classes from the Swagger definition.
- Extension of the REST API by **OData** conventions like $filter, $select and $expand. 
- ...


