# DBC Microservices - samples

Suggester webservice for læsekompas/MinNæsteBog, talks to the SolR and do a little bit of formatting of the SolR response.

## Building

### Building `.war`

```bash
mvn clean verify
```

### Building docker image

Make sure to build the project with maven first

```bash
cd canonical
docker build -f target/docker/Dockerfile .
```

