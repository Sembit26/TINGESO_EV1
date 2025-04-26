FROM openjdk:17

# ARG es para que puedas usarlo con --build-arg si quieres, pero no es obligatorio
ARG JAR_FILE=target/*.jar

# Copias el JAR y lo renombras dentro del contenedor
COPY ${JAR_FILE} backend.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "./backend.jar", "--server.port=8090"]
