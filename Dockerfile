
FROM maven:3.5.2-jdk-8 as build
COPY . /usr/src/app/
RUN mvn -DskipTests -f /usr/src/app/pom.xml clean package


FROM openjdk:8

ENV VERSION=1.0.0-SNAPSHOT
ENV VERTICLE_FILE micro-library-${VERSION}-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY --from=build /usr/src/app/micro-library/target/$VERTICLE_FILE $VERTICLE_HOME/

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory $VERTICLE_FILE"]