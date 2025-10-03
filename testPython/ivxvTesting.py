import json
from socket import socket, AF_INET, SOCK_STREAM
from ssl import SSLContext, PROTOCOL_TLS_SERVER

host = "0.0.0.0"
port = 8080
cert = "fullchain.pem"
key = "privkey.pem"

context = SSLContext(PROTOCOL_TLS_SERVER)
context.load_cert_chain(cert, key)

try:
    with socket(AF_INET, SOCK_STREAM) as server:
        server.bind((host, port))
        server.listen()
        with context.wrap_socket(server, server_side=True) as tls:
            while True:
                connection, address = tls.accept()
                data = connection.recv(4096)
                if "GET" and "config.json" in data.decode():
                    response = json.load(open("config.json"))
                    connection.sendall(bytes("HTTP/1.1 200 OK".encode()))
                    connection.sendall(bytes("\r\n\r\n".encode()))
                    connection.sendall(bytes(json.dumps(response).encode()))
                    connection.sendall(bytes("\r\n\r\n".encode()))
                    connection.close()
                elif "voteid" and "6s4NCktCFFLNHLXd2SDDZA==" and "sessionid" and "19f2ec44573349ca093a82cd57489bf9" \
                        in data.decode():
                    response = json.load(open("voteResponse.json"))
                    connection.sendall(bytes(json.dumps(response).encode()))
                    connection.close()
except KeyboardInterrupt:
    server.close()
