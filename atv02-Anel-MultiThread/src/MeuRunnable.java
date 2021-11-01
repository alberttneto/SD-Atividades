
public class MeuRunnable extends Thread{

	private ModificaString ms;

	public MeuRunnable(ModificaString ms) {
		this.ms = ms;
	}

	@Override
	public void run() {
		try {
			
			// Imprimir mensagem
			System.out.println(Thread.currentThread().getName() + ": " + ms.getMsg());
			
			// Torna o proximo caractere minusculo em maiusculo
			ms.LetraMaiuscula();
			
			// Thread dormir por 1 seg
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
