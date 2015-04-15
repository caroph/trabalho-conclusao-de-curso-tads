package br.com.saat.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.com.saat.core.Constants;
import br.com.saat.model.Atleta;
import br.com.saat.model.Perfis;
import br.com.saat.model.Usuario;
import br.com.saat.model.negocio.AtletaNegocio;

@WebServlet("/NutricionistaController")
public class NutricionistaController extends Controller {
	private static final long serialVersionUID = 1L;
       
    public NutricionistaController() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		RequestDispatcher rd;
		
		//Verifica autenticação usuário
		Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		if(usuarioLogado == null || usuarioLogado.getPerfil() != Perfis.Nutricionista.getValor()){
			super.doPost(request, response, usuarioLogado, false, false);
			return;
		}
		
		String retorno = null;
		String action = request.getParameter("action");
		
		if("jspBuscarAtletas".equals(action)){
			//Carregar página Buscar Atleta
			AtletaNegocio negocio = new AtletaNegocio();
			List<Atleta> lista = new ArrayList<Atleta>();
			try{
				lista = negocio.buscarAtletas(1);
			}catch(Exception ex){
				request.setAttribute("msg", ex.getMessage());
			}
			
			request.setAttribute("listaAtletas", lista);
			retorno = String.format("%s/NutricionistaBuscaAtleta.jsp", Constants.VIEW);
		}else if("RegistrarPresenca".equals(action)){
			String msg = "";
			
			int idAtleta = Integer.parseInt(request.getParameter("idAtleta"));
			String data = request.getParameter("dataPresenca");
			String hora = request.getParameter("hrPresenca");
			Date dt = new Date();
			DateFormat formatter = new SimpleDateFormat("HH:mm");  
        	Date hr = new Date();
			
			if(!"".equals(data))
				dt = new Date(data);
			
			if(!"".equals(hora)){
				try {
					hr = (Date)formatter.parse(hora);
				} catch (ParseException e) {
					msg = "Erro ao formatar o horário de treino";
				}
			}
			
			//Deixei para depois fazer a consulta de semana, comparação com dia de treino e verificação da Chamada existente.
			
		}else{
			retorno = String.format("%s/NutricionistaPrincipal.jsp", Constants.VIEW);
		}
		
		if(retorno != null){
			rd = getServletContext().getRequestDispatcher(retorno);
			rd.forward(request, response);
		}
	}

}
