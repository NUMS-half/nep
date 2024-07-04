FROM openjdk:17.0.1-slim
LABEL authors="NUMS"

# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 将JAR文件复制到镜像中
COPY chat-service-1.0.jar ./app/chat-service-1.0.jar

# 运行服务
ENTRYPOINT ["java", "-jar", "./app/chat-service-1.0.jar"]