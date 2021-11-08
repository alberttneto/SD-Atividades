import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Adm {
    public static void main(String[] args) throws UnknownHostException, IOException {

        // Comunica��o entre os sockets
        DataOutputStream ostream = null;
        DataInputStream istream = null;
        Scanner s = new Scanner(System.in);

        System.out.println("Conectando-se ao servidor");
        Socket cliente = new Socket("127.0.0.1", 5000);

        istream = new DataInputStream(cliente.getInputStream());
        ostream = new DataOutputStream(cliente.getOutputStream());

        String mrcv = istream.readUTF();//le mensagem do servidor.
        System.out.println(mrcv);
        
        
        System.out.println("Bem Vindo ao Portal Adm.\n\n##Indentificacao");
        System.out.println("Nome: ");
        String paciente = s.nextLine();

        while (true) {
            System.out.println("\n\n"
            +"--Dr(a)"+paciente+"--\n"
            +"Informe comando:\n"
            +"## - inserirPaciente(cpf, nome, telefone)\n"
            +"## - modificarPaciente(cpf, nome, telefone)\n"
            +"## - recuperarPaciente(cpf)\n"
            +"## - apagarPaciente(cpf)\n"
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