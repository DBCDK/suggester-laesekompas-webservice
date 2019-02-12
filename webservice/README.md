# DBC Microservices - Canonical sample

Exemplifies the structure of a simple micro-service with RESTful resources and database persistence, including:

* Database schema migration using flyway in DatabaseMigrator class.
* Configuration based on the running environment using the config MicroProfile in MicroProfilesResource class.
* Using an Enterprise Java Bean class as a RESTful web resource in MicroProfilesResource class.
* Building a docker image containing the service using a Jenkinsfile.

Not covered by the current state of the implementation

* How to write tests for a service (will be added in the near future).
* How to deploy a service in Kubernetes (will be added in the near future).
* How to enable readiness and liveness checks for a service.