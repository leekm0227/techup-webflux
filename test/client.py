import json
import time

import gevent
import locust
from websocket import create_connection


class ChannelTaskSet(locust.TaskSet):
    wait_time = locust.between(0.5, 1)

    def __init__(self, parent):
        super().__init__(parent)
        self.ws = create_connection('ws://localhost:22222/v2/channel')

    def on_start(self):
        def _receive():
            while True:
                try:
                    res = self.ws.recv()
                    data = json.loads(res)

                    if data['payloadType'] == 'TEST':
                        locust.events.request_success.fire(
                            request_type='recv1',
                            name='recv1',
                            response_time=round(time.time() * 1000) - int(data['regTime']),
                            response_length=len(res),
                        )
                    else:
                        locust.events.request_success.fire(
                            request_type='recv2',
                            name='recv2',
                            response_time=round(time.time() * 1000) - int(data['regTime']),
                            response_length=len(res),
                        )
                except Exception as e:
                    locust.events.request_failure.fire(
                        request_type='recv',
                        name='recv',
                        response_time=0,
                        response_length=0,
                        exception=e
                    )

        gevent.spawn(_receive)

    def on_quit(self):
        self.ws.close()

    @locust.task
    def send(self):
        try:
            data = {
                "payloadType": 0,
                "code": 0,
                "regTime": round(time.time() * 1000)
            }
            body = json.dumps(data)
            self.ws.send(body)
            locust.events.request_success.fire(
                request_type='send1',
                name='send1',
                response_time=round(time.time() * 1000) - int(data['regTime']),
                response_length=len(data),
            )
        except Exception as e:
            locust.events.request_failure.fire(
                request_type='send1',
                name='send1',
                response_time=0,
                response_length=0,
                exception=e
            )

    @locust.task
    def send2(self):
        try:
            data = {
                "payloadType": 1,
                "code": 0,
                "regTime": round(time.time() * 1000)
            }
            body = json.dumps(data)
            self.ws.send(body)
            locust.events.request_success.fire(
                request_type='send2',
                name='send2',
                response_time=round(time.time() * 1000) - int(data['regTime']),
                response_length=len(data),
            )
        except Exception as e:
            locust.events.request_failure.fire(
                request_type='send1',
                name='send1',
                response_time=0,
                response_length=0,
                exception=e
            )


class ChatLocust(locust.HttpUser):
    tasks = [ChannelTaskSet]
