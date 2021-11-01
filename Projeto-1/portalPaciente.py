from tabelaAgendamento import HashAgendamento
from queue import Queue
import time
import socket
import paho.mqtt.client as mqtt 

s = socket.socket()
host = socket.gethostname()
port = 12345
s.bind((host, port))

# Aguardando conexão
s.listen(5)

# Tabela Hash com 30 possiveis chaves e 10 tarefas por chave.
h = HashAgendamento(30)

# Filtrar os dados de entrada enviado pelo usuario
def filtraDados(msg):
    
    flag = 0
    result = ""
        
    for i in range(len(msg)):
        
        if msg[i-1] == '(':
            flag = 1
        elif msg[i] == ')':
            return result

        if flag == 1:
            result += msg[i]

# Armazena mensagens recebidas via mosquitto
q = Queue()

# Função para armazenar as mensagens recebidas
def on_message(client, userdata, message):
   q.put(message)

# Autenticar usuario
def solicitaAutenticacao(cid, idPortal):
    broker_address="test.mosquitto.org" # Endereço do servidor
    client = mqtt.Client(idPortal) # Criar instancia do cliente
    client.on_message=on_message
    client.connect(broker_address) # Conectando com servidor
    client.loop_start() 

    client.subscribe("retorno",1)
    client.publish("autenticar",str(cid))
    retorno = 1
    time.sleep(20)
    while not q.empty():
        retorno = q.get()
        if retorno is None:
            continue
        retorno = str(retorno.payload.decode("utf-8"))
        print("received from queue",str(retorno.payload.decode("utf-8")))
    
    client.loop_stop() #stop the loop
    return retorno



            
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
        msg = c.recv(1024)
        comando = msg.decode()

        # Encerrando conexão
        if(comando == "SAIR"):
            print("Conexão encerrada.")
            break
        
        # Identifica comando
        op = 0
        if comando.find('inserir') == 0:
            op = 1
        elif comando.find('modificar') == 0:
            op = 2
        elif comando.find('listar') == 0:
            op = 3
        elif comando.find('apagarAgendamento') == 0:
            op = 4
        elif comando.find('apagarTodosAgendamentos') == 0:
            op = 5
        
        # Mensagem de retorno das operações
        retorno = ""

        # identifica comando solicitado
        # Recupera dados da mensagem
        if filtraDados(comando) != None:
            lsDados = filtraDados(comando)
            lsDados = lsDados.split(',')
            lsDados = [x.strip() for x in lsDados]
            #aut = solicitaAutenticacao(str(lsDados[0]), "P1")
        else:
            op = 0

        #if aut == 0:
        #    print("Erro: Paciente inexistente!!")
        #    continue

        if op == 1 and len(lsDados) == 3:
            retorno = h.inserirTarefa(lsDados[0], lsDados[1], lsDados[2])
        elif op == 2 and len(lsDados) == 3:
            retorno = h.modificaTarefa(lsDados[0], lsDados[1], lsDados[2])
        elif op == 3 and len(lsDados) == 1:
            retorno = h.listarTarefas(lsDados[0])
        elif op == 4 and len(lsDados) == 2:
            retorno = h.apagarTarefa(lsDados[0], lsDados[1])
        elif op == 5 and len(lsDados) == 1:
            retorno = h.apagarTarefas(lsDados[0])
        else:
            retorno = "Erro: Comando invalido"

        
        # Enviando retorno
        c.send(retorno.encode())
        print("Resposta enviada.")
    
    print("Deseja aguardar nova conexão?")
    conx =  input("(S/N): ")
    if conx == "N":
        break

    c.close()

s.close()