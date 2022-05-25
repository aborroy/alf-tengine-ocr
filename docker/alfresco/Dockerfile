ARG ALFRESCO_TAG
FROM alfresco/alfresco-content-repository-community:${ALFRESCO_TAG}

ARG TOMCAT_DIR=/usr/local/tomcat
ARG IMAGEUSERNAME=alfresco

# default user is alfresco (added on the base image alfresco/alfresco-content-repository-community)
# change to root user to be able to install the addons and packages
USER root

# Install modules and addons
RUN mkdir -p $TOMCAT_DIR/amps
COPY modules/amps $TOMCAT_DIR/amps
COPY modules/jars $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib

RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
    $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force



# DATABASE
ARG DB
ENV DB $DB

# Install mysql JDBC driver
RUN if [ "$DB" == "mariadb" ] ; then \
    set -x \
        && yum install -y wget \
        && yum clean all \
        && wget -P /tmp/ https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.7.4/mariadb-java-client-2.7.4.jar \
        && cp /tmp/mariadb-java-client-2.7.4.jar $TOMCAT_DIR/lib/ \
        && rm -rf /tmp/mariadb-java-client-2.7.4.jar; \
fi



# COMMS
ARG SOLR_COMMS
ENV SOLR_COMMS $SOLR_COMMS

# SSL
ARG TRUSTSTORE_TYPE
ARG TRUSTSTORE_PASS
ARG KEYSTORE_TYPE
ARG KEYSTORE_PASS

ENV TRUSTSTORE_TYPE=$TRUSTSTORE_TYPE \
    TRUSTSTORE_PASS=$TRUSTSTORE_PASS \
    KEYSTORE_TYPE=$KEYSTORE_TYPE \
    KEYSTORE_PASS=$KEYSTORE_PASS

# Enable SSL by adding the proper Connector to server.xml
RUN if [ "$SOLR_COMMS" == "https" ] ; then \
      sed -i "s/\
[[:space:]]\+<\/Engine>/\n\
        <\/Engine>\n\
        <Connector port=\"8443\" protocol=\"HTTP\/1.1\"\n\
            connectionTimeout=\"20000\"\n\
            SSLEnabled=\"true\" maxThreads=\"150\" scheme=\"https\" clientAuth=\"want\" sslProtocol=\"TLS\" sslEnabledProtocols=\"TLSv1.2\"\n\
            keystoreFile=\"\/usr\/local\/tomcat\/keystore\/ssl.keystore\"\n\
            keystorePass=\"${KEYSTORE_PASS}\" keystoreType=\"${KEYSTORE_TYPE}\" secure=\"true\"\n\
            truststoreFile=\"\/usr\/local\/tomcat\/keystore\/ssl.truststore\"\n\
            truststorePass=\"${TRUSTSTORE_PASS}\" truststoreType=\"${TRUSTSTORE_TYPE}\">\n\
        <\/Connector>/g" ${TOMCAT_DIR}/conf/server.xml; \
    fi




# Restore original user
RUN chown -R ${IMAGEUSERNAME} $TOMCAT_DIR
USER ${IMAGEUSERNAME}

