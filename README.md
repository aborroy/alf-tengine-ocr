## Alfresco Transformer from PDF to OCRd PDF

This project includes a simple Transformer for Alfresco from PDF to OCRd PDF to be used with **ACS Community 7.0+**

>> OCR Transformation is performed by [ocrmypdf](https://ocrmypdf.readthedocs.io/en/latest/), a wrapper of [Tesseract](https://github.com/tesseract-ocr/tesseract) that includes additional features in order to improve the accuracy of the process.

The Transformer `ats-transformer-ocr` uses the new Alfresco Local Transform API, that allows to register a Spring Boot Application as a local transformation service.

The folder `embed-metadata-action` includes an Alfresco Repository Addon that enables the action `embed-metadata` in Folder Rule feature.

**ACS Community 7.4 or later** requires modifying default configuration for HTTP requests timeouts. Increase default values (5000 ms / 5 s) to a larger value, like in the following sample that uses 500000 ms / 500 s

```
httpclient.config.transform.socketTimeout=500000
httpclient.config.transform.connectionRequestTimeout=500000
httpclient.config.transform.connectionTimeout=500000
```

## Local testing

### Build Docker Image for Alfresco OCR Transformer

Building the Alfresco OCR Transformer Docker Image is required before running the Docker Compose template provided.

```
$ cd ats-transformer-ocr

$ mvn clean package
```

Maven will create a Docker Image named `alfresco/tengine-ocr:latest`

### Starting

```
$ docker run -p 8090:8090 alfresco/tengine-ocr:latest
```

### Testing

A sample web page has been created in order to test the transformer is working:

http://localhost:8090


## Deployment with ACS Stack

### Obtaining Repository Addon to enable Embed Metadata Action

Before deploying Alfresco OCR Transformer, `embed-metadata-action` Repository Addon should be built.

```
$ cd embed-metadata-action

$ mvn clean package

$ ls target/embed-metadata-action-1.0.0.jar
target/embed-metadata-action-1.0.0.jar
```

Alternatively `embed-metadata-action-1.0.0.jar` can be download from [Releases](https://github.com/aborroy/alf-tengine-ocr/releases/download/1.0.0/embed-metadata-action-1.0.0.jar)

### Deploying Repository Addon to enable Embed Metadata Action

Use some of the available alternatives to deploy `embed-metadata-action-1.0.0.jar` in alfresco service, like adding the JAR to `alfresco/modules/jar` folder when using [Alfresco Docker Installer](https://github.com/alfresco/alfresco-docker-installer) tool.

### Adding Alfresco OCR Transformer to Docker Compose (Local Transformer - HTTP) - Community Edition

Review that the following configuration is applied to `docker-compose.yml` file.

```
services:
    alfresco:
        environment:
            JAVA_OPTS : "
                -DlocalTransform.core-aio.url=http://transform-core-aio:8090/
                -DlocalTransform.ocr.url=http://transform-ocr:8090/
            "

    transform-core-aio:
        image: alfresco/alfresco-transform-core-aio:2.3.10
        mem_limit: 1536m
        environment:
            JAVA_OPTS: " -XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80"

    transform-ocr:
        image: alfresco/tengine-ocr:latest
        mem_limit: 1536m
        environment:
            JAVA_OPTS: " -XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80"
```

* Include the `localTransform` URL for OCR Transformer in `alfresco` Docker Container, http://transform-ocr:8090/ by default
* Declare the new `transform-ocr` Docker Container

>> Remember that you need to build Docker Image for `alfresco/tengine-ocr` before running this composition

Start ACS Stack from folder containing `docker-compose.yml` file.

```
$ docker-compose up --build --force-recreate
```

Sample deployment is available in [docker](docker) folder.


### Adding Alfresco OCR Transformer to Docker Compose (Async Transformer - ActiveMQ) - Enterprise Edition

Review that the following configuration is applied to `docker-compose.yml` file.

```
services:
    alfresco:
        environment:
            JAVA_OPTS : "
              -Dlocal.transform.service.enabled=true
              -Dtransform.service.enabled=true
              -Dtransform.service.url=http://transform-router:8095
              -Dsfs.url=http://shared-file-store:8099/
            "

    transform-router:
      image: quay.io/alfresco/alfresco-transform-router:${TRANSFORM_ROUTER_TAG}
      environment:
        JAVA_OPTS: " -XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80"
        ACTIVEMQ_URL: "nio://activemq:61616"
        CORE_AIO_URL: "http://transform-core-aio:8090"
        TRANSFORMER_URL_OCR: "http://transform-ocr:8090"
        TRANSFORMER_QUEUE_OCR: "ocr-engine-queue"
        FILE_STORE_URL: "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file"

    transform-ocr:
      image: alfresco/tengine-ocr:latest
      mem_limit: 1536m
      environment:
        JAVA_OPTS: " -XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80 
		  -Docrmypdf.path=ocrmypdf -Docrmypdf.arguments=--skip-text -Dqueue.engineRequestQueue=ocr-engine-queue
		 "
        ACTIVEMQ_URL: "nio://activemq:61616"
        FILE_STORE_URL: "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file"
```

* You can optionally disable `local.transform` service in `alfresco` Docker Container and enable `transform` service (asynchronous). Local Transform Service or Transform Service (supports only asynchronous requests) can be enabled or disabled independently of each other. Please keep in mind that when your deployment has Share and SOLR (think of full text indexing), or both then you'll need to have `local.transform` and `transform` service (asynchronous) enabled and running. The Repository will try to transform content using the Transform Service via the T-Router if possible and fall back to direct Local Transform Service. Share makes use of both, so functionality such as preview will be unavailable if `local.transform` service is disabled.
* Add OCR Transformer configuration to `transform-router` Docker Container: URL (http://transform-ocr:8090/ by default) and Queue Name (`ocr-engine-queue` as declared in [ats-transformer-ocr/src/main/resources/application-default.yaml](ats-transformer-ocr/src/main/resources/application-default.yaml))
* Declare the new `transform-ocr` Docker Container using the ActiveMQ and Shared File services

>> Remember that you need to build Docker Image for `alfresco/tengine-ocr` before running this composition

Start ACS Stack from folder containing `docker-compose.yml` file.

```
$ docker-compose up --build --force-recreate
```

Sample deployment is available in [docker-enterprise](docker-enterprise) folder.


### Defining the OCR Rule in Alfresco Share

Use your browser to access to Alfresco Share App (by default available in http://localhost:8080/share/)

Create a folder and add following rule (`Manage Rules` folder option):

* When: Items are created or enter this folder
* If all criteria are met: Mimetype is 'Adobe PDF Document'
* Perform Action: Embed properties as metadata in content

>> To limit the amount of parallel OCR processing threads, use the **Run rule in background** checkbox.

From that point, every PDF File uploaded to the folder will be OCRd. Original version for the PDF file will remain as 1.0 version, while the one with text layer on it will be labeled as 1.1 version.

## Customizing ocrmypdf arguments

By default, Alfresco OCR Transformer is providing following `ocrmypdf` configuration.

```
# Executable command for ocrmypdf program
ocrmypdf.path=ocrmypdf

# Arguments for ocrmypdf invocation. This is the optimized option. 
# If --skip-text is issued, then no image processing or OCR will be performed on pages that already have text.
ocrmypdf.arguments=--skip-text

# To force OCR, use the following:
ocrmypdf.arguments=--force-ocr
```   

Configuration can be changed by using Docker environment variables from command line.

```
$ docker run -p 8090:8090 -e OCRMYPDF_ARGUMENTS='--skip-text -l eng' alfresco/tengine-ocr:latest
```

Or with the equivalent notation in `docker-compose.yml`

```
transform-ocr:
    image: alfresco/tengine-ocr:latest
    mem_limit: 1536m
    environment:
      JAVA_OPTS: "-XX:MinRAMPercentage=50 -XX:MaxRAMPercentage=80 -Dqueue.engineRequestQueue=ocr-engine-queue"
      OCRMYPDF_ARGUMENTS: "--skip-text -l eng"
```

## Additional contributors

* Thanks to [dgradecak](https://github.com/dgradecak) for the `embed-metadata` action approach: https://github.com/aborroy/alf-tengine-ocr/pull/2
