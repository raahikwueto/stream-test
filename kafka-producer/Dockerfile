From python:3.8-slim

COPY requirement.txt .

RUN pip install --no-cache-dir -r requirement.txt

# Ensure wait.sh has Unix line endings and is executable
COPY wait.sh /wait.sh
RUN sed -i 's/\r$//' /wait.sh && chmod +x /wait.sh

COPY python-producer.py /python-producer.py

WORKDIR /
CMD ./wait.sh -s -t 30 $ZOOKEEPER_SERVER -- ./wait.sh -s -t 30 $KAFKA_SERVER -- python -u python-producer.py