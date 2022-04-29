package dk.dbc.laesekompas.suggester.webservice;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
@Lock(LockType.READ)
public class SolrBean {
    private final static Logger log = LoggerFactory.getLogger(SolrBean.class);

    /**
     * LAESEKOMPAS_SOLR_URL is the URL for the laesekompas SolR that this webservice uses. This service is heavily coupled
     * with this SolRs interface, see https://gitlab.dbc.dk/os-scrum/suggester-laesekompas-solr for exact SolR config
     */
    @Inject
    @ConfigProperty(name = "LAESEKOMPAS_SOLR_URL")
    String laesekompasSolrUrl;

    /**
     * COPREPO_SOLR_URL is the URL for the corepo solr url. Used for checking holdings status when searching.
     */
    @Inject
    @ConfigProperty(name = "COREPO_SOLR_URL")
    String corepoSolrUrl;

    HttpSolrClient laesekompasSolr;
    HttpSolrClient corepoSolr;

    @PostConstruct
    public void initialize() {
        if(!this.laesekompasSolrUrl.endsWith("/solr")) {
            this.laesekompasSolrUrl = this.laesekompasSolrUrl +"/solr";
        }
        log.info("config/laesekompas SolR URL: {}", laesekompasSolrUrl);
        this.laesekompasSolr = new HttpSolrClient.Builder(laesekompasSolrUrl).build();
        if (this.corepoSolrUrl == null) {
            this.corepoSolrUrl = "";
        }
        if(!this.corepoSolrUrl.endsWith("/solr")) {
            this.corepoSolrUrl = this.corepoSolrUrl +"/solr";
        }
        // Appending alias for our specific application
        this.corepoSolrUrl = this.corepoSolrUrl+"/cisterne-laesekompas-suggester-lookup";
        log.info("config/corepo SolR URL: {}", corepoSolrUrl);
        this.corepoSolr = new HttpSolrClient.Builder(corepoSolrUrl).build();
    }

    public HttpSolrClient getLaesekompasSolr() {
        return laesekompasSolr;
    }

    public HttpSolrClient getCorepoSolr() {
        return corepoSolr;
    }

    @PreDestroy
    void onDestroy(){
        log.info("SolrBean destroyed");
    }

}
