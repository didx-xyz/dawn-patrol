FROM eclipse-temurin:21

WORKDIR /app
COPY ./target/dawn-patrol ./dawn-patrol

ENV HF_API_KEY=
ENV OPENAI_TOKEN=

ENTRYPOINT [ "/app/dawn-patrol" ]
