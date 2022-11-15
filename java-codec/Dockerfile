FROM gradle:7.5.1-jdk17 AS build
#RUN apk add gradle
WORKDIR /app
#COPY --chown=gradle:gradle . .
COPY . .
#RUN apk update && apk add wget ca-certificates && update-ca-certificates
#RUN wget --ca-certificate=/app/certs/artifactory-cert.pem https://artifactory.cern.ch:443/artifactory/gradle/cern/devops/cbng/cbng-dependency-plugin/4.0.52

USER root
RUN keytool -importcert -cacerts -alias artifactory-cert -file ./certs/artifactory-cert.pem -storepass changeit -noprompt
RUN gradle clean build createJAPCWar --no-daemon -Dorg.gradle.java.home=$JAVA_HOME

FROM tomcat:10-jdk17
EXPOSE 6000
COPY --from=build /app/build/wars/examples/TomcatJAPC-1.0.0.war $CATALINA_HOME/webapps
COPY --from=build /app/server.xml $CATALINA_HOME/conf
CMD ["catalina.sh", "run"]
