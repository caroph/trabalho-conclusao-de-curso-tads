package br.com.saat.model.negocio;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import br.com.saat.core.JavaMailApp;
import br.com.saat.model.Atleta;
import br.com.saat.model.Dieta;
import br.com.saat.model.dao.DietaDAO;

public class DietaNegocio {

	public List<Object> validaDados(Dieta dieta) {
		List<Object> lista = new ArrayList<Object>();

		if (dieta.isCompeticao()) {
			if (dieta.getDtValidadeInicio() == null || dieta.getDtValidadeFim() == null) {
				lista.add(false);
				lista.add("Informe corretamente a data de validade para o per�odo de competi��o !");
			} else if (dieta.getDtValidadeInicio().after(dieta.getDtValidadeFim())) {
				lista.add(false);
				lista.add("Data final do per�odo de validade deve ser maior ou igual a data inicial!");
			} else {
				lista.add(true);
			}
		} else {
			lista.add(true);
		}

		return lista;
	}

	public boolean inserir(Dieta dieta) throws Exception {
		boolean retorno = false;

		try {
			DietaDAO dao = new DietaDAO();
			if (dao.inserir(dieta)) {
				retorno = true;
			}
		} catch (Exception e) {
			throw new Exception(
					"Erro! Ocorreu algum erro ao inserir o atendimento.");
		}

		return retorno;
	}

	public List<Dieta> buscaDietas(Atleta atleta) throws Exception{
		try {
			DietaDAO dao = new DietaDAO();
			List<Dieta> lista = dao.buscaDietas(atleta);
			return lista;
		} catch (Exception e) {
			throw new Exception("Erro! Ocorreu algum erro ao buscar a(s) dieta(s) do atleta.");
		}
	}
	
	public Dieta buscaDieta(Dieta dieta) throws Exception{
		try {
			DietaDAO dao = new DietaDAO();
			dieta = dao.buscaDieta(dieta);
			return dieta;
		} catch (Exception e) {
			throw new Exception("Erro! Ocorreu algum erro ao buscar dieta a ser editada.");
		}
	}

	public boolean excluir(Dieta dieta) throws Exception{
		boolean retorno = false;

		try {
			DietaDAO dao = new DietaDAO();
			if (dao.excluir(dieta)) {
				retorno = true;
			}
		} catch (Exception e) {
			throw new Exception("Erro! Ocorreu algum erro ao excluir a dieta.");
		}

		return retorno;
	}

	public boolean alterar(Dieta dieta) throws Exception {
		boolean retorno = false;

		try {
			DietaDAO dao = new DietaDAO();
			if (dao.alterar(dieta)) {
				retorno = true;
			}
		} catch (Exception e) {
			throw new Exception("Erro! Ocorreu algum erro ao editar a dieta.");
		}

		return retorno;
	}

	public boolean enviar(Atleta atleta, String path) throws Exception {
		boolean retorno = false;
		ResponsavelNegocio negocio = new ResponsavelNegocio();
		JavaMailApp email = new JavaMailApp();
		try {
			List<String> listaDestinatarios = negocio.buscaEmailResp(atleta);
			
			for (String destino : listaDestinatarios) {
				email.enviarDieta(atleta, destino, path);
			}
			
			retorno = true;
		} catch (AddressException e) {
			throw new Exception("Erro ao identificar email de respons�vel!");
		} catch (MessagingException e) {
			throw new Exception("Erro ao enviar email ao respons�vel!");
		}
		return retorno;
	}



}
