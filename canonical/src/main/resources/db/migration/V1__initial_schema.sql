CREATE TABLE micro_profile (
  id          SERIAL PRIMARY KEY,
  name        TEXT UNIQUE NOT NULL,
  source_repo TEXT NOT NULL,
  description TEXT
);

INSERT INTO micro_profile(name, source_repo, description)
VALUES('config', 'https://github.com/eclipse/microprofile-config', 'The majority of applications need to be configured based on a running environment. It must be possible to modify configuration data from outside an application so that the application itself does not need to be repackaged. The configuration data can come from different locations and in different formats (e.g. system properties, system environment variables, .properties, .xml, datasource). We call these config locations ConfigSources. If the same property is defined in multiple ConfigSources, we apply a policy to specify which one of the values will effectively be used. Under some circumstances, some data sources may change dynamically. The changed values should be fed into the client without the need for restarting the application. This requirement is particularly important for microservices running in a cloud environment. The MicroProfile Config approach allows to pick up configured values immediately after they got changed.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('fault-tolerance', 'https://github.com/eclipse/microprofile-fault-tolerance', 'Fault tolerance is about leveraging different strategies to guide the execution and result of some logic. Retry policies, bulkheads, and circuit breakers are popular concepts in this area. They dictate whether and when executions should take place, and fallbacks offer an alternative result when an execution does not complete successfully.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('health', 'https://github.com/eclipse/microprofile-health', 'Health checks are used to probe the state of a computing node from another machine (i.e. kubernetes service controller) with the primary target being cloud infrastructure environments where automated processes maintain the state of computing nodes. In this scenario, health checks are used to determine if a computing node needs to be discarded (terminated, shutdown) and eventually replaced by another (healthy) instance.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('open-api', 'https://github.com/eclipse/microprofile-open-api', 'Aims at providing a unified Java API for the OpenAPI v3 specification, that all application developers can use to expose their API documentation.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('opentracing', 'https://github.com/eclipse/microprofile-opentracing', 'Defines behaviors and an API for accessing an OpenTracing compliant Tracer object within your JAX-RS application. The behaviors specify how incoming and outgoing requests will have OpenTracing Spans automatically created. The API defines how to explicitly disable or enable tracing for given endpoints.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('metrics', 'https://github.com/eclipse/microprofile-metrics', 'Aims at providing a unified way for Microprofile servers to export Monitoring data ("Telemetry") to management agents and also a unified Java API, that all (application) programmers can use to expose their telemetry data.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('rest-client', 'https://github.com/eclipse/microprofile-rest-client', 'Provides a type-safe approach to invoke RESTful services over HTTP. As much as possible the MP Rest Client attempts to use JAX-RS 2.0 APIs for consistency and easier re-use.');

INSERT INTO micro_profile(name, source_repo, description)
VALUES('jwt-auth', 'https://github.com/eclipse/microprofile-jwt-auth', 'Using OpenID Connect(OIDC) based JSON Web Tokens(JWT) for role based access control(RBAC) of microservice endpoints.');




