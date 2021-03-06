package br.com.saat.model;

public class AvaliacaoResultado {

	private int idAvaliacaoFisicaResultado;
	private CategoriaAtividade categoriaAtividade;
	private float desempenho;
	private String resultado;
	
	public int getIdAvaliacaoFisicaResultado() {
		return idAvaliacaoFisicaResultado;
	}
	public void setIdAvaliacaoFisicaResultado(int idAvaliacaoFisicaResultado) {
		this.idAvaliacaoFisicaResultado = idAvaliacaoFisicaResultado;
	}
	public CategoriaAtividade getCategoriaAtividade() {
		return categoriaAtividade;
	}
	public void setCategoriaAtividade(CategoriaAtividade categoriaAtividade) {
		this.categoriaAtividade = categoriaAtividade;
	}
	public float getDesempenho() {
		return desempenho;
	}
	public void setDesempenho(float desempenho) {
		this.desempenho = desempenho;
	}
	public String getResultado() {
		return resultado;
	}
	public void setResultado(String resultado) {
		this.resultado = resultado;
	}
	
}
