FROM tomcat:10-jdk17

COPY java-codec/build/wars/TomcatJAPC.war /usr/local/tomcat/webapps

CMD ["usr/local/tomcat/bin/catalina.sh", "run"]
