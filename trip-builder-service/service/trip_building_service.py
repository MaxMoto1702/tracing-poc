import logging

from py_zipkin import get_default_tracer
from py_zipkin.zipkin import zipkin_span

from constant import service_name
from service.place_service import PlaceService
from service.route_service import RouteService

logger = logging.getLogger(f"tg.{__name__}")


class TripBuildingService(object):
    def __init__(self, place_service: PlaceService, route_service: RouteService):
        self.place_service = place_service
        self.route_service = route_service

    @zipkin_span(service_name=service_name, span_name='build_trip')
    def build_trip(self, build_request):
        attrs = get_default_tracer().get_zipkin_attrs()
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] Build request {build_request}")
        places = self.place_service.places()
        self.route_service.durations(places)
        logger.debug(f"[{attrs.trace_id}-{attrs.span_id}] Build trip using places and durations between them")
        return build_request
