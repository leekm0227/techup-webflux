import json
import random
import time

import gevent
import locust
from websocket import create_connection


class ChannelTaskSet(locust.TaskSet):
    wait_time = locust.between(1, 2)

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
                    res_time = time.time() * 1000

                    if data["payloadType"] == "INFO":
                        self.session_id = data["sessionId"]
                        self.channel_id = data["channelId"]
                        self.pos = data["pos"]
                    elif data["payloadType"] == "MOVE" and data["sessionId"] == self.session_id:
                        x = self.pos[0] + data["dir"][0]
                        y = self.pos[0] + data["dir"][0]

                        if data["pos"][0] == x and data["pos"][0] == y:
                            locust.events.request_success.fire(
                                request_type='recv',
                                name='pos',
                                response_time=res_time - data['regTime'],
                                response_length=len(res),
                            )
                        else:
                            # print("fail:", self.session_id, self.pos, data['dir'], data['pos'])
                            locust.events.request_failure.fire(
                                request_type='recv',
                                name='pos',
                                response_time=res_time - data['regTime'],
                                response_length=len(res),
                                exception='not valid pos'
                            )

                        self.pos = data["pos"]

                    if data["payloadType"] == "MOVE":
                        # print(data['regTime'], res_time)
                        locust.events.request_success.fire(
                            request_type='recv',
                            name='recv',
                            response_time=res_time - data['regTime'],
                            response_length=len(res),
                        )
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
        # if self.channel_id != "" and self.pos and self.pos != self.tmpPos:
        if self.channel_id != "" and self.pos:
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
                    "regTime": time.time() * 1000,
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
