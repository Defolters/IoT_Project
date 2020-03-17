import argparse
import datetime
import re
import sys
import time
import warnings

import Adafruit_DHT
import mh_z19
from firebase import firebase


def check_pi():
    with open("/proc/cpuinfo", "r") as info_file:
        cpuinfo = info_file.read()

    match = re.search(
        "^Hardware\s+:\s+(\w+)$", cpuinfo, flags=re.MULTILINE | re.IGNORECASE
    )
    if not match:
        raise RuntimeError("Could not find the hardware, assume it is not a pi.")
    if match.group(1) not in ["BCM2708", "BCM2709", "BCM2835", "BCM2837"]:
        raise RuntimeError("Could not recognize Raspberry Pi version.")

    return None


def check_args(args, available_sensors, pin_range):
    if args.sensor not in available_sensors:
        raise ValueError("Expected DHT11, DHT22, or AM2302 sensor value.")

    if args.pin < pin_range[0] or args.pin > pin_range[1]:
        raise ValueError("Pin must be a valid GPIO number 0 to 31.")


def get_frequency(firebase_app):
    settings = firebase_app.get("/homeweather-iot/UserSettings", "")
    return datetime.timedelta(seconds=list(settings.values())[0]["DelayS"])


def measure_data(sensor, pin):
    humidity, temperature = Adafruit_DHT.read(sensor, pin)
    co2 = mh_z19.read()["co2"]
    time = datetime.datetime.now()
    return {"humidity": humidity, "temperature": temperature, "co2": co2, "time": time}


def record_data(firebase_app, data):
    record = {
        "Time": data["time"],
        "CO2": data["co2"],
        "Humidity": data["humidity"],
        "Temperature": data["temperature"],
    }
    result = firebase_app.post("/homeweather-iot/WeatherRecords", record)
    print(result)
    if not result:
        warnings.warn(
            f"Recording failed for unknown reason. \nData:{record}", RuntimeWarning
        )


def run(sensor, pin):
    firebase_app = firebase.FirebaseApplication(
        "https://homeweather-iot.firebaseio.com/", None
    )
    last_measurement = datetime.datetime.now()
    while True:
        frequency = get_frequency(firebase_app)

        if (last_measurement + frequency) <= datetime.datetime.now():
            data = measure_data(sensor, pin)
            record_data(firebase_app, data)
            last_measurement = data["time"]

        time.sleep(((last_measurement + frequency) - datetime.datetime.now()).seconds)


if __name__ == "__main__":
    sensor_args = {
        "11": Adafruit_DHT.DHT11,
        "22": Adafruit_DHT.DHT22,
        "2302": Adafruit_DHT.AM2302,
    }
    available_sensors = list(sensor_args.keys())
    pin_range = (0, 31)

    parser = argparse.ArgumentParser(
        description="Run simple home weather station.\n"
        "Usage: sudo HomeWeather.py [11|22|2302] <GPIO pin number>"
    )
    optional = parser._action_groups.pop()
    required = parser.add_argument_group("required arguments")
    parser._action_groups.append(optional)

    required.add_argument(
        "-s",
        "--sensor",
        dest="sensor",
        required=True,
        choices=available_sensors,
        help=f"Sensor type, {available_sensors} are available",
    )
    required.add_argument("-p", "--pin", dest="pin", type=int, help="GPIO pin number")
    optional.add_argument(
        "-f",
        "--frequency",
        dest="frequency",
        type=int,
        help="Measurement time frequency",
    )

    try:
        args = parser.parse_args()
        check_pi()
        check_args(args, available_sensors, pin_range)

        run(sensor_args[args.sensor], args.pin)

    except Exception as err:
        print("Failed: ", err)
        sys.exit(1)
