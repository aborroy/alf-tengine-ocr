## Alfresco Transformer from PDF to OCRd PDF

Public Docker Images available in https://hub.docker.com/r/angelborroy/alfresco-tengine-ocr/tags using following command:

```
docker buildx build --platform linux/amd64,linux/arm64 --attest type=sbom \
--attest type=provenance,mode=max -t angelborroy/alfresco-tengine-ocr:2.0.1 . \
--push
```

If you want to build a custom docker images with additional languages, use the `build-arg` named `languages`.

For instance, to include German, French, Spanish and Italian, use following command.

```
$ docker build . -t alfresco-tengine-ocr --build-arg languages=deu,fra,spa,ita
```

If you want to `publish` a Docker Image for multiple architectures use the `buildx` tool:

```
$ docker buildx build --push \
--attest type=sbom --attest type=provenance,mode=max \
--platform=linux/amd64,linux/arm64 \
--build-arg languages=deu,fra,spa,ita \
--tag=angelborroy/alfresco-tengine-ocr:2.0.1-deu-fra-spa-ita .
```

Additionally, before building the Docker Image, modify [application.properties](src/main/resources/application.properties) to include languages available:

```
# Arguments for ocrmypdf invocation
ocrmypdf.arguments=--skip-text -l eng+deu+fra+spa+ita
```