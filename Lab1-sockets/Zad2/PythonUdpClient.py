import socket;

serverIP = "127.0.0.1"
serverPort = 9008
msg = "Żółta Gęś"

print('PYTHON UDP CLIENT')
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(bytes(msg, 'utf-8'), (serverIP, serverPort))
data, addr = client.recvfrom(1024)
print("Received message:", data.decode('utf-8'))



