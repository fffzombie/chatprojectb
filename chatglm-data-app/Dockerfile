# 基础镜像
FROM openjdk:8
# 作者
MAINTAINER zombie
# 配置
ENV PARAMS=""
# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/chatglm-data-app.jar /chatglm-data-app.jar

## 在镜像运行为容器后执行的命令
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /chatglm-data-app.jar $PARAMS"]