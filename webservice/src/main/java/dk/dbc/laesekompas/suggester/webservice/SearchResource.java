package dk.dbc.laesekompas.suggester.webservice;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of microservice-sample
 *
 * microservice-sample is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * microservice-sample is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File created: 20/02/2019
 */

import dk.dbc.laesekompas.suggester.webservice.solr_entity.SearchEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SearchEntityType;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@Path("search")
public class SearchResource {
    public static final String SOLR_FULL_TEXT_QUERY = "author^6.0 title^5.0 all abstract";
    public static final String COREPO_SOLR_TEXT_QUERY = "holdingsitem.agencyId:%s AND holdingsitem.bibliographicRecordId:\"%s\" AND holdingsitem.status:OnShelf";
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);
    HttpSolrClient laesekompasSolr;
    HttpSolrClient corepoSolr;

    /**
     * SUGGESTER_SOLR_URL is the URL for the suggestion SolR that this webservice uses. This service is heavily coupled
     * with this SolRs interface, see https://gitlab.dbc.dk/os-scrum/suggester-laesekompas-solr for exact SolR config
     */
    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String searchSolrUrl;

    /**
     * COPREPO_SOLR_URL is the URL for the corepo solr url
     */
    @Inject
    @ConfigProperty(name = "COREPO_SOLR_URL")
    String corepoSolrUrl;

    /**
     * SOLR_APPID is the Application ID that we send to Solr for tracing purposes.
     */
    @Inject
    @ConfigProperty(name = "SOLR_APPID")
    String solrAppId;

    /**
     * MAX_NUMBER_SUGGESTIONS is the maximum number of suggestion that should be returned by all suggest endpoints.
     * Should match the number of suggestions given by the suggestion SolR, a parameter that is statically configured
     * on the SolR.
     */
    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS", defaultValue = "10")
    Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        if(!this.searchSolrUrl.endsWith("/solr")) {
            this.searchSolrUrl = this.searchSolrUrl +"/solr";
        }
        LOGGER.info("config/laesekompas SolR URL: {}", searchSolrUrl);
        this.laesekompasSolr = new HttpSolrClient.Builder(searchSolrUrl).build();
        if(!this.corepoSolrUrl.endsWith("/solr")) {
            this.corepoSolrUrl = this.corepoSolrUrl +"/solr";
        }
        // Appending alias for our specific application
        this.corepoSolrUrl = this.corepoSolrUrl+"/cisterne-laesekompas-suggester-lookup";
        LOGGER.info("config/corepo SolR URL: {}", corepoSolrUrl);
        this.corepoSolr = new HttpSolrClient.Builder(corepoSolrUrl).build();
        LOGGER.info("config/MAX_NUMBER_SUGGESTIONS: {}", maxNumberSuggestions);
        if (solrAppId == null) {
            solrAppId = "";
        }
        LOGGER.info("solrAppId: {}", solrAppId);
    }

    private static SolrParams SolrSearchParams(SearchParams params, String solrAppId) {
        HashMap<String, String> hm = new HashMap<String, String>() {{
            String qf;
            switch (params.field) {
                case "author":
                    qf = params.exact ? "author_exact" : "author";
                    break;
                case "title":
                    qf = params.exact ? "title_exact" : "title";
                    break;
                case "pid":
                    qf = "pid";
                    break;
                case "workid":
                    qf = "workid";
                    break;
                default:
                    qf = SOLR_FULL_TEXT_QUERY;
                    break;
            }
            put(CommonParams.Q, params.query);
            put("defType", "dismax");
            put("qf", qf);
            put("bf", "log(loans)");
            put("appId", solrAppId);
            if (params.branchId != null) {
                put("fq", "branch_id:\""+params.branchId+"\"");
            }
            put(CommonParams.ROWS, Integer.toString(params.rows));
        }};
        return new MapSolrParams(hm);
    }

    private static SolrParams onShelfLookupParams(String agencyId, String bibId, String solrAppId) {
        String query = String.format(COREPO_SOLR_TEXT_QUERY, agencyId, bibId);
        LOGGER.debug("Query for holdings items OnShelf lookup: {}", query);
        HashMap<String, String> hm = new HashMap<String, String>() {{
            put(CommonParams.Q, query);
            put(CommonParams.ROWS, "0");
        }};
        return new MapSolrParams(hm);
    }

    private static SolrQuery onShelfLookupQuery(String agencyId, String bibId, String solrAppId) {
        String query = String.format(COREPO_SOLR_TEXT_QUERY, agencyId, bibId);
        SolrQuery res = new SolrQuery();
        res.setParam(CommonParams.Q, query);
        res.setParam(CommonParams.ROWS, "0");
//        res.setParam("appId", solrAppId);
        return res;
    }


    /**
     * Performs a freeform user search on all the content of laesekompasset.
     * @param query User query search
     * @param field The document field to be queried, for example title/author etc. If empty, a general search is
     *              performed across all relevant fields
     * @param exact Whether or not the query should be an exact match, only works when `field` parameter is set to
     *              either `author` or `title`.
     * @param mergeWorkID Whether to merge documents on workID, so the same work does not get returned as both EBook,
     *                    audio book and regular book. Picks A-posts, then books to represent the work.
     * @param rows Number of result rows to be returned
     * @param branchId the branch id the result should filter on, if not given no filtering is applied
     * @param filterStatusOnShelf Whether materials should be filtered based on if the branch specified in branchId
     *                            parameter has them OnShelf
     * @return List of search results, ranked by relevancy. With `field` and `exact` set, array will only contain
     * 1 element, as it serves as a lookup.
     * @throws SolrServerException Thrown if the SolR client throws any exceptions
     * @throws IOException Thrown if the network connection to the SolR fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("query") String query,
                           @DefaultValue("") @QueryParam("field") String field,
                           @DefaultValue("false") @QueryParam("exact") boolean exact,
                           @DefaultValue("false") @QueryParam("merge_workid") boolean mergeWorkID,
                           @DefaultValue("10") @QueryParam("rows") int rows,
                           @DefaultValue("false") @QueryParam("filter_status") boolean filterStatusOnShelf,
                           @QueryParam("branch_id") String branchId) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }
        MDC.put("requestType", "search");
        MDC.put("query", query);
        MDC.put("field", field);
        MDC.put("exact", "" + exact);
        MDC.put("merge_workid", "" + mergeWorkID);
        MDC.put("rows", "" + rows);
        MDC.put("filter_status", "" + filterStatusOnShelf);
        MDC.put("branch_id", branchId);

        LOGGER.info("/search performed with query: {}, field: {}, exact: {}, merge_workid: {}, rows: {}, branch_id: {}, filter_status: {}",
                query, field, exact, mergeWorkID, rows, branchId, filterStatusOnShelf);

        // Order of which search results where retrieved
        Integer order = 0;
        // Index of SolR responses
        int solrSearchIndex = 0;
        boolean solrSearchResultEnd = false;
        List<SearchEntity> searchResults = new ArrayList<>();
        // We loop because filterStatusOnShelf might filter out results
        while (searchResults.size() < rows && !solrSearchResultEnd) {
            // Asks for x3 rows when merging workID's, since a work can potentially have 3 manifestations
            SolrParams params = SolrSearchParams(new SearchParams(query, field, exact, rows * (mergeWorkID ? 3 : 1), branchId, solrSearchIndex), solrAppId);
            QueryResponse solrResponse = laesekompasSolr.query("search", params);

            // If we have paged past the number of total results in SolR, we are at the end
            solrSearchResultEnd = (solrSearchIndex+rows) >= (solrResponse.getResults().getNumFound());

            // Converting search results to SearchEntity
            ArrayList<SearchEntity> buffer = SearchEntity.searchResultsIntoSearchEntities(solrResponse.getResults(), order);

            List<SearchEntity> filtered_buffer;
            // Filter based on holdings items status for each result
            if (branchId != null && filterStatusOnShelf) {
                LOGGER.debug("filtering...");
                String agencyId = branchId.substring(0, 6);
                filtered_buffer = buffer.stream()
                        .filter(searchEntity -> {
                            // Checks if any of the items in the work have an OnShelf status
                            return searchEntity.getBibIdsInWork().stream()
                                    .map(bibId -> {
                                        try {

                                            SolrParams solrParams = onShelfLookupParams(agencyId, bibId, solrAppId);
                                            SolrQuery solrQuery = onShelfLookupQuery(agencyId, bibId, solrAppId);
                                            QueryResponse response = corepoSolr.query(solrQuery);
                                            return response.getResults().getNumFound() > 1;
                                        } catch (IOException | SolrServerException e) {
                                            LOGGER.error("Failed talking to corepo SolR by looking up: {}/{}", agencyId, bibId);
                                            LOGGER.error("{}", e);
                                            throw new RuntimeException("Failed talking to corepo SolR");
                                        }
                                    })
                                    // Only one of the bibliographic items needs to be `OnShelf` ie. is true
                                    .reduce(false, Boolean::logicalOr);
                        })
                        .collect(Collectors.toList());
            } else {
                filtered_buffer = buffer;
            }
            searchResults.addAll(filtered_buffer);
            solrSearchIndex += rows;
        }
        // If true, returns 1 material per workID
        if (mergeWorkID) {
            HashMap<String, SearchEntity> duplicateRemover = new HashMap<>();
            for (SearchEntity searchEntity : searchResults) {
                duplicateRemover.merge(searchEntity.getWorkid(), searchEntity, (se1, se2) -> {
                    // When selecting a document to represent the work, we prefer A-posts, then books, then the highest
                    // ranked. Regardless if both are or aren't A-posts, we continue with prioritizing books and the
                    // highest ranked
                    if (se1.getAPost() != se2.getAPost()) {
                        if(se1.getAPost()) return se1; else return se2;
                    } else if(se1.getType() == SearchEntityType.BOOK && se2.getType() == SearchEntityType.BOOK) {
                        return se1.getOrder() < se2.getOrder() ? se1 : se2;
                    } else if (se1.getType() == SearchEntityType.BOOK) return se1;
                    else if (se2.getType() == SearchEntityType.BOOK) return se2;
                    else {
                        // Order assignment is strictly increasing, which why they can never be equal
                        return se1.getOrder() < se2.getOrder() ? se1 : se2;
                    }
                });
            }
            // Sort merged works according to order to achieve the final result
            searchResults = duplicateRemover.values().parallelStream()
                    // Order integers are never the same
                    .sorted((a,b) -> a.getOrder() > b.getOrder() ? 1 : -1)
                    .collect(Collectors.toList());
        }
        MDC.clear();
        // If mergeWorkId is set, but not all works has 3 manifestations, we could potentially have more than 'rows'
        // results, which is why we create the sublist. We might also have less than 'rows' results if the SolR search
        // did not return 'rows' results, which we must account for
        return Response.ok().entity(searchResults.subList(0, Integer.min(rows, searchResults.size()))).build();
    }

    // POJO to pass 4 arguments to solr params function
    private static class SearchParams implements Serializable {
        private static final long serialVersionUID = -3844877980534226242L;
        String query;
        String field;
        boolean exact;
        int rows;
        String branchId;
        int start;

        SearchParams(String query, String field, boolean exact, int rows, String branchId, int start) {
            this.query = query;
            this.field = field;
            this.exact = exact;
            this.rows = rows;
            this.branchId = branchId;
            this.start = start;
        }

        @Override
        public String toString() {
            return query+"|"+field+"|"+exact+"|"+rows;
        }
    }
}
