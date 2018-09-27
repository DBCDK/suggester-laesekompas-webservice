/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.microservice.sample.canonical;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

@Stateless
@Path("micro-profiles")
public class MicroProfilesResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroProfilesResource.class);

    @PersistenceContext(unitName = "sample_db_PU")
    private EntityManager entityManager;

    /* Inject value of mandatory property MANDATORY_CONFIG_VALUE
       otherwise a DeploymentException will be thrown. */
    @Inject
    @ConfigProperty(name = "MANDATORY_CONFIG_VALUE")
    private String mandatoryConfigValue;

    /* Inject optional value of OPTIONAL_CONFIG_VALUE property.
       This will not lead to a DeploymentException if the configured value
       is missing. */
    @Inject
    @ConfigProperty(name = "OPTIONAL_CONFIG_VALUE")
    private Optional<String> optionalConfigValue;

    @PostConstruct
    public void initialize() {
        LOGGER.info("config/MANDATORY_CONFIG_VALUE: {}", mandatoryConfigValue);
        LOGGER.info("config/OPTIONAL_CONFIG_VALUE: {}", optionalConfigValue
                .orElse("__missing__"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMicroProfiles() {
        final TypedQuery<MicroProfileEntity> findAllQuery = entityManager
                .createNamedQuery(MicroProfileEntity.FIND_ALL, MicroProfileEntity.class);
        return Response.ok(findAllQuery.getResultList()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMicroProfile(MicroProfileEntity profile, @Context UriInfo uriInfo) {
        entityManager.persist(profile);
        final URI uri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(profile.getId()))
                .build();
        return Response.created(uri).build();
    }
}
