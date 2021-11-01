from tabelaPaciente import HashCliente
from threading import Thread
from queue import Queue
import socket
import paho.mqtt.client as mqtt 
import time


class Th(Thread):

        def __init__ (self, idPortal, h):
            Thread.__init__(self)
            self.idPortal = idPortal
            self.h = h

        def run(self):
            autenticarPaciente(self.idPortal, h)

# Armazena mensagens recebidas via mosquitto
q = Queue()

# Função para armazenar as mensagens recebidas
def on_message(client, userdata, message):
   q.put(message)

# Autenticando usuario para o portal cliente
def autenticarPaciente(idPortal, h):
    broker_address="test.mosquitto.org"
    client = mqtt.Client(idPortal) 
    client.on_message=on_message # Defini metodo para receber mensagens
    client.connect(broker_address) # Conxão com servidor
    client.loop_start() 
    client.subscribe("autenticar",1)

    while 1:
        time.sleep(20) # wait
        cid = ""
        while not q.empty():
            message = q.get()
            if message is None:
                continue
            cid = str(message.payload.decode("utf-8"))
            print("received from queue",str(message.payload.decode("utf-8")))

        retorno = 1
        if h.recuperaCliente(cid).find("Erro"):
            retorno = 0
        
        client.publish("retorno", str(retorno))
    client.loop_stop() #stop the loop


s = socket.socket()
host = socket.gethostname()
port = 5000
s.bind((host, port))

# Aguardando conexão
s.listen(5)

# Tabela Hash
h = HashCliente(30)

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

while True:
    print("Esperando conexão")
    #t1 = Th("P2", h)
    #t1.start()

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
        elif comando.find('recuperar') == 0:
            op = 3
        elif comando.find('apagar') == 0:
            op = 4
        
        # Mensagem de retorno das operações
        retorno = ""
        
        # identifica comando solicitado     
        # Recupera dados da mensagem
        if filtraDados(comando) != None:
            lsDados = filtraDados(comando)
            lsDados = lsDados.split(',')
            lsDados = [x.strip() for x in lsDados]
        else:
            op = 0
        
        
        if op == 1:
            retorno = h.inserirCliente(lsDados[0], lsDados[1], lsDados[2])
        elif op == 2:
            retorno = h.modificaCliente(lsDados[0], lsDados[1], lsDados[2])
        elif op == 3:
            retorno = h.recuperaCliente(lsDados[0])
        elif op == 4:
            retorno = h.apagarCliente(lsDados[0])
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