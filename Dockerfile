FROM eclipse-temurin:17-jre

ENV JVM_ARGS="--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED"

ARG SCM_URL=https://github.com/julbme/gh-action-semver-release-vars
ARG ARTIFACT_ID=gh-action-semver-release-vars
ARG VERSION=1.0.3-SNAPSHOT

WORKDIR /app

RUN curl -s -L -o /app/app.jar "${SCM_URL}/releases/download/v${VERSION}/${ARTIFACT_ID}-${VERSION}-shaded.jar"

CMD ["sh", "-c", "java ${JVM_ARGS} -jar /app/app.jar"]
