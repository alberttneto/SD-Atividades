import json

# Tarefas de uma clinica medica
# Agendamento de consultas, exames ou cirurgias
class Agendamento:
    
    def __init__(self, titulo, descricao):
        self.titulo = titulo
        self.descricao = descricao
            
class HashAgendamento:
    def __init__(self, tamanhoTabela):
        self.__qtd = 0 #Qtd de itens na tabela
        self.__TABLE_SIZE = tamanhoTabela
        self.__itens = [[] for j in range(tamanhoTabela)]
    
    # Geração de chave pelo metodo de divisão
    def __chaveDivisao(self,chave):
        return (int(chave) & 0x7FFFFFFF) % self.__TABLE_SIZE
    
    def inserirTarefa(self, cpf, titulo, descricao):
        if(self.__qtd == self.__TABLE_SIZE):
            return "Erro: Tabela cheia"
        
        chave = cpf
        pos = self.__chaveDivisao(chave)
        tarefa = json.dumps(Agendamento(titulo, descricao).__dict__)

        # Verifica se tarefa existe
        if self.__itens[pos] != []:
            for i in range(len(self.__itens[pos])):
                item = json.loads(self.__itens[pos][i])
                if item['titulo'] == titulo:
                    return "Erro: Agendamento já existe!"

        
        # Se não existir inserir.
        self.__itens[pos].append(json.dumps(Agendamento(titulo, descricao).__dict__))
        self.__qtd = self.__qtd + 1

        return "Agendado!!"

    def modificaTarefa(self, cpf, titulo, descricao):
        
        chave = cpf
        pos = self.__chaveDivisao(chave)
        tarefa = json.dumps(Agendamento(titulo, descricao).__dict__)
        flag = 0 # Saber se foi modificado a tarefa

        # Modifica tarefa
        if self.__itens[pos] != []:
            for i in range(len(self.__itens[pos])):
                item = json.loads(self.__itens[pos][i]) 
                if item['titulo'] == titulo:
                    self.__itens[pos][i] = tarefa
                    flag = 1
                    break

        # Erro se agendamento não existir
        if flag == 0:
            return "Erro: Agendamento não existe!"
        
        return "Tarefa modificada"
    
    def listarTarefas(self, cpf):

        chave = cpf
        pos = self.__chaveDivisao(chave)

        if self.__itens[pos] == []:
            return "Erro: Cliente não tem agendamentos"
        
        tarefas = ""
        # Imprimir cada tarefa do cliente
        for i in range(len(self.__itens[pos])):
            item = json.loads(self.__itens[pos][i])
            tarefas += "Titulo: "+item['titulo']+"\n"+"Descricao: "+item['descricao']+"\n"
       
        return tarefas

    def apagarTarefas(self, cpf):
        chave = cpf
        pos = self.__chaveDivisao(chave)

        if self.__itens[pos] == []:
            return "Erro: Cliente não tem agendamentos"

        self.__itens[pos] = []

        return "Agendamentos Apagados com sucesso"
        
    
    def apagarTarefa(self, cpf, titulo):
        chave = cpf
        pos = self.__chaveDivisao(chave)
        flag = 0
        
        # Apaga tarefa
        if self.__itens[pos] != []:
            for i in range(len(self.__itens[pos])):
                item = json.loads(self.__itens[pos][i])
                if item['titulo'] == titulo:
                    del self.__itens[pos][i]
                    flag = 1
                    break

        # Erro se agendamento não existir
        if flag == 0:
            return "Erro: Agendamento não existe!"
        
        return "Agendamento apagado!!"

    