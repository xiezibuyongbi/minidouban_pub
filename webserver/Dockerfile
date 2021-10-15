FROM xiezibuyongbi/openjdk-11-jre-alpine
MAINTAINER xiezibuyongbi <2793001466@qq.com>
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ADD target/webserver.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
#EXPOSE 80
