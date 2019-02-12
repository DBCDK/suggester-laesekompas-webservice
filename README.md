# DBC Microservices - samples

* canonical - simple sample using JAX-RS 2.1 / JPA 2.2 / MicroProfile 1.4

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

## Making it your own; checklist

 - Change `dockerRepository` variable to the URL of our scrum team, on line 2 in `Jenkinsfile`
 - Change artifact id of `pom.xml` and `canonical/pom.xml` to your needs
 - Change package path in `canonical/src/java`
 - Change `canonical` folder name
 - Change `appName` in `src/main/docker/reference-app.json`
