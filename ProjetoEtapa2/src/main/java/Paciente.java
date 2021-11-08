import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Paciente {
    public static void main(String[] args) throws UnknownHostException, IOException {

        // Comunicação entre os sockets
        DataOutputStream ostream = null;
        DataInputStream istream = null;
        Scanner s = new Scanner(System.in);

        System.out.println("Conectando-se ao servidor");
        Socket cliente = new Socket("127.0.0.1", 12345);

        istream = new DataInputStream(cliente.getInputStream());
        ostream = new DataOutputStream(cliente.getOutputStream());

        String mrcv = istream.readUTF();//le mensagem do servidor.
        System.out.println(mrcv);
        
        System.out.println("Bem Vindo ao Portal Paciente.\n\n##Indentificacao");
        System.out.println("Nome: ");
        String paciente = s.nextLine();
        
        while (true) {
        	
        	// Menu de opcoes
            System.out.println("\n\n"
            +"--"+paciente+"--\n"
            +"Informe comando:\n"
            +"## - inserirAgendamento(cpf, titulo, descricao)\n"
            +"## - modificarAgendamento(cpf, titulo, descricao)\n"
            +"## - listarAgendamento(cpf)\n"
            +"## - apagarAgendamento(cpf, titulo, descricao)\n"
            +"## - apagarTodosAgendamentos(cpf)\n"
            +"## - SAIR\n"
            +"Opcao: ");
            String op = s.nextLine();
            
            // Enviando comando
            ostream.writeUTF(op);
            ostream.flush();

            if(op.equals("SAIR")){
                System.out.println("Desconectado");
                break;
            }
            
            // Retorno
            System.out.println(istream.readUTF());
        }

        s.close();
        cliente.close();
    }
}