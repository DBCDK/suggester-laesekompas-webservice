# Læsekompas Suggester and Search Webservice

Suggester and Search webservice for læsekompas/MinNæsteBog, talks to the SolR and do a little bit of formatting of the SolR response.

## Building

### Building `.war`

```bash
mvn clean verify
```

### Building docker image

Make sure to build the project with maven first

```bash
cd webservice
docker build -t laesekompas-webservice -f target/docker/Dockerfile .
```

### Running docker image

When the docker image is built, you can run it with a command looking like the following. Feel free to alter environments as you see fit. Most of the environment variables here are documented in ```Dockerfile```, the rest only control how much output is written to the console, and how it is formatted.

```bash
docker run -it -e JAVA_MAX_HEAP_SIZE=1G -e LOG_FORMAT=text -e COREPO_SOLR_URL=http://cisterne.solr.dbc.dk:8983/ -e SUGGESTER_SOLR_URL=http://laesekompas-suggester-laesekompas-solr-3-service.os-externals-staging.svc.cloud.dbc.dk:8983 -e SOLR_APPID=laesekompas-solr-appId  -e LOG_LEVEL=debug -p 8080:8080 laesekompas-webservice
```

### Calling the web service

When the docker image is up and running as described above, you can invoke a URL like the following to call the ```search``` endpoint - you can use your browser or ```curl``` for this as you wish:

```
http://localhost:8080/api/search?query=eventyr&branch_id=784600%2FMariager&filter_status=true
```

You can also invoke the ```suggest``` part of the service with something like ```http://localhost:8080/api/suggest?query=eventy``` - or you can consult the documentation that should be available on ```http://localhost:8080/```.