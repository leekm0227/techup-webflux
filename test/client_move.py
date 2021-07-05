import json
import random
import time

import gevent
import locust
from websocket import create_connection


class ChannelTaskSet(locust.TaskSet):
    def __init__(self, parent):
        super().__init__(parent)
        self.ws = create_connection('ws://localhost:22222/channel')
        self.channel_id = ""
        self.session_id = ""
        self.pos = []

    def on_start(self):
        def _receive():
            while True:
                try:
                    res = self.ws.recv()
                    data = json.loads(res)

                    if data["payloadType"] == "START_TEST":
                        self.session_id = data["sessionId"]
                        self.channel_id = data["receiver"]
                        self.pos = data["pos"]
                    elif data["payloadType"] == "MOVE":
                        locust.events.request_success.fire(
                            request_type='recv',
                            name='recv',
                            response_time=round(time.time() * 1000) - int(data['regTime']),
                            response_length=len(res),
                        )
                except Exception as e:
                    locust.events.request_failure.fire(
                        request_type='send',
                        name='send',
                        response_time=round(time.time() * 1000),
                        exception=e
                    )

        gevent.spawn(_receive)

    def on_quit(self):
        self.ws.close()

    @locust.task
    def send(self):
        if self.channel_id != "" and self.pos:
            try:
                x = 0
                y = 0

                if random.choice([True, False]):
                    if random.choice([True, False]):
                        x = max(x-1, 0)
                    else:
                        x = min(x+1, 1000)
                else:
                    if random.choice([True, False]):
                        y = max(x-1, 0)
                    else:
                        y = min(x+1, 1000)

                direction = [x, y]
                data = {
                    "payloadType": 6,
                    "receiveType": 1,
                    "receiver": self.channel_id,
                    "regTime": round(time.time() * 1000),
                    "dir": direction
                }
                body = json.dumps(data)
                self.ws.send(body)
                locust.events.request_success.fire(
                    request_type='send',
                    name='send',
                    response_time=round(time.time() * 1000) - int(data['regTime']),
                    response_length=len(data['body']),
                )
            except Exception as e:
                locust.events.request_failure.fire(
                    request_type='send',
                    name='send',
                    response_time=round(time.time() * 1000),
                    exception=e
                )


class ChatLocust(locust.HttpUser):
    tasks = [ChannelTaskSet]
    locust.between(5, 10)
