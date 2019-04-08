FROM maven:3.6.0-jdk-8-alpine
COPY . /usr/src
WORKDIR /usr/src

## Compile application
RUN mvn clean install

## Add the wait script for mysql to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait /wait
RUN chmod +x /wait

CMD ["sh","-c","/wait && java -Djava.security.egd=file:/dev/./urandom -jar target/carsecurity-rest-client-0.0.1-SNAPSHOT.jar"]