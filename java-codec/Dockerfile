FROM gradle:7.5.1-jdk17 AS build
ENV buildDir = /app/build
WORKDIR /app
COPY --chown=gradle:gradle . .
USER root
RUN keytool -import -trustcacerts -cacerts -alias cern-cert -file certs/artifactory-cert.pem -storepass changeit -noprompt
RUN gradle clean build createJAPCWar --no-daemon --debug --info -Dorg.gradle.java.home=$JAVA_HOME -Djavax.net.ssl.keyStore="certs/artifactory-cert-root.pem" -Djavax.net.ssl.keyStoreType=KeychainStore -Djavax.net.ssl.keyStorePassword=changeit

FROM tomcat:10-jdk17
COPY --from=build $buildDir/wars/examples/TomcatJAPC.war $CATALINA_HOME/webapps
CMD ["catalina.sh", "run"]
