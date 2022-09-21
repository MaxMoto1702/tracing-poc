import json
import logging

import boto3
from py_zipkin import get_default_tracer, Kind
from py_zipkin.transport import SimpleHTTPTransport
from py_zipkin.zipkin import zipkin_client_span, zipkin_span

logger = logging.getLogger(f"tg.{__name__}")


class SQSClient(object):

    def __init__(self, queue_url: str):
        self.queue_url = queue_url
        self.sqs = boto3.client(
            'sqs',
            region_name="us-east-1",
            aws_access_key_id="AKIAU2QQQNBEVX6NEUTJ",
            aws_secret_access_key="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",
        )

    def read_message(self):
        response = self.sqs.receive_message(
            QueueUrl=self.queue_url,
            MaxNumberOfMessages=1,
            MessageAttributeNames=['All'],
            VisibilityTimeout=310,
            WaitTimeSeconds=1,
        )
        return response

    def send_message(self, message_body, headers):
        attrs = get_default_tracer().get_zipkin_attrs()
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] send message: {message_body}")
        with zipkin_span(
                service_name='builder',
                span_name='send message',
                transport_handler=SimpleHTTPTransport('localhost', 9411),
                port=8085,
                sample_rate=100,  # 0.05, # Value between 0.0 and 100.0
                kind=Kind.PRODUCER,
        ) as zipkin_context:
            zipkin_context.add_sa_binary_annotation(service_name="aws")
            response = self.sqs.send_message(
                QueueUrl=self.queue_url,
                DelaySeconds=0,
                MessageBody=(json.dumps(message_body)),
                MessageAttributes={
                    'X-B3-TraceId': {"StringValue": zipkin_context.zipkin_attrs.trace_id, "DataType": "String"},
                    'X-B3-SpanId': {"StringValue": zipkin_context.zipkin_attrs.span_id, "DataType": "String"},
                    'tripId': headers.get("tripId"),
                }
            )
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] sent message id: {response['MessageId']}")

    def delete_message(self, message):
        self.sqs.delete_message(
            QueueUrl=self.queue_url,
            ReceiptHandle=message.get('ReceiptHandle')
        )
        logger.debug(f'deleted message: {message}')
