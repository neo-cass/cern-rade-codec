FROM gradle:7.5.1-jdk17-alpine AS build
WORKDIR /buildDir
COPY . .
RUN apk update && apk add openssl
RUN true | openssl s_client -connect artifactory.cern.ch:443 | openssl x509 > artifactory.cert
USER root
RUN keytool -importcert -cacerts -alias artifactory-cert -file artifactory.cert -storepass changeit -noprompt
RUN gradle --no-daemon clean war


FROM tomcat:11.0.0-jdk17
ENV webappDir=/buildDir/webapp-build
EXPOSE 9090
WORKDIR $CATALINA_HOME

RUN groupadd -r myuser -g 501
RUN useradd -d /home/myuser -u 501 -m -s /bin/bash -g myuser myuser
RUN chown -R myuser:myuser /usr/local/tomcat
USER myuser

COPY --from=build $webappDir/java-codec-1.0.0.war webapps
COPY --from=build $webappDir/server.xml conf

CMD ["catalina.sh", "run"]
