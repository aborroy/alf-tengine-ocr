ARG ALFRESCO_TAG
FROM quay.io/alfresco/alfresco-content-repository:${ALFRESCO_TAG}

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

# Restore original user
RUN chown -R ${IMAGEUSERNAME} $TOMCAT_DIR
USER ${IMAGEUSERNAME}
