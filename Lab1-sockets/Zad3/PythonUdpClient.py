import socket

serverIP = "127.0.0.1"
serverPort = 9008

print('PYTHON UDP CLIENT')
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
msg_bytes = (300).to_bytes(4, byteorder='little')
client.sendto((msg_bytes), (serverIP, serverPort))
buff, addr = client.recvfrom(1024)
print("Received message: ", int.from_bytes(buff, byteorder='little'), " from ", addr[0], ":", addr[1],sep='')
