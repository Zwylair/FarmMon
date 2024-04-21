from flask import Flask
import random
import threading
import time

app = Flask(__name__)
CURRENT_INT = 0


def change_current_int():
    global CURRENT_INT
    while True:
        CURRENT_INT = random.randint(0, 1000)
        time.sleep(1)


def generate_random_number():
    print(f'sent: {CURRENT_INT}')
    yield str(CURRENT_INT)


@app.route('/')
def stream_random_number():
    return app.response_class(generate_random_number(), mimetype='text/plain')


if __name__ == '__main__':
    change_int_thread = threading.Thread(target=change_current_int)
    server_thread = threading.Thread(target=app.run, kwargs={'host': '0.0.0.0', 'port': 5000})
    change_int_thread.start()
    server_thread.start()
