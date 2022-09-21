import json
import logging
import traceback

import requests
from flask import jsonify, request, make_response
from py_zipkin.request_helpers import extract_zipkin_attrs_from_headers
from py_zipkin.transport import SimpleHTTPTransport
from py_zipkin.zipkin import zipkin_server_span

from app import app
from constant import service_name
from service import trip_building_service

logger = logging.getLogger(f"tg.{__name__}")


@app.route("/")
def root():
    logger.debug(f"Trace ID: {request.headers.get('x-b3-traceId')}. Span ID: {request.headers.get('x-b3-spanId')}")
    return "trip-generation-poc"


# noinspection PyBroadException
@app.route("/suggest-places-by-preferences", methods=["POST"])
def suggest_places_by_preferences():
    try:
        logger.debug(f"Trace ID: {request.headers.get('x-b3-traceId')}. Span ID: {request.headers.get('x-b3-spanId')}")
        request_dict = json.loads(request.data)
        response = requests.get(
            f"http://localhost:8080/version",
            headers={
                "Accept": "application/json",
                "Content-Type": "application/json",
                "X-B3-TraceId": "",
                "X-B3-ParentSpanId": "",
                "X-B3-SpanId": "",
            }
        )
        logger.debug(f"Response: {response.json()}")
        return jsonify(response.json())
    except Exception as e:
        traceback.print_exc()
        return make_response(f"Unable to process add place request due to error: {traceback.format_exc()}", 500)


# noinspection PyBroadException
@app.route("/suggest-places-nearby", methods=["POST"])
def suggest_places_nearby():
    try:
        logger.debug(f"Trace ID: {request.headers.get('x-b3-traceId')}. Span ID: {request.headers.get('x-b3-spanId')}")
        return "{}"
    except Exception as e:
        traceback.print_exc()
        return make_response(f"Unable to process add place request due to error: {traceback.format_exc()}", 500)


# noinspection PyBroadException
@app.route("/build-trip", methods=["POST"])
def build_trip():
    try:
        zipkin_attrs = extract_zipkin_attrs_from_headers(request.headers)
        with zipkin_server_span(
                service_name=service_name,
                zipkin_attrs=zipkin_attrs,
                span_name='post /build-trip',
                transport_handler=SimpleHTTPTransport('localhost', 9411),
                port=8085,
                sample_rate=100,  # 0.05, # Value between 0.0 and 100.0
        ) as zipkin_context:
            logger.debug(f"[{zipkin_context.zipkin_attrs.trace_id}-{zipkin_context.zipkin_attrs.span_id}] Build trip")
            zipkin_context.update_binary_annotations({"request": request.data})
            request_dict = json.loads(request.data)
            trip_dict = trip_building_service.build_trip(request_dict)
            response_text = jsonify(trip_dict)
            zipkin_context.update_binary_annotations({"response": response_text})
            raise ValueError('A very specific bad thing happened.')
            return response_text
            # return jsonify({"key": "value"})
    except Exception as e:
        logger.error(f"Unable to process add place request due to error: {e}", exc_info=e)
        return make_response(f"Unable to process add place request due to error: {e}\n{traceback.format_exc()}", 500)
