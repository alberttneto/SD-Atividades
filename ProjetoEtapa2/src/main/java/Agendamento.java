
public class Agendamento {
	
	private String cpf;
	private String tipo;
	private String descricao;
	
	public Agendamento(String cpf, String tipo, String descricao) {
		this.tipo = tipo;
		this.descricao = descricao;
		this.cpf = cpf;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	
	
	

	
	
}
