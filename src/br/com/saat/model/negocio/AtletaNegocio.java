package br.com.saat.model.negocio;

import java.util.ArrayList;
import java.util.List;

import br.com.saat.model.Atleta;
import br.com.saat.model.dao.AtletaDAO;

public class AtletaNegocio {

	public AtletaNegocio(){}

	public List<Object> validaDados(Atleta atleta) {
		List<Object> lista = new ArrayList<Object>();
		
		if ("".equals(atleta.getNrMatricula()) || atleta.getNrMatricula() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Nº Matrícula Clube Curitibano' !");
		} else if ("".equals(atleta.getNome()) || atleta.getNome() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Nome' !");
		} else if ("".equals(atleta.getDtNascimento()) || atleta.getDtNascimento() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Data de Nascimento' !");
		} else if ("".equals(atleta.getRG()) || atleta.getRG()== null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'RG' !");
		} else if ("".equals(atleta.getCPF()) || atleta.getCPF() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'CPF' !");
		} else if ("".equals(atleta.getNmContatoEmergencia()) || atleta.getNmContatoEmergencia() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Nome Contato Emergência' !");
		} else if ("".equals(atleta.getTelContatoEmergencia()) || atleta.getTelContatoEmergencia() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Telefone Contato de Emergência' !");
		} else if ("".equals(atleta.getGrauParentescoContatoEmergencia()) || atleta.getGrauParentescoContatoEmergencia() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Grau de Parentesco [Contato de Emergência]' !");
		} else if ("".equals(atleta.getDtValidade()) || atleta.getDtValidade() == null) {
			lista.add(false);
			lista.add("Informe corretamente o campo 'Data de Validade' !");
		}else {
			lista.add(true);
		}
		
		return lista;
	}

	public int inserir(Atleta atleta) throws Exception {
		try {
			AtletaDAO dao = new AtletaDAO();
			int idAtleta = dao.inserir(atleta);
			return idAtleta;
		} catch (Exception e) {
			throw new Exception("Erro! Ocorreu algum erro ao inserir o atleta.");
		}
	}

	public boolean alterar(Atleta atleta) {
		// TODO Auto-generated method stub
		return false;
	}
	
}