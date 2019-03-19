FROM docker.dbc.dk/payara5-micro:latest

LABEL SUGGESTER_SOLR_URL="Suggester SolR url (required)" \
      MAX_NUMBER_SUGGESTIONS="The maximum number of suggestions returned (default: 10)"

ENV JAVA_MAX_HEAP_SIZE=2g

ADD target/*.war target/docker/app.json deployments/
