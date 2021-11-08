import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.ClientId;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.thirdparty.com.google.gson.Gson;
import org.apache.ratis.thirdparty.com.google.gson.reflect.TypeToken;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

public class PortalPaciente {

	// Selecionar apenas dados do comando enviaod pelo paciente
    public static String filtraDados(String msg){
        
        int flag = 0;
        String result = "";

        for (int i = 0; i < msg.length(); i++) {
			if(i-1 != -1 && msg.charAt(i-1) == '(') {
				flag = 1;
			}else if(msg.charAt(i) == ')') {
				break;
			}
			
			if(flag == 1) {
				result += msg.charAt(i);
			}
		}
        return result;
    }

    public static void main(String[] args) throws IOException {
        
    	// Ratis
    	String raftGroupId = "raft_group__dois";

        Map<String,InetSocketAddress> id2addr = new HashMap<>();
        id2addr.put("t4", new InetSocketAddress("127.0.0.1", 4100));
        id2addr.put("t5", new InetSocketAddress("127.0.0.1", 4200));
        id2addr.put("t6", new InetSocketAddress("127.0.0.1", 4300));

        List<RaftPeer> addresses = id2addr.entrySet()
                .stream()
                .map(e -> RaftPeer.newBuilder().setId(e.getKey()).setAddress(e.getValue()).build())
                .collect(Collectors.toList());

        final RaftGroup raftGroup = RaftGroup.valueOf(RaftGroupId.valueOf(ByteString.copyFromUtf8(raftGroupId)), addresses);
        RaftProperties raftProperties = new RaftProperties();

        RaftClient client = RaftClient.newBuilder()
                                      .setProperties(raftProperties)
                                      .setRaftGroup(raftGroup)
                                      .setClientRpc(new GrpcFactory(new Parameters())
                                      .newRaftClientRpc(ClientId.randomId(), raftProperties))
                                      .build();
        

        // Comunicação entre sockets
        DataOutputStream ostream = null;
        DataInputStream istream = null;
        Scanner s = new Scanner(System.in);
        String comando;
        Gson json = new Gson();
        
        //Iniciando socket servidor
        ServerSocket servidor = new ServerSocket(12345);
        while(true){

            System.out.println("Aguardando conexao...");
            
            // Conexão realizada
            Socket cliente = servidor.accept();
            ostream = new DataOutputStream(cliente.getOutputStream());
            istream = new DataInputStream(cliente.getInputStream());
            ostream.writeUTF("Conectado...");
            ostream.flush();
            
            System.out.println("Nova conexão com " +  cliente.getInetAddress().getHostAddress());
            
            //Iniciando conversa
            while(true){
                System.out.println("Esperando mensagem");
                comando = istream.readUTF();
                System.out.println(comando);
                
                // Encerrando conexão com cliente
                if(comando.equals("SAIR")){
                    System.out.println("Conexao encerrada.");
                    break;
                }

                // Identificando comando
                int op = 0;
                if(comando.indexOf("inserir") == 0){
                    op = 1;
                } else if(comando.indexOf("modificar") == 0){
                    op = 2;
                } else if(comando.indexOf("listar") == 0){
                    op = 3;
                } else if(comando.indexOf("apagarAgendamento") == 0){
                    op = 4;
                } else if(comando.indexOf("apagarTodosAgendamentos") == 0){
                    op = 5;
                }
                
                String retorno = "";
                String arg = filtraDados(comando).replaceAll(" ", ""); // Remove espacos em branco
                String lsDados[] = null;
                
                // Verifica se dados inseridos são validos
                if(arg != null){
                    lsDados = arg.split(",");
                } else {
                    op = 0;
                }
                
                
                // Variaveis para uso do controle dos dados nos servidores.
                RaftClientReply getValue;
                String response, agendamentoJson = "";
                ArrayList<Agendamento> liAgendamento = null;
                
                // Consulta Chave sera usado em todas as solicitações do Paciente
            	getValue = client.io().sendReadOnly(Message.valueOf("get:" + lsDados[0]));
            	response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
            	response = response.substring(4); //Retirar get da string
                
                //Inserir
                if(op == 1 && lsDados.length == 3) {
                	Agendamento agNovo = new Agendamento(lsDados[0], lsDados[1], lsDados[2]);
                	
                	// Se não tiver nenhum agendamento criar um vetor e inserir primeiro agendamento
                	if(response.equals("null")) {
                		liAgendamento = new ArrayList<>();
                		liAgendamento.add(agNovo);
  
                	// Inserir novo agendamento
                	}else {
                		response = response.replaceAll("-", ":");
                		liAgendamento = json.fromJson(response,  new TypeToken<ArrayList<Agendamento>>(){}.getType()); 
                		liAgendamento.add(agNovo);
                	}
                	
                	// Retorna padrão de armazenamento e inserir valor
                	agendamentoJson = json.toJson(liAgendamento).replaceAll(":", "-");
                    getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + agendamentoJson));
                    retorno = "Inserido com sucesso!!\n";
                    
                //Modificar
                }else if(op == 2 && lsDados.length == 3) {
                	Agendamento agNovo = new Agendamento(lsDados[0], lsDados[1], lsDados[2]);
                	
                	if(response.equals("null")) {
                		retorno = "Paciente não tem nenhum agendamento!!\n";
                	}else {
                		retorno = "Agendamento não existe!!\n";// Se não encontrar agendamento
                		response = response.replaceAll("-", ":");
                		liAgendamento = json.fromJson(response,  new TypeToken<ArrayList<Agendamento>>(){}.getType()); 
                		for (Agendamento agAnt : liAgendamento) {
							if(agNovo.getCpf().equals(agAnt.getCpf()) && agNovo.getDescricao().equals(agAnt.getDescricao()) && agNovo.getTipo().equals(agAnt.getTipo())) {
								// Modificação
								agAnt.setDescricao(agNovo.getDescricao());
								agAnt.setTipo(agNovo.getTipo());
								
								retorno = "Agendamento Paciente portador do cpf: "+ agNovo.getCpf() + 
										"\nTipo: "+ agAnt.getTipo() +
										"\nDescricao: "+ agAnt.getDescricao() +
										"\n\nModificado para:"+
										"\nTipo: "+ agNovo.getTipo() +
										"\nDescricao: "+ agNovo.getDescricao() +"\n";
								break;
							}
						}
                		
                    	agendamentoJson = json.toJson(liAgendamento).replaceAll(":", "-");
                        getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + agendamentoJson));
                	}

