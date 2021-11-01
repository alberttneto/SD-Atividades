import java.util.concurrent.*;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		
		// Cria ThreadPool com o maximo de 30 Threads
		ExecutorService es2 = new ThreadPoolExecutor(30, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
		
		// Mensagem 80 caracteres.
		String msg = "A cada vez que um thread recebe a mensagem ele a imprime, modifica o primeiro car";
		// Classe para modificar a mensagem
		ModificaString ms = new ModificaString(msg);
		// Intancia runnable
		MeuRunnable r = new MeuRunnable(ms);
		
		// Iniciar threads até que seja feita a modificação da mensagem toda.
		int cont = 0;
		while(cont < msg.length()) {

			es2.execute(r);
			cont++;
		}
		
		// Encerra o ThreadPool
		es2.shutdown();
		
		while(!es2.isTerminated()) {}
		System.out.println("Mensagem: "+ms.getMsg());
	}
}
