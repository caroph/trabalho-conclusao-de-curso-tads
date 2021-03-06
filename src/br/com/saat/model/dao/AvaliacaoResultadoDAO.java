package br.com.saat.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.saat.model.AtividadeAvaliacao;
import br.com.saat.model.AvaliacaoFisica;
import br.com.saat.model.AvaliacaoResultado;
import br.com.saat.model.CategoriaAtividade;
import br.com.saat.model.ConnectionFactory;

public class AvaliacaoResultadoDAO {
	private Connection con;
	private PreparedStatement stmtScript;
	
	public AvaliacaoResultadoDAO() throws Exception{
        con = ConnectionFactory.getConnection();
    }
	
	public AvaliacaoResultadoDAO(Connection con) throws Exception{
        this.con = con;        
    }

	public List<AvaliacaoResultado> buscarAtividades(int idAtleta) throws SQLException {
		List<AvaliacaoResultado> listaAvaResul = new ArrayList<AvaliacaoResultado>();
		
		stmtScript = con.prepareStatement("SELECT cativ.idCategoriaAtividade, capacidade, teste, idUnidadeDeMedida "
				+ "FROM atleta atle "
				+ "		INNER JOIN categoriaavaliacao caval "
				+ "			ON atle.sexo = caval.sexo "
				+ "				AND TIMESTAMPDIFF(YEAR, atle.dtNascimento, NOW()) >= caval.idadeMinima "
				+ "				AND TIMESTAMPDIFF(YEAR, atle.dtNascimento, NOW()) <= caval.idadeMaxima "
				+ "				AND caval.idTipoCat = 2 "
				+ "		INNER JOIN categoriaatividade cativ "
				+ "			ON caval.idCategoriaAvaliacao = cativ.idCategoriaAvaliacao "
				+ "				AND cativ.flCadastroAtivo = 1 "
				+ "		INNER JOIN atividadeavaliacao ativ "
				+ "			ON cativ.idAtividadeAvaliacao = ativ.idAtividadeAvaliacao "
				+ "WHERE atle.idAtleta = ? "
				+ "ORDER BY capacidade, teste ");
		
		stmtScript.setInt(1, idAtleta);

		ResultSet rs = stmtScript.executeQuery();
		
		while (rs.next()) {
			AtividadeAvaliacao ativAva = new AtividadeAvaliacao();
			ativAva.setCapacidade(rs.getString("capacidade"));
			ativAva.setTeste(rs.getString("teste"));
			ativAva.setIdUnidadeDeMedida(rs.getInt("idUnidadeDeMedida"));
			
			CategoriaAtividade catAtiv = new CategoriaAtividade();
			catAtiv.setIdCategoriaAtividade(rs.getInt("idCategoriaAtividade"));
			catAtiv.setAtividadeAvaliacao(ativAva);
			
			AvaliacaoResultado avaResult = new AvaliacaoResultado();
			avaResult.setCategoriaAtividade(catAtiv);
			
			listaAvaResul.add(avaResult);
		}
		
		return listaAvaResul;
	}

	public boolean inserir(AvaliacaoResultado resultado, AvaliacaoFisica avalFis) throws SQLException {
		boolean retorno = false;
		
		stmtScript = con.prepareStatement("INSERT INTO avaliacaofisicaresultado "
				+ "(idCategoriaAtividade, idAvaliacaoFisica, desempenho) "
				+ "VALUES (?, ?, ?)");
		
		stmtScript.setInt(1, resultado.getCategoriaAtividade().getIdCategoriaAtividade());
		stmtScript.setInt(2, avalFis.getIdAvaliacaoFisica());
		stmtScript.setFloat(3, resultado.getDesempenho());
		
		if (stmtScript.executeUpdate() > 0) {
			retorno = true;
		}
		
		return retorno;
	}

	public List<AvaliacaoResultado> buscarResultDesempenho(int idAvaliacaoFisica) throws SQLException {
		List<AvaliacaoResultado> resultDesempenho = new ArrayList<AvaliacaoResultado>();
		
		stmtScript = con.prepareStatement("SELECT ca.idCategoriaAtividade, capacidade, teste, idUnidadeDeMedida, desempenho, "
				+ "CASE "
				+ "WHEN desempenho <= melhorar THEN 'Melhorar' "
				+ "WHEN desempenho <= media THEN 'Média'  "
				+ "WHEN desempenho <= bom THEN 'Bom'  "
				+ "ELSE 'Excelente'  "
				+ "END as resultado "
				+ "FROM avaliacaofisicaresultado afr "
				+ "INNER JOIN categoriaatividade ca "
				+ "ON ca.idCategoriaAtividade = afr.idCategoriaAtividade "
				+ "INNER JOIN atividadeavaliacao atAv "
				+ "ON ca.idAtividadeAvaliacao = atAv.idAtividadeAvaliacao "
				+ "WHERE idAvaliacaoFisica = ? "
				+ "ORDER BY capacidade, teste ");
		
		stmtScript.setInt(1, idAvaliacaoFisica);
		
		ResultSet rs = stmtScript.executeQuery();
		
		while (rs.next()) {
			AtividadeAvaliacao atividade = new AtividadeAvaliacao();
			atividade.setCapacidade(rs.getString("capacidade"));
			atividade.setTeste(rs.getString("teste"));
			atividade.setIdUnidadeDeMedida(rs.getInt("idUnidadeDeMedida"));
			
			CategoriaAtividade catAtiv = new CategoriaAtividade();
			catAtiv.setIdCategoriaAtividade(rs.getInt("idCategoriaAtividade"));
			catAtiv.setAtividadeAvaliacao(atividade);
			
			AvaliacaoResultado resultado = new AvaliacaoResultado();
			resultado.setCategoriaAtividade(catAtiv);
			resultado.setDesempenho(rs.getFloat("desempenho"));
			resultado.setResultado(rs.getString("resultado"));
			
			resultDesempenho.add(resultado);
		}
		return resultDesempenho;
	}

	public boolean excluir(int idAvaliacaoFisica) throws SQLException {
		boolean retorno = false;
		
		stmtScript = con.prepareStatement("DELETE FROM avaliacaofisicaresultado WHERE idAvaliacaoFisica = ?");
		
		stmtScript.setInt(1, idAvaliacaoFisica);
		
		if (stmtScript.executeUpdate() >= 0) {
			retorno = true;
		}
		
		return retorno;
	}

	public boolean editar(AvaliacaoResultado resultado, AvaliacaoFisica avalFis) throws SQLException {
		boolean retorno = false;
		
		stmtScript = con.prepareStatement("UPDATE  avaliacaofisicaresultado "
				+ "SET desempenho = ? "
				+ "WHERE idCategoriaAtividade = ? AND idAvaliacaoFisica = ? ");
		
		stmtScript.setFloat(1, resultado.getDesempenho());
		stmtScript.setInt(2, resultado.getCategoriaAtividade().getIdCategoriaAtividade());
		stmtScript.setInt(3, avalFis.getIdAvaliacaoFisica());
		
		if (stmtScript.executeUpdate() > 0) {
			retorno = true;
		}
		
		return retorno;
	}
}
