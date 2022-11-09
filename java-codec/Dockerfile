FROM gradle:7.5.1-jdk17 AS build
ENV buildDir = /app/build
WORKDIR /app
COPY --chown=gradle:gradle . .
USER root
RUN keytool -import -trustcacerts -alias cern-cert -file cs-ccr-builds-cern-ch-chain.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
RUN gradle clean build createJAPCWar --no-daemon --debug --info -Dorg.gradle.java.home=$JAVA_HOME -Djavax.net.ssl.keyStore="cs-ccr-builds-cern-ch-chain.pem" -Djavax.net.ssl.keyStoreType=KeychainStore -Djavax.net.ssl.keyStorePassword=changeit

FROM tomcat:10-jdk17
COPY --from=build $buildDir/wars/examples/TomcatJAPC.war $CATALINA_HOME/webapps
CMD ["catalina.sh", "run"]
