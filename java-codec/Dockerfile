FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
COPY . .
RUN apk update && apk add openssl
RUN true | openssl s_client -connect artifactory.cern.ch:443 | openssl x509 > artifactory.cert
USER root
RUN keytool -importcert -cacerts -alias artifactory-cert -file artifactory.cert -storepass changeit -noprompt
RUN gradle clean war --no-daemon -Dorg.gradle.java.home=$JAVA_HOME

FROM tomcat:10-jdk17
EXPOSE 8080
WORKDIR $CATALINA_HOME
COPY --from=build /buildDir/build/libs/java-codec-1.0.0.war webapps
COPY --from=build /buildDir/server.xml conf
CMD ["catalina.sh", "run"]
