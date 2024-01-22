FROM openjdk:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ARG JAR_FILE=./*.jar

COPY ${JAR_FILE} finance_boot-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-Xmx512m", "-jar", "finance_boot-1.0-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30008