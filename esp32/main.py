from machine import Pin
from time import sleep
from urandom import randint
import urequests, json

class DHT11(object):
    def __init__(self):
        self.t = None
        self.h = None
    def read(self):
        self.t = randint(22, 45)
        self.h = randint(45, 75)
        return self.t, self.h

def do_connect(ssid: str, password: str):
    import network
    wlan = network.WLAN(network.STA_IF)
    wlan.active(False)
    sleep(1)
    wlan.active(True)
    if wlan.isconnected():
        wlan.disconnected()
    if not wlan.isconnected():
        print('Connecting to network', end='')
        wlan.connect(ssid, password)
        while not wlan.isconnected():
            sleep(1)
            print(".", end='')
            pass
    if wlan.isconnected():
        print(" success.")
        print('Network config:', wlan.ifconfig()) 
    else:
        print('Failed to connect to WiFi')
        
def send(temp, humi):
    url = 'https://controller-d86b2-default-rtdb.asia-southeast1.firebasedatabase.app/data.json'
    data = {
        "temperature": temp,
        "humidity": humi
    }
    headers = {
        "Content-Type": "application/json"
    }
    try:
        response = urequests.post(url, data=json.dumps(data), headers=headers)
        print("Response:", response.text)
        response.close()
    except Exception:
        print(f"{Exception}")

if __name__ == "__main__":
    do_connect("Tra Tien Huong", "TIENHUONGNVN")
    dht11 = DHT11()
    while True:
        temp, humi = dht11.read()
        print(f"Temp: {temp}, Humi: {humi}")
        send(temp, humi)
        sleep(1)