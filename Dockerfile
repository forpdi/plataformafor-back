FROM maven:3.6.0-jdk-8 as build

ARG properties

RUN mkdir /usr/app
WORKDIR /usr/app/
COPY . /usr/app

COPY ./conf/${properties} /usr/app/prd.properties

RUN mvn clean package -P prd

FROM adoptopenjdk/openjdk8:jre8u275-b01-alpine

ARG build_version

RUN apk add --upgrade apk-tools
RUN apk add --upgrade busybox
RUN apk add curl
RUN apk add tzdata
RUN cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime
RUN echo "America/Sao_Paulo"  > /etc/timezone
RUN apk del tzdata
RUN apk add --no-cache freetype=2.10.4-r2 fontconfig=2.13.1-r2 ttf-dejavu=2.37-r1

ENV BUILD_VERSION ${build_version}
ENV WILDFLY_VERSION 9.0.2.Final
ENV WILDFLY_SHA1 75738379f726c865d41e544e9b61f7b27d2853c7
ENV JBOSS_HOME /opt/jboss/wildfly
ENV LAUNCH_JBOSS_IN_BACKGROUND 1

RUN cd $HOME \
    && curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
    && tar xz -f wildfly-$WILDFLY_VERSION.tar.gz \
    && mkdir -p /opt/jboss \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz

COPY --from=build /usr/app/target/forpdi.war /opt/jboss/wildfly/standalone/deployments/
COPY ./conf/standalone.conf /opt/jboss/wildfly/bin/standalone.conf
COPY ./conf/standalone.xml /opt/jboss/wildfly/standalone/configuration/

EXPOSE 8080 8009

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]