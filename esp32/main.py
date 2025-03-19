from machine import Pin
from time import sleep
from urandom import randint
import urequests, json, _thread

# Khởi tạo LED trên GPIO 8
led = Pin(8, Pin.OUT)

class DHT11:
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
        wlan.disconnect()
    if not wlan.isconnected():
        print('Connecting to network', end='')
        wlan.connect(ssid, password)
        while not wlan.isconnected():
            sleep(1)
            print(".", end='')
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
    headers = {"Content-Type": "application/json"}
    
    try:
        response = urequests.post(url, data=json.dumps(data), headers=headers)
        print("Response:", response.text)
        response.close()
    except Exception as e:
        print("Error sending data:", e)

def receive_led_status():
    url = 'https://controller-d86b2-default-rtdb.asia-southeast1.firebasedatabase.app/led_status.json'
    
    while True:
        try:
            response = urequests.get(url)
            if response.status_code == 200:
                data = response.json()
                if "led_status" in data:
                    led_status = data["led_status"]  # Lấy giá trị bên trong led_status
                    print("Received LED status:", led_status)
                    
                    # Điều khiển LED GPIO 8
                    if led_status == "ON":
                        led.value(0)  # Bật LED
                        print("LED ON")
                    else:
                        led.value(1)  # Tắt LED
                        print("LED OFF")
                else:
                    print("Error: 'led_status' key not found in response")
            response.close()
        except Exception as e:
            print("Error receiving LED status:", e)
        
        sleep(1)  # Kiểm tra mỗi 1 giây

if __name__ == "__main__":
    do_connect("Tra Tien Huong", "TIENHUONGNVN")
    dht11 = DHT11()

    # Chạy uthread để nhận tín hiệu LED từ Firebase
    _thread.start_new_thread(receive_led_status, ())

    while True:
        temp, humi = dht11.read()
        print(f"Temp: {temp}, Humi: {humi}")
        send(temp, humi)
        sleep(1)
