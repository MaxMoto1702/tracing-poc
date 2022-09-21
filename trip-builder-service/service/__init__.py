from service.place_service import PlaceService
from service.route_service import RouteService
from service.suggested_place_service import SuggestedPlaceService
from service.trip_building_service import TripBuildingService

route_service = RouteService("http://localhost:8084")
place_service = PlaceService("http://localhost:8083")
suggested_place_service = SuggestedPlaceService(place_service, route_service)
trip_building_service = TripBuildingService(place_service, route_service)
