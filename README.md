# dummy-kafka-producer

Produces random data to kafka

# dummy-server

Web server that responses with random data

# processor-demo

- read batch from kafka
- send async request to the dummy server on each message
- send data to kafka
- blocks until batch processed
- commit offset
- in case of errors do not commit offset
