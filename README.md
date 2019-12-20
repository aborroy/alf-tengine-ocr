## Alfresco Transformer from PDF to OCRd PDF

This project includes a sample Transformer for Alfresco from PDF to OCRd PDF to be used with ACS Community 6.2+

The Transformer `ats-transformer-ocr` uses the new Local Transform API, that allows to register a Spring Boot Application as a local transformation service.

## Build Docker Image for ATS Transformer Markdown

Building the ATS Transformer Markdown Docker Image is required before running the Docker Compose template provided.

```
$ cd ats-transformer-ocr

$ mvn clean package

$ docker build . -t alfresco/alfresco-transformer-ocr
```

## Starting

```
$ docker run -p 8090:8090 -t alfresco/alfresco-transformer-ocr:latest
```

## Testing

A sample web page has been created in order to test the transformer is working:

http://localhost:8090
