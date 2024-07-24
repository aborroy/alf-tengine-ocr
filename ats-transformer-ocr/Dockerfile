FROM ubuntu:latest
ARG languages

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y ocrmypdf openjdk-17-jdk

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENV JAVA_OPTS=''

# Install additional Tesseract languages
RUN langs=$(echo $languages | tr "," "\n"); \
    apt-get update; \
    for l in $langs; \
    do \
      sleep 10; \
      apt-get install -y tesseract-ocr-$l; \
    done

# Set default user information
ARG GROUP_NAME=alfresco
ARG GROUP_ID=1001
ARG USER_NAME=alfte
ARG USER_ID=33004

COPY target/*.jar /usr/bin/

RUN groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -G ${GROUP_NAME} ${USER_NAME} && \
    chgrp -R ${GROUP_NAME} /usr/bin/*.jar

EXPOSE 8090

USER ${USER_NAME}

ENTRYPOINT java $JAVA_OPTS -jar /usr/bin/*.jar
