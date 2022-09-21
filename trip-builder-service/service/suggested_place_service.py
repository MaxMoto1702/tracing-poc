import logging

from py_zipkin.zipkin import zipkin_span

from constant import service_name
from service import RouteService
from service.place_service import PlaceService

logger = logging.getLogger(f"tg.{__name__}")


class SuggestedPlaceService(object):
    def __init__(self, place_service: PlaceService, route_service: RouteService):
        self.place_service = place_service
        self.route_service = route_service

    @zipkin_span(service_name=service_name, span_name='suggest_by_preferences')
    def suggest_by_preferences(self, trace_id: str, span_id: str, parent_span_id: str):
        logger.debug(f"Suggest places {self}")
        places = self.place_service.places()
        self.route_service.durations(places)

    @zipkin_span(service_name=service_name, span_name='suggest_nearby')
    def suggest_nearby(self, trace_id: str, span_id: str, parent_span_id: str):
        logger.debug(f"Suggest places {self}")
        places = self.place_service.places()
        self.route_service.durations(places)
