FROM registry.cern.ch/acc/acc-py_cc7_openjdk17_ci:pro
RUN yum install -y tomcat
COPY java-codec/build/wars/examples/TomcatJAPC.war /var/lib/tomcat/webapps
COPY java-codec/server.xml /var/lib/tomcat/conf
CMD ["/var/lib/tomcat/bin/catalina.sh", "run"]
