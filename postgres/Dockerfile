FROM postgres:17.0

RUN apt update && \
    apt install dos2unix -y

COPY ./initdb /docker-entrypoint-initdb.d/

RUN dos2unix /docker-entrypoint-initdb.d/init-db.sh && \
    chmod +x /docker-entrypoint-initdb.d/init-db.sh
