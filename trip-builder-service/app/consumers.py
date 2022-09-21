import logging
from random import random, randrange

from py_zipkin import get_default_tracer
from py_zipkin.util import generate_random_64bit_string

from service import trip_building_service
from sqs.sqs_client import SQSClient
from sqs.sqs_consumer import SqsConsumer

logger = logging.getLogger(f"tg.{__name__}")

request_queue_client = SQSClient("https://sqs.us-east-1.amazonaws.com/331820853321/local-max-demo-rq")
response_queue_client = SQSClient("https://sqs.us-east-1.amazonaws.com/331820853321/local-max-demo-rs")


def build_trip_callback(message_body, headers):
    attrs = get_default_tracer().get_zipkin_attrs()
    logger.info(f"[{attrs.trace_id}-{attrs.span_id}] build_trip_sqs_callback {message_body}")
    trip = trip_building_service.build_trip(message_body.get("request"))
    response_queue_client.send_message(trip, headers)


build_trip_consumer = SqsConsumer(
    request_queue_client,
    build_trip_callback,
    1,
)


def temp_callback(message_body, trace_id, parent_span_id, span_id):
    logger.info(f"[{trace_id}-{span_id}] temp_callback {message_body}")


temp_consumer = SqsConsumer(
    response_queue_client,
    temp_callback,
    1,
)
