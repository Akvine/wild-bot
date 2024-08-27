FROM openjdk:11
COPY target/*.jar /wild-bot/wild-bot.jar
RUN apt-get update && apt-get install -y bash
EXPOSE 8106
ENTRYPOINT ["java", "-jar", "/wild-bot/wild-bot.jar"]