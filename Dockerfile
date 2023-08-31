FROM gradle:7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --info

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/install/ci-cd-helper/ /app/
ENTRYPOINT ["./bin/ci-cd-helper"]