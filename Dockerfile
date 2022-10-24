FROM tomcat:jdk17
COPY build/wars/TomcatJAPC.war /usr/local/tomcat/webapps
CMD ["catalina.sh", "run"]