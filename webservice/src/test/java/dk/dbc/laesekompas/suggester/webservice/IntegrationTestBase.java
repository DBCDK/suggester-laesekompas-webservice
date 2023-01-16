package dk.dbc.laesekompas.suggester.webservice;

import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;

public class IntegrationTestBase {

    public static final GenericContainer SOLR = makeSolr();
    public static final String SOLR_URL = makeContainerUrl(SOLR, 8983) + "/solr";

    public Http2SolrClient makeSolrClient() {
        return new Http2SolrClient.Builder(SOLR_URL).build();
    }

    private static GenericContainer makeSolr() {
        String image = "docker-de.artifacts.dbccloud.dk/suggester-laesekompas-solr:latest";
        GenericContainer solr = new GenericContainer(image)
                .withImagePullPolicy(new ImagePullPolicy() {
                    @Override
                    public boolean shouldPull(DockerImageName din) {
                        return true;
                    }
                })
                .withExposedPorts(8983)
                .waitingFor(Wait.forHttp("/solr/suggest-all/admin/ping"));
        solr.start();
        return solr;
    }

    private static String makeContainerUrl(GenericContainer container, int port) {
        String ip = containerIp(container);
        return "http://" + ip + ":" + port;
    }

    private static String containerIp(GenericContainer container) {
        return container.getContainerInfo().getNetworkSettings().getNetworks().values().stream().findFirst().orElseThrow().getIpAddress();
    }
}
