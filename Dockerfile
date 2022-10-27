FROM registry.cern.ch/acc/acc-py_cc7_openjdk17_ci:pro
RUN yum install -y tomcat
COPY java-codec/build/wars/examples/TomcatJAPC.war /etc/tomcat/webapps
COPY java-codec/server.xml /etc/tomcat/conf
RUN chmod +x /etc/tomcat/bin/catalina.sh
CMD ["/etc/tomcat/bin/catalina.sh", "run"]
