package br.com.saat.controller;

import java.io.IOException;
import java.text.DateFormat;
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
import br.com.saat.enumeradores.Perfis;
import br.com.saat.model.Atleta;
import br.com.saat.model.Prontuario;
import br.com.saat.model.Usuario;
import br.com.saat.model.negocio.AtletaNegocio;
import br.com.saat.model.negocio.ProntuarioNegocio;

/**
 * Servlet implementation class SaudeGeralController
 */
@WebServlet("/SaudeGeralController")
public class SaudeGeralController extends Controller {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaudeGeralController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		RequestDispatcher rd;
		
		//Verifica autenticação usuário
		Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		if(usuarioLogado == null || (usuarioLogado.getPerfil() != Perfis.Fisioterapeuta.getValor() && usuarioLogado.getPerfil() != Perfis.Psicologa.getValor())){
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
			retorno = String.format("%s/SaudeGeralBuscaAtleta.jsp", Constants.VIEW);
			
		}else if ("novoAtendimento".equals(action)){
			//inserir Novo Atendimento
			boolean exception = false;
			String msgSucesso = "";
			String msg = "";
			
			Atleta atleta = new Atleta();
			Usuario usuario = new Usuario();
			
			String dt = request.getParameter("dtAtendimento");			
			String hr = request.getParameter("hrAtendimento");
			Date dtAtendimento = null; 
            Date hrAtendimento = null;
            
            try{
            	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
            	dtAtendimento = (Date)formatter.parse(dt);
            	
            	formatter = new SimpleDateFormat("HH:mm");  
            	hrAtendimento = (Date)formatter.parse(hr);
            	
            	atleta.setIdPessoa(Integer.parseInt(request.getParameter("idAtletaAtend")));
    			usuario.setIdPessoa(usuarioLogado.getIdPessoa());
            }catch(Exception ex){
            	msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
            	exception = true;
            }
            
            if(!exception){
            	Prontuario prontuario = new Prontuario();
    			ProntuarioNegocio negocio = new ProntuarioNegocio();
    			
    			prontuario.setDtaAtendimento(dtAtendimento);
    			prontuario.setHrAtendimento(hrAtendimento);
    			prontuario.setAnotacao(request.getParameter("anotacao"));
    			prontuario.setAtleta(atleta);
    			prontuario.setUsuario(usuario);
    			
    			//Valida dados prontuário
				List<Object> listaValidacao = negocio.validaDados(prontuario);
				boolean valida = (boolean) listaValidacao.get(0);
				
				try {
					if(valida){
						//Inserindo prontuário
						if(negocio.inserir(prontuario)){
							msgSucesso = "Atendimento cadastrado com sucesso!";
						}
					}else{
						msg = (String) listaValidacao.get(1);
					}
				} catch (Exception ex) {
					msg = ex.getMessage(); 
				}
				
				
				AtletaNegocio atletaNegocio = new AtletaNegocio();
				List<Atleta> lista = new ArrayList<Atleta>();				
				try{
					lista = atletaNegocio.buscarAtletas(1);
				}catch(Exception ex){
					request.setAttribute("msg", ex.getMessage());
				}
				
				if("".equals(msgSucesso)){
					request.setAttribute("msg", msg);
				}else{
					request.setAttribute("msgSucesso", msgSucesso);
				}
				
				request.setAttribute("listaAtletas", lista);
				retorno = String.format("%s/SaudeGeralBuscaAtleta.jsp", Constants.VIEW);
            }
		}else{
		retorno = String.format("%s/SaudeGeralPrincipal.jsp", Constants.VIEW);
		}
		
		if(retorno != null){
			rd = getServletContext().getRequestDispatcher(retorno);
			rd.forward(request, response);
		}
	}
}