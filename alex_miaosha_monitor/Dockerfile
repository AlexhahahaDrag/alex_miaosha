FROM java:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

JAR_FILE alex-miaosha/alex-miaosha-monitor/target

ARG JAR_FILE

COPY ${JAR_FILE} /alex-miaosha-monitor.jar

ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/user.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30006