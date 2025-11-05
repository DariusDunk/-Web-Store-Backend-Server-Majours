FROM ubuntu:latest
LABEL authors="4ovak"

ENTRYPOINT ["top", "-b"]

#networks:
#  si:
#
#services:
#  keycloak:
#    container_name: mitko_keycloak
##    ports:
##      - "8888:8080"
##    environment:
##      - KC_BOOTSTRAP_ADMIN_USERNAME=admin
##      - KC_BOOTSTRAP_ADMIN_PASSWORD=admin
##      - ENV KC_DB=postgres
##      - ENV KC_DB_URL=jdbc:postgresql://dbb:5432/auth-db
##      - ENV KC_DB_USERNAME=admin
##      - ENV KC_DB_PASSWORD=admin
##
##    image: quay.io/keycloak/keycloak:latest
##    command:
##      - start-dev
#    image: quay.io/keycloak/keycloak:latest
#    environment:
#      - KEYCLOAK_ADMIN=admin
#      - KEYCLOAK_ADMIN_PASSWORD=admin
#      - KEYCLOAK_IMPORT=/opt/keycloak/data/import/quarkus-realm.json
#    ports:
#      - "8543:8080"
#    volumes:
#      - ./quarkus-realm.json:/opt/keycloak/data/import/quarkus-realm.json
#    command: ["start-dev", "--import-realm"]
#    restart: unless-stopped
#
#    depends_on:
#      - db
#    networks:
#      si:
#  db:
#    container_name: pe6o
#    image:
#      postgres:latest
#    environment:
#      - POSTGRES_USER=admin
#      - POSTGRES_PASSWORD=admin
#      - POSTGRES_DB=auth-db
#    ports:
#      - "5432:5432"
#    networks:
#      si: