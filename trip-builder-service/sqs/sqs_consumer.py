import json
import logging
import multiprocessing
from threading import Thread
from time import sleep
from typing import Callable

from py_zipkin import Kind
from py_zipkin.transport import SimpleHTTPTransport
from py_zipkin.util import generate_random_64bit_string, ZipkinAttrs
from py_zipkin.zipkin import zipkin_span

from sqs.sqs_client import SQSClient

logger = logging.getLogger(f"tg.{__name__}")


class SqsConsumer:

    def __init__(
            self,
            queue_client: SQSClient,
            consumer_callback: Callable,
            num_workers: int
    ) -> None:
        self.queue_client = queue_client
        self.consumer_callback = consumer_callback
        self.num_workers = num_workers

    @staticmethod
    def error_handler(ex):
        logger.error(f'Error consumer handler: {ex}')

    def handle(self):
        while True:
            sleep(1)
            try:
                message_pool = self.queue_client.read_message()
                messages = message_pool.get('Messages')
            except Exception as e:
                logger.error(f"Message processing failed: {e}\n", exc_info=e)
                continue
            if not messages:
                continue
            message = messages[0]
            if not message:
                continue
            try:
                logger.info(f'Message {message} from SQS queue {self.queue_client.queue_url}')
                trace_id = message.get('MessageAttributes').get('X-B3-TraceId')["StringValue"] or f"{generate_random_64bit_string()}"
                span_id = message.get('MessageAttributes').get('X-B3-SpanId')["StringValue"] or trace_id
                is_sampled = str(message.get('x-b3-sampled') or '0') == '1'
                # is_sampled = True
                attrs = ZipkinAttrs(
                    trace_id=trace_id,
                    span_id=generate_random_64bit_string(),
                    parent_span_id=span_id,
                    flags=message.get('X-B3-Flags') or "0",
                    is_sampled=is_sampled,
                )
                with zipkin_span(
                        service_name='builder',
                        span_name='receive message',
                        zipkin_attrs=attrs,
                        kind=Kind.CONSUMER,
                        transport_handler=SimpleHTTPTransport('localhost', 9411),
                        # sample rate commented because a non-None value causes attr to be regenerated
                        sample_rate=100,  # 0.05, # Value between 0.0 and 100.0
                ) as zipkin_context:
                    logger.info(f'[{zipkin_context.zipkin_attrs.trace_id}-{zipkin_context.zipkin_attrs.span_id}] '
                                f'Message {message} from SQS queue {self.queue_client.queue_url}')
                    zipkin_context.add_sa_binary_annotation(service_name="aws")
                    zipkin_context.update_binary_annotations({'tripId': message.get('MessageAttributes').get('tripId')["StringValue"]})
                    message_body = json.loads(message.get("Body"))
                    self.consumer_callback(message_body, headers=message.get('MessageAttributes'))
            except Exception as ex:
                logger.error(f"Failed to process message {message} from queue {self.queue_client.queue_url}, "
                             f"error: {ex}", exc_info=ex)
            finally:
                self.queue_client.delete_message(message)
                logger.info(f'Message {message} has been deleted from tripId from queue {self.queue_client.queue_url}')

    def start(self) -> multiprocessing.Pool:
        for _ in range(self.num_workers):
            Thread(target=self.handle).start()

        logger.debug(f"Started SQS consumer for queue {self.queue_client.queue_url} "
                     f"Workers count: {self.num_workers} ")
