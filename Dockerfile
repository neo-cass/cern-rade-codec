FROM registry.cern.ch/acc/acc-py_cc7_openjdk17_ci:pro
RUN yum install tomcat
COPY java-codec/build/wars/examples/TomcatJAPC.war /usr/local/tomcat/webapps
COPY java-codec/server.xml /usr/local/tomcat/conf
CMD ["catalina.sh", "run"]
