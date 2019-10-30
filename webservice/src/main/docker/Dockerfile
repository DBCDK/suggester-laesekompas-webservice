FROM docker.dbc.dk/payara5-micro:latest

ENV MAX_POOL_SIZE=3

LABEL SUGGESTER_SOLR_URL="Suggester SolR url (required)" \
      MAX_NUMBER_SUGGESTIONS="The maximum number of suggestions returned (default: 10)" \
      COREPO_SOLR_URL="Corepo SolR url (required)" \
      MAX_POOL_SIZE="JDBC connection pool parameter (optional, default is 3)"

ENV JAVA_MAX_HEAP_SIZE=2g

ADD target/*.war target/docker/app.json deployments/
