import json
import random
import time

import gevent
import locust
from websocket import create_connection


class ChannelTaskSet(locust.TaskSet):
    wait_time = locust.between(2, 5)

    def __init__(self, parent):
        super().__init__(parent)
        self.ws = create_connection('ws://localhost:22222/channel')
        self.channel_id = ""
        self.session_id = ""
        self.pos = []
        self.tmpPos = []

    def on_start(self):
        def _receive():
            while True:
                try:
                    res = self.ws.recv()
                    data = json.loads(res)
                    res_time = time.time()

                    if data["payloadType"] == "INFO":
                        self.session_id = data["sessionId"]
                        self.channel_id = data["channelId"]
                        self.pos = data["pos"]
                    elif data["payloadType"] == "MOVE" and data["sessionId"] == self.session_id:
                        response_time = int((res_time - data['regTime']) * 1000)
                        x = self.pos[0] + data["dir"][0]
                        y = self.pos[0] + data["dir"][0]

                        if data["pos"][0] == x and data["pos"][0] == y:
                            locust.events.request_success.fire(
                                request_type='recv',
                                name='success',
                                response_time=response_time,
                                response_length=len(res),
                            )
                        else:
                            print("fail:", self.session_id, self.pos, data['dir'], data['pos'])
                            locust.events.request_failure.fire(
                                request_type='recv',
                                name='fail',
                                response_time=res_time - data['regTime'],
                                response_length=len(res),
                                exception='not valid pos'
                            )

                        self.pos = data["pos"]
                except Exception as e:
                    locust.events.request_failure.fire(
                        request_type='recv',
                        name='fail',
                        response_time=0,
                        response_length=0,
                        exception=e
                    )

        gevent.spawn(_receive)

    def on_quit(self):
        self.ws.close()

    @locust.task
    def send(self):
        if self.channel_id != "" and self.pos and self.pos != self.tmpPos:
            try:
                x = 0
                y = 0

                if random.choice([True, False]):
                    if random.choice([True, False]):
                        x = -1
                    else:
                        x = 1
                else:
                    if random.choice([True, False]):
                        y = -1
                    else:
                        y = 1

                direction = [x, y]
                data = {
                    "payloadType": 6,
                    "receiveType": 1,
                    "receiver": self.channel_id,
                    "regTime": time.time(),
                    "body": "test",
                    "dir": direction,
                }
                body = json.dumps(data)
                self.ws.send(body)
                self.tmpPos = self.pos
                locust.events.request_success.fire(
                    request_type='send',
                    name='success',
                    response_time=0,
                    response_length=len(data['body']),
                )
            except Exception as e:
                locust.events.request_failure.fire(
                    request_type='send',
                    name='fail',
                    response_time=0,
                    response_length=0,
                    exception=e
                )
        else:
            data = {"payloadType": 7}
            body = json.dumps(data)
            self.ws.send(body)


class ChatLocust(locust.HttpUser):
    tasks = [ChannelTaskSet]
