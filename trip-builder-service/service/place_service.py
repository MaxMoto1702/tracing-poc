import logging

import requests
from py_zipkin import get_default_tracer
from py_zipkin.request_helpers import extract_zipkin_attrs_from_headers
from py_zipkin.transport import SimpleHTTPTransport
from py_zipkin.zipkin import create_http_headers_for_new_span, zipkin_span, zipkin_client_span

from constant import service_name

logger = logging.getLogger(f"tg.{__name__}")


class PlaceService(object):

    def __init__(self, url):
        self.url = url

    # noinspection DuplicatedCode
    @zipkin_span(service_name=service_name, span_name='places')
    def places(self):
        attrs = get_default_tracer().get_zipkin_attrs()
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] Get durations from {self.url}")
        # get places
        headers = create_http_headers_for_new_span()
        zipkin_attrs = extract_zipkin_attrs_from_headers(headers)
        with zipkin_client_span(
                service_name=service_name,
                zipkin_attrs=zipkin_attrs,
                span_name='post /places',
                transport_handler=SimpleHTTPTransport('localhost', 9411),
                port=8085,
                sample_rate=100,  # 0.05, # Value between 0.0 and 100.0
        ) as zipkin_context:
            zipkin_context.update_binary_annotations({'request': f"{{ids: [1,2]}}"})
            response = requests.get(
                f"{self.url}/places",
                headers=dict(headers, **{
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                }),
            )
            zipkin_context.update_binary_annotations({'response': f"{response.text}"})
        if response.status_code != 200:
            raise Exception(f"Request to {self.url} failed with status code {response.status_code}")
        return response.json()
