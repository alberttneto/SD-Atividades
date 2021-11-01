
public class ModificaString {
	
	private StringBuilder msg;
	
	public ModificaString(String msg) {
		this.msg = new StringBuilder(msg);
	}
	
	// Metodo que modifica o caractere para maiusulo
	public void LetraMaiuscula() {
		
		for (int i = 0; i < msg.length(); i++) {

			char letra = msg.charAt(i); // Proxima letra
			if(Character.isLowerCase(letra)) { // Se for minuscula modifica

				this.msg.setCharAt(i, Character.toUpperCase(letra));					
				break;
			}
		}
	}
	
	// Retorna mensagem
	public String getMsg() {
		return msg.toString();
	}
}
