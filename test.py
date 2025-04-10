import asyncio
import serial
import websockets
import random

SERIAL_PORT = "COM5"
BAUDRATE = 9600


greenhouse_id = None
ser = serial.Serial(SERIAL_PORT, BAUDRATE, timeout=1)

async def wait_for_ready():
    while True:
        line = ser.readline().decode().strip()
        if "READY" in line:
            print("[ARDUINO READY]")
            break

async def connect_channel():
    global greenhouse_id
    uri = "ws://localhost:8080/connect"
    async with websockets.connect(uri) as websocket:
        greenhouse_id = await websocket.recv()
        print(f"[CONNECTED] Received id: {greenhouse_id}")
        ser.write(f"ID:{greenhouse_id}\n".encode())

        async def handle_incoming():
            try:
                async for message in websocket:
                    if message == "update_request":
                        print("[SERVER REQUEST] Requesting Arduino data")
                        ser.write(b"update_request\n")
                    elif message == "start_watering":
                        print("[SERVER REQUEST] Starting watering")
                        ser.write(b"start_watering\n")
            except websockets.exceptions.ConnectionClosed:
                print("[DISCONNECTED] Server closed connection")

        async def send_ping():
            try:
                while True:
                    await websocket.send("pong")
                    await asyncio.sleep(10)
            except websockets.exceptions.ConnectionClosed:
                pass

        await asyncio.gather(handle_incoming(), send_ping())

async def read_from_serial_and_send_data():
    uri = "ws://localhost:8080/data"
    while True:
        line = ser.readline().decode().strip()
        if line and greenhouse_id and not line.startswith("["):
            async with websockets.connect(uri) as websocket:
                await websocket.send(line)
                print(f"[DATA SENT] {line}")
        await asyncio.sleep(0.1)

async def main():
    await wait_for_ready()
    await asyncio.gather(
        connect_channel(),
        read_from_serial_and_send_data()
    )

if __name__ == "__main__":
    asyncio.run(main())
