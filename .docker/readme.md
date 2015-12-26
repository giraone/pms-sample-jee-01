# How to run the JEE application using Docker

## Prerequisites
- Install docker
- Get PostgreSQL docker image `docker pull postgres`
- Get WebSphere Liberty (WLP) docker image `docker pull websphere-liberty:javaee7`

## Adapt WLP image to use PostgreSQL JDBC driver and adapted `server.xml`
```
cd pms-sample-jee-01/.docker

docker build --rm -t websphere-liberty-jee7-psql .
```

## Change persistence.xmli

Uncomment line 40 and comment line 38, to create tables

```
<property name="eclipselink.ddl-generation" value="drop-and-create-tables" /> <!-- DROP and CREATE tables -->
```

## Build the war file
```
cd pms-sample-jee-01

mvn -dskipTests=true package
```

## Start the containers

### PostgreSQL
```
cd pms-sample-jee-01/..

docker run \
--name postgresql-pmssample \
-p 5432:5432 \
-v `pwd`/data:/var/lib/postgresql/data \
-e POSTGRES_DB=PmsSample1 \
-e POSTGRES_USER=pms \
-e POSTGRES_PASSWORD=pms \
-d postgres

docker ps
```

### WLP

```
cd pms-sample-jee-01/..

docker run \
--name wlp-pmssample \
--link postgresql-pmssample \
-p 80:9080 -p 443:9443 \
-v `pwd`/logs:/logs \
-v `pwd`/pms-sample-jee-01/target/PmsSample.war:/config/dropins/PmsSample.war \
-d websphere-liberty-jee7-psql

docker ps
```

## Help
```
# Show WLP log files
less logs/messages.log

# Check postgresql environment - this is used in WLP's `server.xml`
docker run -it --link postgresql-pmssample --rm postgres bash -c 'env''

```
