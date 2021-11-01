import socket

s = socket.socket()
host = socket.gethostname()
port = 12345

print("Conectando-se ao servidor")
# Conectando
s.connect((host, port))

# Confirmando conexao
data = s.recv(1024)
print(data.decode())
s.send("Conectado".encode())

# Iniciando conversa
while True:
	
	# Enviando mensagem
	msgEnv = input("Digite mensagem: ")
	s.send(msgEnv.encode())
	print("Mensagem enviada")

	# Encerrando client
	if msgEnv == "SAIR":
		print("Desconectado.")
		break

	# Recendo mensagem
	print("Esperando resposta")
	msgRec = s.recv(1024)
	print("Resposta recebida: ", msgRec.decode())
	
	
s.close()
