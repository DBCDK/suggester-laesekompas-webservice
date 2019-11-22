/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.laesekompas.suggester.webservice;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines the other classes that make up this JAX-RS application by
 * having the getClasses method return a specific set of resources.
 * <p>
 * Note: application path must not be "/" if webapp/index.html is to be loaded.
 * </p>
 */
@ApplicationPath("api")
public class WebserviceApplication extends Application {
    private static final Set<Class<?>> classes = new HashSet<>();
    static {
        classes.add(SuggestResource.class);
        classes.add(SearchResource.class);
        classes.add(SolrProxyBean.class);
        classes.add(StatusBean.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