                //Listar
                }else if(op == 3 && lsDados.length == 1) {
                	
                	if(response.equals("null")) {
                		retorno = "Paciente não possui agendamentos!!\n";
                	}  else {
                		response = response.replaceAll("-", ":");
                		liAgendamento = json.fromJson(response,  new TypeToken<ArrayList<Agendamento>>(){}.getType()); 
                		retorno = "Agendamentos Paciente portador do cpf: "+ lsDados[0] + "\n";
                		for (Agendamento agAnt : liAgendamento) {
							retorno += "\n\nTipo: "+ agAnt.getTipo() +
							"\nDescricao: "+ agAnt.getDescricao();
						}         
                	}

                //ApagarUm
                }else if(op == 4 && lsDados.length == 3) {
                	Agendamento agNovo = new Agendamento(lsDados[0], lsDados[1], lsDados[2]);
                	
                	if(response.equals("null")) {
                		retorno = "Paciente não possui agendamentos!!\n";
                	}else {
                		retorno = "Agendamento não existe!!\n";// Se não encontrar agendamento
                		response = response.replaceAll("-", ":");
                		liAgendamento = json.fromJson(response,  new TypeToken<ArrayList<Agendamento>>(){}.getType()); 
                		for (Agendamento agAnt : liAgendamento) {
							if(agNovo.getCpf().equals(agAnt.getCpf()) && agNovo.getDescricao().equals(agAnt.getDescricao()) && agNovo.getTipo().equals(agAnt.getTipo())) {
								liAgendamento.remove(agAnt);

								retorno = "Agendamento removido Paciente portador do cpf: "+ agNovo.getCpf() + 
										"\nTipo: "+ agAnt.getTipo() +
										"\nDescricao: "+ agAnt.getDescricao() +"\n";
								break;
							}
						}
                		
                    	agendamentoJson = json.toJson(liAgendamento).replaceAll(":", "-");
                        getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + agendamentoJson));
                	}
                	
                //ApagarTodos
                }else if(op == 5 && lsDados.length == 1) {
                	if(response.equals("null")) {
                		retorno = "Paciente não possui agendamentos!!\n";
                	}else {
                		
                        getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + "null"));
                		retorno = "Todos os agendamento removido do Paciente portador do cpf: "+ lsDados[0] +"\n";
                	}
                	
                }else {
                	retorno = "Comando invalido!!\n";
                }

                System.out.println("\nRETORNO: \n"+retorno);
                
                ostream.writeUTF(retorno);
                ostream.flush();
            }


            // Iniciando nova conexao
            System.out.println("Deseja aguardar nova conexao? (S/N)");
            comando = s.nextLine();

            if(comando.equals("N")){
                break;
            }

            cliente.close();
        }

        s.close();
        servidor.close();
        client.close();
    }
}