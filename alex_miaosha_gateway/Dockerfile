FROM openjdk:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ARG JAR_FILE=./*.jar

COPY ${JAR_FILE} alex_miaosha_gateway-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-Xmx128m", "-jar", "alex_miaosha_gateway-1.0-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30001