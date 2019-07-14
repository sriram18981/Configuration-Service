FROM  openjdk:12-jdk-oracle

USER root

WORKDIR /opt/config

# Provided full permissions to all the files in the work dfirectory
RUN chmod 777 -R .

# Creating new user to avoid using root user in the container bootup
RUN groupadd -g 888 appuser && \
    useradd -r -u 888 -g appuser appuser
    
USER appuser

# Set default options
ENV JAVA_OPTS="-XX:MinHeapFreeRatio=30  -XX:MaxHeapFreeRatio=50 -XX:+UseConcMarkSweepGC -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=2222  -Dcom.sun.management.jmxremote.rmi.port=2222  -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=10.134.108.84"

EXPOSE 8082 2222

COPY target/config-service*.jar config-service.jar

CMD ["java", "-jar", "config-service.jar"]
