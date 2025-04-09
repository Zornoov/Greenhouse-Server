import asyncio
import websockets

async def communicate_with_socket():
    uri = "ws://localhost:8080/ws"

    async with websockets.connect(uri) as websocket:
        message = "Hello, server!"
        await websocket.send(message)
        print(f"Sent: {message}")

        response = await websocket.recv()
        print(f"Received: {response}")


        close_response = await websocket.recv()
        print(f"Received: {close_response}")

asyncio.get_event_loop().run_until_complete(communicate_with_socket())
