FROM eclipse-temurin:21

WORKDIR /app
COPY ./target/dawn-patrol ./dawn-patrol

ENTRYPOINT [ "/app/dawn-patrol" ]
