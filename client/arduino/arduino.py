import asyncio
import serial
import websockets

SERIAL_PORT = "COM3"
BAUDRATE = 9600

ser = serial.Serial(SERIAL_PORT, BAUDRATE, timeout=1)

async def wait_for_ready():
    while "READY" not in (line := ser.readline().decode().strip()):
        pass
    print("[ARDUINO READY]")

async def connect_channel():
    uri = "ws://localhost:8080/connect"
    async with websockets.connect(uri) as ws:
        greenhouse_id = await ws.recv()
        print(f"[CONNECTED] ID: {greenhouse_id}")
        ser.write(f"ID:{greenhouse_id}\n".encode())

        async def listen():
            async for msg in ws:
                print(f"[SERVER] {msg}")
                ser.write(f"{msg}\n".encode())

        async def ping():
            while True:
                await ws.send("pong")
                await asyncio.sleep(10)

        await asyncio.gather(listen(), ping())

async def send_serial_data():
    uri = "ws://localhost:8080/data"
    while True:
        line = ser.readline().decode().strip()
        if line and not line.startswith("["):
            async with websockets.connect(uri) as ws:
                await ws.send(line)
                print(f"[CLIENT] {line}")
        await asyncio.sleep(0.1)

async def main():
    await wait_for_ready()
    await asyncio.gather(connect_channel(), send_serial_data())

if __name__ == "__main__":
    asyncio.run(main())
