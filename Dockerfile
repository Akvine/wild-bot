FROM openjdk:11
COPY target/*.jar /marketspace-bot/marketspace-bot.jar
RUN apt-get update && apt-get install -y bash
EXPOSE 8106
ENTRYPOINT ["java", "-jar", "/marketspace-bot/marketspace-bot.jar"]