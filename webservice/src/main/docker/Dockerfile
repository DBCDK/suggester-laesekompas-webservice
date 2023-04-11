FROM docker-dbc.artifacts.dbccloud.dk/payara6-micro:latest

ENV MAX_POOL_SIZE=3

LABEL LAESEKOMPAS_SOLR_URL="Laesekompas SolR url (required)" \
      MAX_NUMBER_SUGGESTIONS="The maximum number of suggestions returned (default: 10)" \
      COREPO_SOLR_URL="Corepo SolR url (required)" \
      SOLR_APPID="Application ID to pass to Solr for logging purposes in Solr (required)" \
      MAX_POOL_SIZE="JDBC connection pool parameter (optional, default is 3)"

ENV JAVA_MAX_HEAP_SIZE=2g

ADD target/*.war target/docker/app.json deployments/
