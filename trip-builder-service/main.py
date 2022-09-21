import logging.config
import yaml
from app import app
from app.consumers import build_trip_consumer, temp_consumer

logger = logging.getLogger(f"tg.{__name__}")

if __name__ == "__main__":
    with open('logging.yaml', 'r') as f:
        config = yaml.safe_load(f.read())
    logging.config.dictConfig(config)
    build_trip_consumer.start()
    # temp_consumer.start()
    app.run("0.0.0.0", 8085, threaded=True, debug=True)
