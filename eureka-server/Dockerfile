FROM openjdk:17

# 作者
LABEL authors="NUMS"

# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 拷贝jar包
COPY eureka-server-1.0.jar /home/wyx/apps/euraka-server/eureka-server-1.0.jar

# 入口
ENTRYPOINT ["java", "-jar", "/home/wyx/apps/euraka-server/eureka-server-1.0.jar"]