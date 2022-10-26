FROM tomcat:10-jdk17
COPY java-codec/build/wars/examples/TomcatJAPC.war /usr/local/tomcat/webapps
COPY java-codec/server.xml /usr/local/tomcat/conf
CMD ["catalina.sh", "run"]
