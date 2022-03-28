## Alfresco Transformer from PDF to OCRd PDF

Public Docker Images available in https://hub.docker.com/r/angelborroy/alfresco-tengine-ocr/tags

If you want to build a custom docker images with additional languages, use the `build-arg` named `languages`.

For instance, to include German, French, Spanish and Italian, use following command.

```
$ docker build . -t alfresco-tengine-ocr --build-arg languages=deu,fra,spa,ita
```
