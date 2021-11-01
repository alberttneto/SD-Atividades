import socket

s = socket.socket()
host = socket.gethostname()
port = 5000

print("Conectando-se ao servidor")
# Conectando
s.connect((host, port))

# Confirmando conexao
data = s.recv(1024)
print(data.decode())
s.send("Conectado".encode())

#Portal Administrador
print("Bem Vindo ao Portal Adm.")
print("##Identificação")
adm = input("Dr(a):")


# Iniciando conversa
while True:
    print("\n\n")
    print("--Dr(a) "+adm+"--\n")
    print("Informe comando:")
    print("## - inserirPaciente(cpf, nome, telefone)\n"
          "## - modificarPaciente(cpf, nome, telefone)\n"
          "## - recuperarPaciente(cpf)\n"
          "## - apagarPaciente(cpf)\n"
          "## - SAIR\n")

    op = input("opção:")

    # Enviando mensagem
    s.send(op.encode())

    # Encerrando client
    if op == "SAIR":
        print("Desconectado.")
        break

    # Recendo mensagem
    print("Esperando resposta")
    msgRec = s.recv(1024)
    print("Resposta recebida:\n", msgRec.decode())


s.close()