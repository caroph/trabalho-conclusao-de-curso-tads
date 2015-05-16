package br.com.saat.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.saat.model.AtividadeAvaliacao;
import br.com.saat.model.ConnectionFactory;

public class AtividadeAvaliacaoDAO {
	private Connection con;
	private PreparedStatement stmtScript;
	
	public AtividadeAvaliacaoDAO() throws Exception{
        con = ConnectionFactory.getConnection();
    }
	
	public AtividadeAvaliacaoDAO(Connection con) throws Exception{
        this.con = con;        
    }
	public List<AtividadeAvaliacao> buscarAtividades() throws SQLException {
		List<AtividadeAvaliacao> lista = new ArrayList<AtividadeAvaliacao>();
		
		stmtScript = con.prepareStatement("SELECT idAtividadeAvaliacao, idUnidadeDeMedida, capacidade, teste "
				+ "FROM atividadeAvaliacao "
				+ "WHERE flCadastroAtivo = 1");
		
		ResultSet rs = stmtScript.executeQuery();
		
		while(rs.next()){
			AtividadeAvaliacao atividade = new AtividadeAvaliacao();
			atividade.setIdAtividadeAvaliacao(rs.getInt(1));
			atividade.setIdUnidadeDeMedida(rs.getInt(2));
			atividade.setCapacidade(rs.getString(3));
			atividade.setTeste(rs.getString(4));
			lista.add(atividade);
		}
		return lista;
	}

	public boolean desativar(AtividadeAvaliacao atividade) throws SQLException {
		boolean retorno = false;
		
		stmtScript = con.prepareStatement("UPDATE atividadeAvaliacao "
				+ "SET flCadastroAtivo = 0 "
				+ "WHERE idAtividadeAvaliacao = ? ");
		
		stmtScript.setInt(1, atividade.getIdAtividadeAvaliacao());
		
		if(stmtScript.executeUpdate() > 0){
			retorno = true;
		}
		return retorno;	
	}

	public boolean inserir(AtividadeAvaliacao atividade) throws SQLException {
		boolean retorno = false;
		
		stmtScript = con.prepareStatement("INSERT INTO atividadeAvaliacao "
				+ "(idUnidadeDeMedida, capacidade, teste) "
				+ " VALUES (?, ?, ?)");
		
		stmtScript.setInt(1, atividade.getIdUnidadeDeMedida());
		stmtScript.setString(2, atividade.getCapacidade());
		stmtScript.setString(3, atividade.getTeste());
		
		if(stmtScript.executeUpdate() > 0){
			retorno = true;
		}
		return retorno;
	}

}