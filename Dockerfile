FROM tomcat:10-jdk17
COPY java-codec/build/wars/examples/TomcatJAPC.war /usr/local/tomcat/webapps

CMD ["catalina.sh", "run"]
