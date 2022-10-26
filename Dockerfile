FROM alpine

COPY java-codec/build/wars/TomcatJAPC.war /usr/local/tomcat/webapps

CMD ["catalina.sh", "run"]
