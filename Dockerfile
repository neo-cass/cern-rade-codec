FROM tomcat:10-jdk17
COPY java-codec/server.xml /usr/local/tomcat/conf
COPY java-codec/build/wars/examples/TomcatJAPC.war /usr/local/tomcat/webapps.dist

CMD ["catalina.sh", "run"]
