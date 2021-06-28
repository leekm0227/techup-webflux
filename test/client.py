import json
import time
import gevent

from websocket import create_connection

import locust


class ChannelTaskSet(locust.TaskSet):
    def __init__(self, parent):
        super().__init__(parent)
        self.ws = create_connection('ws://localhost:22222/v1/channel')
        self.channel_id = ""
        self.count = 0

    def on_start(self):
        def _receive():
            while True:
                res = self.ws.recv()
                data = json.loads(res)

                if data["payloadType"] == "START_TEST":
                    self.channel_id = data["receiver"]
                elif data['txtime']:
                    locust.events.request_success.fire(
                        request_type='recv',
                        name=data['body'],
                        response_time=int((time.time() - data['txtime']) * 1000),
                        response_length=len(res),
                    )

        gevent.spawn(_receive)

    def on_quit(self):
        self.ws.close()

    @locust.task
    def sent(self):
        if self.channel_id != "" and self.count < 1000:
            data = {
                "payloadType": 4,
                "receiveType": 1,
                "receiver": self.channel_id,
                "txtime": time.time(),
                "body": "test msg " + str(self.count)
            }
            body = json.dumps(data)
            self.ws.send(body)
            self.count += 1


class ChatLocust(locust.HttpUser):
    tasks = [ChannelTaskSet]
    min_wait = 2000
    max_wait = 3000
