# D@wn Patrol
![dawn-patrol](src/resources/DawnPatrolLogo.png)

DawnPatrol provides a chatbot interface to messaging services such as Signal, Slack, WhatsApp and Telegram.
It utilizes LLM's such as ChatGPT to provide a conversational capability to support a large variety of client inputs
Special messages:

- Admin messages: @admin|add <name> <mobilenumber>- allows administrators to add new users to ecosystem
This will be replaced by uploading .vcf file

- Location data: Upload a Google Maps format pin.

## Prerequisites

Two API-keys are required to bet set as environment variables in order to run the AI logic that's built into DawnPatrol:
- `HF_API_KEY` = a free key obtained from Hugging Face
- `OPENAI_TOKEN` = an OpenAI token for the charged API calls

Additionally, this project uses [scala-cli](https://scala-cli.virtuslab.org/docs/overview/) as build tool. 

Installing scala-cli on Linux:
```bash
curl -sSLf https://scala-cli.virtuslab.org/get | sh
```

or on Mac:
```bash
brew install Virtuslab/scala-cli/scala-cli
```

## Running DawnPatrol

```bash
scala-cli run .
```

Once running, the application will poll the Signal API, obtaining messages for the configured phone number (`signal-conf.signal-phone` in [application.conf](src/resources/application.conf)), and responding accordingly.

The messaging logic is defined in [DawnPatrol](src/xyz/didx/DawnPatrol.scala) and [ConversationPollingHandler](src/xyz/didx/ConversationPollingHandler.scala)
