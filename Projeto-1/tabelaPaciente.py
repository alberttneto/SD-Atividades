import json

class Cliente:
    
    def __init__(self, cpf, nome, telefone):
        self.cpf = cpf
        self.nome = nome
        self.telefone = telefone
        
class HashCliente:
    
    def __init__(self, tamanho):
        self.__qtd = 0 # qtd inserida
        self.__TABLE_SIZE = tamanho # tamanho da tabela
        self.__itens = [None for i in range(tamanho)] # vetor que armazena dados
        
    # Criando chave pelo metodo de divisao
    def __chaveDivisao(self,chave):
        return (int(chave) & 0x7FFFFFFF) % self.__TABLE_SIZE
    
    # inserção de cliente na tabela
    def inserirCliente(self, cpf, nome, telefone):
        if(self.__qtd == self.__TABLE_SIZE):
            return "Erro: Tabela cheia"
        
        chave = cpf
        pos = self.__chaveDivisao(chave)
        
        # Se cliente já existe retorna erro                        
        if self.__itens[pos] != None:
            return "Erro: Cliente já existe"
        
        # Se não existir inseri.
        self.__itens[pos] = json.dumps(Cliente(cpf, nome, telefone).__dict__)
        self.__qtd = self.__qtd + 1
        
        return "Cliente inserido!" 

    # Modifica dado na tabela
    def modificaCliente(self, cpf, nome, telefone):
        
        chave = cpf
        pos = self.__chaveDivisao(chave)
        
        # Se cliente já existe retorna erro                        
        if self.__itens[pos] == None:
            return "Erro: Cliente não existe"
        
        # Se não existir inseri.
        self.__itens[pos] = json.dumps(Cliente(cpf, nome, telefone).__dict__)
        self.__qtd = self.__qtd + 1

        return "Cliente modificado!"
    
    # Busca por cliente
    def recuperaCliente(self, cpf):
        chave = cpf
        pos = self.__chaveDivisao(chave)
        
        if self.__itens[pos] == None:
            return "Erro: Cliente não existe"
            
        return self.__itens[pos]
    
    # Remover cliente.
    def apagarCliente(self, cpf):
        chave = cpf
        pos = self.__chaveDivisao(chave)
        
        if self.__itens[pos] == None:
            return "Erro: Cliente não existe"
        else:
            self.__itens[pos] = None
            return "Cliente removido!"
    
