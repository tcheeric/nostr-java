# Builds the MCP server as a self-contained image.
#
# Two stages, so the image carries a runtime and a jar rather than a JDK, Maven, and the whole
# dependency cache. The result is smaller and has far less in it that could be exploited.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
# Copy the poms first so dependency resolution is cached independently of the source: editing a
# Java file should not re-download the world.
COPY pom.xml .
COPY nostr-java-core/pom.xml nostr-java-core/
COPY nostr-java-event/pom.xml nostr-java-event/
COPY nostr-java-identity/pom.xml nostr-java-identity/
COPY nostr-java-client/pom.xml nostr-java-client/
COPY nostr-java-api/pom.xml nostr-java-api/
COPY nostr-java-mcp/pom.xml nostr-java-mcp/
RUN mvn -B -q dependency:go-offline -DskipTests || true

COPY . .
RUN mvn -B -q -DskipTests -Dmaven.javadoc.skip=true package

# Distroless: no shell, no package manager, nothing to pivot to if the server is compromised.
# The server holds private keys, so the smallest possible surface is worth the loss of
# debuggability.
FROM gcr.io/distroless/java21-debian12:nonroot

# Runs as the base image's unprivileged user. Nothing here needs root, and a container that
# does not need it should not have it.
USER nonroot

WORKDIR /app
COPY --from=build /build/nostr-java-mcp/target/nostr-java-mcp-*-runnable.jar /app/nostr-java-mcp.jar

# HTTP is the only transport that makes sense in a container: stdio needs the host to own the
# process, which defeats the point of packaging it.
ENV NOSTR_MCP_TRANSPORT=http

# Binds every interface *inside* the container, which is what lets Docker's port mapping reach
# it. The container is the boundary, so the compose file publishes only to the host loopback;
# see the warning in docker-compose.yml.
ENV NOSTR_MCP_BIND_ADDRESS=0.0.0.0
ENV NOSTR_MCP_PORT=8080

# A keychain does not exist in a container, so the portable backend is the only sensible default
# here. The keystore itself is mounted, never baked in.
ENV NOSTR_MCP_KEYSTORE_TYPE=encrypted-file
ENV NOSTR_MCP_KEYSTORE_PATH=/keys/keys.p12

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/nostr-java-mcp.jar"]
