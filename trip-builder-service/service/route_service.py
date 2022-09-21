import logging

import requests
from py_zipkin import get_default_tracer
from py_zipkin.request_helpers import extract_zipkin_attrs_from_headers
from py_zipkin.transport import SimpleHTTPTransport
from py_zipkin.zipkin import create_http_headers_for_new_span, zipkin_span, zipkin_client_span

from constant import service_name

logger = logging.getLogger(f"tg.{__name__}")


class RouteService(object):

    def __init__(self, url):
        self.url = url

    # noinspection DuplicatedCode
    @zipkin_span(service_name=service_name, span_name='durations')
    def durations(self, places):
        attrs = get_default_tracer().get_zipkin_attrs()
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] Get durations between {places} from {self.url}")
        # get durations
        headers = create_http_headers_for_new_span()
        zipkin_attrs = extract_zipkin_attrs_from_headers(headers)
        with zipkin_client_span(
            service_name='builder',
            zipkin_attrs=zipkin_attrs,
            span_name='post /durations/matrix',
            transport_handler=SimpleHTTPTransport('localhost', 9411),
            port=8085,
            sample_rate=100,  # 0.05, # Value between 0.0 and 100.0
        ):
            response = requests.post(
                f"{self.url}/durations/matrix",
                json=places,
                headers=dict(headers, **{
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                }),
            )
        if response.status_code != 200:
            raise Exception(f"Request to {self.url} failed with status code {response.status_code}")
        # raise ValueError('A very specific bad thing happened.')
        return response.json()

