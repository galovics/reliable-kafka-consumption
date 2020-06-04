# Fault-tolerant and reliable Kafka message consumption with Spring Boot
The purpose of this application is to give a showcase on how
a reliable and fault-tolerant Kafka messaging system can be
implemented using Spring Boot.

The article that covers the implementation can be found
here:

The applicatio uses manual offset commit in order to achieve
at least once message delivery guarantee. Additionally, to
make it more robust, there is a dead-letter queue (DLQ) implemented
as well that is used for two reasons:
- A way of retrying to process messages
- When the retry threshold is reached, it logs the message

## Project structure
### Infrastructure
There's Kafka and Zookeeper needed for the messaging system to
work. In the root directory, there's a `docker-compose.yml`
file available that can be used to set up these components.
It was tested with Docker for Windows, might need some 
tuning for Mac or Linux, specifically on the networking.

### normal-topic-consumer
This is the service that consumes messages from the
`normal-topic` Kafka topic. In case there's an error,
it sends the messages to the `dlq-topic` for applying
the DLQ logic.

### dlq-topic-consumer
This service acts as a DLQ and implements the message
retry. It consumes messages from the `dlq-topic`. It 
resends failed messages to the original topic until
the retry threshold is exceeded.

## Running the system
First, set up the infrastructure:
```bash
$ docker-compose up -d
```

Then start up the 2 microservices (ordering is not important):
```bash
$ ./gradlew clean build bootRun
```

Now, send some messages to the `normal-topic`. You can
exec into the Kafka container and use the kafka-console-producer.sh
to generate the messages.

```bash
$ sh /opt/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic normal-topic
>{"data":"test"}
```

## License
```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```