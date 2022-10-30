FROM openjdk:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone


ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "alex_miaosha_monitor-1.0-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30006