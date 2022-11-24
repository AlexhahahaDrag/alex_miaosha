FROM openjdk:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ARG JAR_FILE=./*.jar

COPY ${JAR_FILE} alex_miaosha_monitor-1.0-SNAPSHOT.jar

COPY /usr/local/soft/drone/alex_miaosha/common /common

ENTRYPOINT ["java", "-Xmx512m", "-jar", "alex_miaosha_monitor-1.0-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30006