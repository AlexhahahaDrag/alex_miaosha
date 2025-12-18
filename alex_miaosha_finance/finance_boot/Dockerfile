#
# Finance Boot 运行镜像
# 仅做 Dockerfile 最佳实践优化（不改变端口/启动参数/镜像功能；不修改 jar 包名称）
#
FROM openjdk:17

# 维护者信息（MAINTAINER 已废弃，改用 LABEL）
LABEL maintainer="alex <734663446@qq.com>"

# 时区设置（两种方式同时保留）：
# - ENV TZ：对 Java/多数程序已足够，且更通用
# - cp zoneinfo：让系统时区文件也同步（部分镜像可能没有 zoneinfo，因此加兜底避免构建失败）
# 这里做成“可选复制”，避免基础镜像缺少 /usr/share/zoneinfo 时直接失败
ENV TZ=Asia/Shanghai
RUN if [ -f "/usr/share/zoneinfo/${TZ}" ]; then \
      cp "/usr/share/zoneinfo/${TZ}" /etc/localtime && echo "${TZ}" > /etc/timezone; \
    else \
      echo "warn: /usr/share/zoneinfo/${TZ} not found, fallback to ENV TZ=${TZ}"; \
    fi

ARG JAR_FILE=./*.jar

COPY ${JAR_FILE} finance_boot-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-Xmx512m", "-jar", "finance_boot-1.0-SNAPSHOT.jar"]
CMD ["--spring.profiles.active=prod"]

EXPOSE 30008