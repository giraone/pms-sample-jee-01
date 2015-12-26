FROM websphere-liberty:javaee7
COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
RUN mkdir /opt/ibm/wlp/usr/shared/resources/postgresql
COPY postgresql-9.4.1207.jar /opt/ibm/wlp/usr/shared/resources/postgresql
ENV LICENSE accept
