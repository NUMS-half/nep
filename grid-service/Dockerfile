# 基础镜像
FROM openjdk:8

# 作者
LABEL authors="wyx"

# 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 拷贝jar包
COPY mgzmsys.jar /home/numbers/apps/myapp.jar

# 入口
ENTRYPOINT ["java", "-jar", "/home/wyx/apps/grid-service.jar"]