import socket

s = socket.socket()
host = socket.gethostname()
print("host name: " + host)
port = 12345

print("Conectando-se ao servidor")
# Conectando
s.connect((host, port))

# Confirmando conexao
data = s.recv(1024)
print(data.decode())
s.send("Conectado".encode())

#Portal Administrador
print("Bem Vindo ao Portal Paciente.")
print("##Identificação")
paciente = input("Nome: ")

# Iniciando conversa
while True:
    print("\n\n")
    print("--"+paciente+"--\n")
    print("Informe comando:")
    print("## - inserirAgendamento(cpf, titulo, descricao)\n"
          "## - modificarAgendamento(cpf, titulo, descricao)\n"
          "## - listarAgendamento(cpf)\n"
          "## - apagarAgendamento(cpf, titulo)\n"
          "## - apagarTodosAgendamentos(cpf)\n"
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