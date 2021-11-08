import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.*;
import org.apache.ratis.thirdparty.com.google.gson.Gson;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class PortalAdm
{
	
	// Filtra informa��es enviadas pelo Adm
	public static String filtraDados(String msg) throws UnknownHostException, IOException {
        
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
	
	
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        
    	
    	// Ratis
    	String raftGroupId = "raft_group____um";

        Map<String,InetSocketAddress> id2addr = new HashMap<>();
        id2addr.put("t1", new InetSocketAddress("127.0.0.1", 3000));
        id2addr.put("t2", new InetSocketAddress("127.0.0.1", 3500));
        id2addr.put("t3", new InetSocketAddress("127.0.0.1", 4000));

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
        
        // Comunicacao entre sockets
        DataOutputStream ostream = null;
        DataInputStream istream = null;
        Scanner s = new Scanner(System.in);
        String comando;
        Gson json = new Gson();
        
        //Iniciando socket servidor
        ServerSocket servidor = new ServerSocket(5000);
        
        while(true){

            System.out.println("Aguardando conexao...");
            
            // Conex�o realizada
            Socket cliente = servidor.accept();
            ostream = new DataOutputStream(cliente.getOutputStream());
            istream = new DataInputStream(cliente.getInputStream());
            ostream.writeUTF("Conectado...");
            ostream.flush();
            
            System.out.println("Nova conexao com " +  cliente.getInetAddress().getHostAddress());
            
            //Iniciando conversa
            while(true){
                System.out.println("Esperando mensagem");
                comando = istream.readUTF();
                System.out.println(comando);
                
                // Encerrando conex�o com cliente
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
                } else if(comando.indexOf("recuperar") == 0){
                    op = 3;
                } else if(comando.indexOf("apagar") == 0){
                    op = 4;
                } 

                String retorno = "";
                String arg = filtraDados(comando).replaceAll(" ", "");
                String lsDados[] = null;
    
                if(arg != null){
                    lsDados = arg.split(",");
                } else {
                    op = 0;
                }
                
                String cJson = "";
                
                if(op == 1 || op == 2) {
                	Cliente c = new Cliente(lsDados[0],lsDados[1],lsDados[2]);
                    cJson = json.toJson(c).replaceAll(":", "-"); 
                }

                RaftClientReply getValue;
                String response;
                
                // Consulta Chave sera usado em todas as solicitações do Paciente
            	getValue = client.io().sendReadOnly(Message.valueOf("get:" + lsDados[0]));
            	response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
            	response = response.substring(4); //Retirar get da string
                
                if(op == 1 && lsDados.length == 3) {  
                	if(!response.equals("null")) {
                		retorno = "Paciente já existe!\n";
                	}else {
                        getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + cJson));
                        response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
                        retorno = "Inserido com sucesso!!\n";
                	}

                }else if(op == 2 && lsDados.length == 3) {
                	if(response.equals("null")) {
                		retorno = "Paciente não existe!\n";
                	}else{
                		
                		getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + cJson));
                		response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
                		retorno = "Modificado com sucesso!!\n";
                	}
                }else if(op == 3 && lsDados.length == 1) {
                	if(response.equals("null")) {
                		retorno = "Paciente não existe!\n";
                	}else {
                		response = response.replaceAll("-", ":");
                		Cliente cli = json.fromJson(response, Cliente.class);
                		retorno = "Paciente: "+ cli.getNome() + "\nCPF: " + cli.getCpf() + "\nTelefone: "+ cli.getTelefone();
                	}

                }else if(op == 4 && lsDados.length == 1) {
                	if(response.equals("null")) {
                		retorno = "Paciente não existe!\n";
                	}else {
                		getValue = client.io().send(Message.valueOf("add:" + lsDados[0] + ":" + "null"));
                		response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
                		retorno = "Deletado com sucesso!!\n";                		
                	}
                	
                }else {
                	retorno = "Comando invalido";
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