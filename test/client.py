import json
import time
import gevent

from websocket import create_connection

import locust


class ChannelTaskSet(locust.TaskSet):
    wait_time = locust.between(2, 5)

    def __init__(self, parent):
        super().__init__(parent)
        self.ws = create_connection('ws://localhost:22222/v2/channel')
        self.channel_id = ""

    def on_start(self):
        def _receive():
            while True:
                res = self.ws.recv()
                data = json.loads(res)
                res_time = time.time() * 1000

                if data["payloadType"] == "START_TEST":
                    self.channel_id = data["receiver"]
                elif data["payloadType"] == "BROADCAST":
                    response_time = res_time - data['txtime']
                    locust.events.request_success.fire(
                        request_type='recv',
                        name=data["payloadType"],
                        response_time=response_time,
                        response_length=len(res),
                    )

        gevent.spawn(_receive)

    def on_quit(self):
        self.ws.close()

    @locust.task
    def sent(self):
        if self.channel_id != "":
            data = {
                "payloadType": 4,
                "receiveType": 1,
                "receiver": self.channel_id,
                "txtime": time.time() * 1000,
                "body": "Welcome to the website. If you're here, you're likely looking to find random words. Random Word Generator is the perfect tool to help you do this. While this tool isn't a word creator, it is a word generator that will generate random words for a variety of activities or uses. Even better, it allows you to adjust the parameters of the random words to best fit your needs."
            }
            body = json.dumps(data)
            self.ws.send(body)


class ChatLocust(locust.HttpUser):
    tasks = [ChannelTaskSet]
