import socket

s = socket.socket()
host = socket.gethostname()
port = 12345
s.bind((host, port))

# Aguardando conexão
s.listen(5)

while True:
	print("Esperando conexão")

	# Conectando
	c, addr = s.accept()
	c.send("Conectado\n".encode())
	msgRec = c.recv(1024)
	print(msgRec.decode())

	# Iniciando conversa
	while True:
	
		# Recebendo mensagem
		print("Esperando mensagem")
		msgRec = c.recv(1024)
	
		# Encerrando conexão
		if(msgRec.decode() == "SAIR"):
			print("Conexão encerrada.")
			break;

		print("Mensagem recebida: ", msgRec.decode())
	
		# Enviando mensagem
		msgEnv = input("Digite resposta: ")
		c.send(msgEnv.encode())
		print("Resposta enviada.")

	
	c.close()
