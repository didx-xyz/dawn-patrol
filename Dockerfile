FROM eclipse-temurin:21

RUN adduser --system --group --uid 1001 --shell /bin/bash dawn

WORKDIR /app
COPY --chown=0:0 ./target/dawn-patrol ./dawn-patrol

USER dawn

ENV HF_API_KEY=
ENV OPENAI_TOKEN=

ENTRYPOINT [ "/app/dawn-patrol" ]
