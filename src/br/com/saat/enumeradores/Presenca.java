package br.com.saat.enumeradores;

public enum Presenca {
	Presente (1, "Presen�a em treino"),
	Falta (2, "Falta"),
	Torneio (3, "Presen�a em torneio"),
	Medico (4, "Falta m�dica"),
	Nutricionista (5, "Nutricionista"),
	Fisioterapeuta (6, "Fisioterapeuta"),
	Psicologo (7, "Psic�logo"),
	Escola(8, "Escola"),
	Outros (9, "Outro");
	
	private final int valor;
	private final String nome;
	
	private Presenca(int valor, String nome) {
		this.valor = valor;
		this.nome = nome;
	}
	
	public int getValor(){
		return valor;
	}
	
	public String getNome(){
		return nome;
	}
}
