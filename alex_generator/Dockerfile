#
# Alex Generator 运行镜像
# 仅做 Dockerfile 最佳实践优化（不改变端口/启动参数/镜像功能）
#
FROM eclipse-temurin:17-jre

# 维护者信息
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

# 构建参数：支持外部传入 jar 路径（默认取构建上下文下的 *.jar）
ARG JAR_FILE=./*.jar

# 复制 jar（保持原有 jar 包名称不变）
COPY ${JAR_FILE} alex_miaosha_generator-1.0-SNAPSHOT.jar

# 启动命令：保持原有 JVM 内存参数；其余参数由 CMD 追加
ENTRYPOINT ["java", "-Xmx512m", "-jar", "alex_miaosha_generator-1.0-SNAPSHOT.jar"]

# 默认参数：生产环境（可在 docker run 时覆盖）
CMD ["--spring.profiles.active=prod"]

EXPOSE 30006