FROM java:17

MAINTAINER alex 734663446@qq.com

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ARG JAR_FILE

COPY ${JAR_FILE} /user.jar

ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/user.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30006