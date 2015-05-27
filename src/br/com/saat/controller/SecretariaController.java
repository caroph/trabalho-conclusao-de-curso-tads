package br.com.saat.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import br.com.saat.core.Constants;
import br.com.saat.core.JavaMailApp;
import br.com.saat.enumeradores.DiasSemana;
import br.com.saat.enumeradores.Equipes;
import br.com.saat.enumeradores.GrauParentesco;
import br.com.saat.enumeradores.Perfis;
import br.com.saat.enumeradores.Sexo;
import br.com.saat.enumeradores.TpDocumento;
import br.com.saat.enumeradores.TpEndereco;
import br.com.saat.enumeradores.TpPessoa;
import br.com.saat.enumeradores.Turno;
import br.com.saat.model.Atleta;
import br.com.saat.model.DiaTreino;
import br.com.saat.model.Documento;
import br.com.saat.model.Endereco;
import br.com.saat.model.Responsavel;
import br.com.saat.model.Torneio;
import br.com.saat.model.Usuario;
import br.com.saat.model.negocio.AtletaNegocio;
import br.com.saat.model.negocio.DiaTreinoNegocio;
import br.com.saat.model.negocio.DiasSemanaNegocio;
import br.com.saat.model.negocio.DocumentoNegocio;
import br.com.saat.model.negocio.EnderecoNegocio;
import br.com.saat.model.negocio.EquipesNegocio;
import br.com.saat.model.negocio.GrauParentescoNegocio;
import br.com.saat.model.negocio.PerfisNegocio;
import br.com.saat.model.negocio.ResponsavelNegocio;
import br.com.saat.model.negocio.SexoNegocio;
import br.com.saat.model.negocio.TorneioNegocio;
import br.com.saat.model.negocio.TurnoNegocio;
import br.com.saat.model.negocio.UsuarioNegocio;

import com.google.gson.Gson;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

@WebServlet("/SecretariaController")
public class SecretariaController extends Controller {
	private static final long serialVersionUID = 1L;

	public SecretariaController() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			ParseException {
		HttpSession session = request.getSession();
		RequestDispatcher rd;

		// Verifica autentica��o usu�rio
		Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
		if (usuarioLogado == null
				|| usuarioLogado.getPerfil() != Perfis.Secretaria.getValor()) {
			super.doPost(request, response, usuarioLogado, false, false);
			return;
		}

		String servletRetorno = "/SecretariaController?action=jspPaginaInicialSecretaria";
		String retorno = null;// String.format("%s/SecretariaPrincipal.jsp",
								// Constants.VIEW);
		String action = request.getParameter("action");

		if ("jspPaginaInicialSecretaria".equals(action)) {
			DocumentoNegocio documentoNegocio = new DocumentoNegocio();
			int nrPendencias = 0;
			String msg = "";

			ArrayList<HashMap<Integer, String>> listaPendencias = null;
			try {
				listaPendencias = documentoNegocio.buscarPendencias();

				for (HashMap<Integer, String> pendencia : listaPendencias) {
					nrPendencias += pendencia.size();
				}

			} catch (Exception ex) {
				msg = ex.getMessage();
			}
			if (listaPendencias == null) {
				request.setAttribute("msgAlerta",
						"Nenhum atleta possui pend�ncia!");
			}

			request.setAttribute("listaPendencias", listaPendencias);
			request.setAttribute("nrPendencias", nrPendencias);
			request.setAttribute("msgErro", msg);
			retorno = String.format("%s/SecretariaPrincipal.jsp",
					Constants.VIEW);

		} else if ("jspNovoAtleta".equals(action)) {
			// Carregar p�gina Novo Atleta
			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			GrauParentescoNegocio negocioGrau = new GrauParentescoNegocio();
			List<GrauParentesco> listaGraus = negocioGrau.listaGraus();

			TurnoNegocio turnoNegocio = new TurnoNegocio();
			List<Turno> listaTurnos = turnoNegocio.listaTurnos();
			
			SexoNegocio sexoNegocio = new SexoNegocio();
			List<Sexo> listaSexo = sexoNegocio.listaSexo();

			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaGrauParentesco", listaGraus);
			request.setAttribute("listaTurnos", listaTurnos);
			request.setAttribute("listaSexo", listaSexo);

			retorno = String.format("%s/SecretariaNovoAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoAtleta";

		} else if ("carregaDiasTreino".equals(action)) {
			String msg = "";
			boolean exception = false;
			String tpEquipe = request.getParameter("tpEquipe");

			if (!"".equals(tpEquipe)) {
				int idTipoEquipe = Integer.parseInt(tpEquipe);

				Atleta atleta = new Atleta();
				atleta.setIdTpEquipe(idTipoEquipe);

				DiaTreinoNegocio negocio = new DiaTreinoNegocio();
				List<DiaTreino> lista = new ArrayList<DiaTreino>();

				if (!"".equals(request.getParameter("idAtleta"))
						&& !"0".equals(request.getParameter("idAtleta"))) {

					String nascimento = request.getParameter("dtNascimento");
					String validade = request.getParameter("dtValidade");
					Date dtNascimento = null;
					Date dtValidade = null;
					int numero = 0;
					int idTurno = 0;
					int idGrauParentesco = 0;
					int sexo = 0;
					String escolha;
					int idEndereco;
					int idAtleta = 0;

					try {
						DateFormat formatter = new SimpleDateFormat(
								"dd/MM/yyyy");
						dtNascimento = (Date) formatter.parse(nascimento);
						dtValidade = (Date) formatter.parse(validade);
					} catch (Exception ex) {
						msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
						exception = true;
					}
					try {
						numero = Integer.parseInt(request
								.getParameter("numero"));
					} catch (Exception ex) {
						msg = "Favor informar corretamente o campo 'N�mero' do endere�o";
						exception = true;
					}
					try {
						idTurno = Integer.parseInt(request
								.getParameter("turno"));
					} catch (Exception ex) {
						msg = "Favor selecionar corretamente o campo 'Turno'.";
						exception = true;
					}
					try {
						idGrauParentesco = Integer.parseInt(request
								.getParameter("grauParentesco"));
					} catch (Exception ex) {
						msg = "Favor selecionar corretamente o campo 'Grau de Parentesco'.";
						exception = true;
					}
					try {
						sexo = Integer.parseInt(request
								.getParameter("sexo"));
					} catch (Exception ex) {
						msg = "Favor selecionar corretamente o campo 'Sexo'.";
						exception = true;
					}
					if (!exception) {
						Endereco endereco = new Endereco();
						AtletaNegocio atletaNegocio = new AtletaNegocio();

						// Dados do Atleta
						idAtleta = Integer.parseInt(request
								.getParameter("idAtleta"));
						atleta.setIdPessoa(idAtleta);
						atleta.setNome(request.getParameter("nome"));
						atleta.setEmail(request.getParameter("email"));
						atleta.setSexo(sexo);
						atleta.setCelular(request.getParameter("celular"));
						atleta.setNrMatricula(request
								.getParameter("nrMatricula"));
						atleta.setNrCadCBT(request.getParameter("nrCadCBT"));
						atleta.setNrCadFPT(request.getParameter("nrCadFPT"));
						atleta.setDtNascimento(dtNascimento);
						atleta.setRG(request.getParameter("rg"));
						atleta.setCPF(request.getParameter("cpf"));
						atleta.setEscola(request.getParameter("escola"));
						atleta.setSerie(request.getParameter("serie"));
						atleta.setIdTurno(idTurno);
						escolha = request.getParameter("acompPsicologico");
						atleta.setAcompPsicologico("sim".equals(escolha) ? true
								: false);
						atleta.setNmMedicoResponsavel(request
								.getParameter("nmMedicoResponsavel"));
						atleta.setTelMedicoResponsal(request
								.getParameter("telMedicoResponsavel"));
						atleta.setConvenio(request.getParameter("convenio"));
						atleta.setMedicacaoAutorizada(request
								.getParameter("medicacaoAutorizada"));
						escolha = request.getParameter("flAlergias");
						atleta.setFlAlergias("sim".equals(escolha) ? true
								: false);
						atleta.setDsAlergias(request.getParameter("dsAlergias"));
						escolha = request.getParameter("flMedicacao");
						atleta.setFlMedicacao("sim".equals(escolha) ? true
								: false);
						atleta.setDsMedicacao(request
								.getParameter("dsMedicacao"));
						atleta.setNmContatoEmergencia(request
								.getParameter("nmContatoEmergencia"));
						atleta.setTelContatoEmergencia(request
								.getParameter("telContatoEmergencia"));
						atleta.setIdGrauParentesco(idGrauParentesco);
						atleta.setDtValidade(dtValidade);
						atleta.setEndereco(endereco);

						idEndereco = Integer.parseInt(request
								.getParameter("idEndereco"));
						endereco.setIdEndereco(idEndereco);
						endereco.setEndereco(request.getParameter("endereco"));
						endereco.setNumero(numero);
						endereco.setComplemento(request
								.getParameter("complemento"));
						endereco.setBairro(request.getParameter("bairro"));
						endereco.setEstado(request.getParameter("estado"));
						endereco.setCidade(request.getParameter("cidade"));
						endereco.setTelefone(request.getParameter("telefone"));
						endereco.setTpEndereco(TpEndereco.Residencial
								.getValor());

						try {
							lista = negocio.carregaDiasTreino(idTipoEquipe);
							List<Integer> listaDias = atletaNegocio
									.buscaDiasTreinoAtleta(idAtleta);
							for (DiaTreino dia : lista) {
								if (listaDias != null
										&& listaDias.contains(dia
												.getIdDiaTreino())) {
									dia.setSelecionado(true);
								}
							}
						} catch (Exception ex) {
							msg = ex.getMessage();
						}
					}
				} else {
					try {
						lista = negocio.carregaDiasTreino(idTipoEquipe);
					} catch (Exception ex) {
						msg = ex.getMessage();
					}
				}
				request.setAttribute("atleta", atleta);
				request.setAttribute("listaDiasTreinos", lista);
			}

			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			GrauParentescoNegocio negocioGrau = new GrauParentescoNegocio();
			List<GrauParentesco> listaGraus = negocioGrau.listaGraus();

			TurnoNegocio turnoNegocio = new TurnoNegocio();
			List<Turno> listaTurnos = turnoNegocio.listaTurnos();
			
			SexoNegocio sexoNegocio = new SexoNegocio();
			List<Sexo> listaSexo = sexoNegocio.listaSexo();

			request.setAttribute("msgErro", msg);
			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaGrauParentesco", listaGraus);
			request.setAttribute("listaTurnos", listaTurnos);
			request.setAttribute("listaSexo", listaSexo);

			retorno = String.format("%s/SecretariaNovoAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoAtleta";
			
		} else if ("inserirAtleta".equals(action)) {
			boolean exception = false;
			String escolha;
			String msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
			String msgSucesso = "";
			String idAtleta;
			String idEndereco;
			int numero = 0;
			int idTpEquipe = 0;
			int idGrauParentesco = 0;
			int idTurno = 0;
			int sexo = 0;
			String[] diasTreino = null;

			Atleta atleta = new Atleta();

			String nascimento = request.getParameter("dtNascimento");
			String validade = request.getParameter("dtValidade");
			Date dtNascimento = null;
			Date dtValidade = null;

			try {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				dtNascimento = (Date) formatter.parse(nascimento);
				dtValidade = (Date) formatter.parse(validade);
			} catch (Exception ex) {
				msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				exception = true;
			}
			try {
				idTpEquipe = Integer.parseInt(request.getParameter("tpEquipe"));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o campo 'Equipe'.";
				exception = true;
			}
			try {
				numero = Integer.parseInt(request.getParameter("numero"));
			} catch (Exception ex) {
				msg = "Favor informar corretamente o campo 'N�mero' do endere�o";
				exception = true;
			}
			try {
				idTurno = Integer.parseInt(request.getParameter("turno"));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o campo 'Turno'.";
				exception = true;
			}
			try {
				idGrauParentesco = Integer.parseInt(request
						.getParameter("grauParentesco"));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o campo 'Grau de Parentesco'.";
				exception = true;
			}
			try {
				sexo = Integer.parseInt(request
						.getParameter("sexo"));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o campo 'Sexo'.";
				exception = true;
			}
			if (!exception) {
				Endereco endereco = new Endereco();
				AtletaNegocio negocio = new AtletaNegocio();
				EnderecoNegocio endNegocio = new EnderecoNegocio();
				DiaTreinoNegocio diaNegocio = new DiaTreinoNegocio();

				// Dados do Atleta
				idAtleta = request.getParameter("idAtleta");
				atleta.setNome(request.getParameter("nome"));
				atleta.setEmail(request.getParameter("email"));
				atleta.setSexo(sexo);
				atleta.setCelular(request.getParameter("celular"));
				atleta.setIdTpEquipe(idTpEquipe);
				atleta.setNrMatricula(request.getParameter("nrMatricula"));
				atleta.setNrCadCBT(request.getParameter("nrCadCBT"));
				atleta.setNrCadFPT(request.getParameter("nrCadFPT"));
				atleta.setDtNascimento(dtNascimento);
				atleta.setRG(request.getParameter("rg"));
				atleta.setCPF(request.getParameter("cpf"));
				atleta.setEscola(request.getParameter("escola"));
				atleta.setSerie(request.getParameter("serie"));
				atleta.setIdTurno(idTurno);
				escolha = request.getParameter("acompPsicologico");
				atleta.setAcompPsicologico("sim".equals(escolha) ? true : false);
				atleta.setNmMedicoResponsavel(request
						.getParameter("nmMedicoResponsavel"));
				atleta.setTelMedicoResponsal(request
						.getParameter("telMedicoResponsavel"));
				atleta.setConvenio(request.getParameter("convenio"));
				atleta.setMedicacaoAutorizada(request
						.getParameter("medicacaoAutorizada"));
				escolha = request.getParameter("flAlergias");
				atleta.setFlAlergias("sim".equals(escolha) ? true : false);
				atleta.setDsAlergias(request.getParameter("dsAlergias"));
				escolha = request.getParameter("flMedicacao");
				atleta.setFlMedicacao("sim".equals(escolha) ? true : false);
				atleta.setDsMedicacao(request.getParameter("dsMedicacao"));
				atleta.setNmContatoEmergencia(request
						.getParameter("nmContatoEmergencia"));
				atleta.setTelContatoEmergencia(request
						.getParameter("telContatoEmergencia"));
				atleta.setIdGrauParentesco(idGrauParentesco);
				atleta.setDtValidade(dtValidade);
				atleta.setEndereco(endereco);

				idEndereco = request.getParameter("idEndereco");
				endereco.setEndereco(request.getParameter("endereco"));
				endereco.setNumero(numero);
				endereco.setComplemento(request.getParameter("complemento"));
				endereco.setBairro(request.getParameter("bairro"));
				endereco.setEstado(request.getParameter("estado"));
				endereco.setCidade(request.getParameter("cidade"));
				endereco.setTelefone(request.getParameter("telefone"));
				endereco.setTpEndereco(TpEndereco.Residencial.getValor());

				diasTreino = request.getParameterValues("diasTreino");

				try {
					if (!"".equals(idAtleta) && !"0".equals(idAtleta)
							&& !"".equals(idEndereco)
							&& !"0".equals(idEndereco)) {
						atleta.setIdPessoa(Integer.parseInt(idAtleta));
						endereco.setIdEndereco(Integer.parseInt(idEndereco));
					}
					// Valida dados atleta
					List<Object> listaValidacao = negocio.validaDados(atleta);
					boolean valida = (boolean) listaValidacao.get(0);

					if (valida) {
						// Valida dados endere�o
						listaValidacao = endNegocio.validar(endereco);
						valida = (boolean) listaValidacao.get(0);

						if (valida) {
							// Valida sele��o de dias de treino
							if (!"".equals(diasTreino) && diasTreino != null) {
								if (atleta.getIdPessoa() == 0) {
									// Inserindo Atleta
									int idNovoAtleta = negocio.inserir(atleta);
									if (idNovoAtleta > 0) {
										// Inserindo endere�o
										if (endNegocio.inserir(endereco,
												idNovoAtleta,
												TpPessoa.Atleta.getValor())) {
											// Inserindo dias de treino
											if (diaNegocio
													.inserirDiaTreinoAtleta(
															diasTreino,
															idNovoAtleta)) {
												msgSucesso = "Atleta cadastrado com sucesso!";
											}
										}
									}
								} else {
									if (negocio.alterar(atleta)
											&& endNegocio.alterar(atleta)
											&& diaNegocio.alterar(diasTreino,
													atleta.getIdPessoa())) {
										msgSucesso = "Atleta alterado com sucesso!";
									}
								}
							} else {
								msg = "Favor selecionar ao menos um dia de treino!";
							}
						} else {
							msg = (String) listaValidacao.get(1);
						}
					} else {
						msg = (String) listaValidacao.get(1);
					}
				} catch (Exception ex) {
					msg = ex.getMessage();
				}
			}

			if ("".equals(msgSucesso)) {
				request.setAttribute("msgErro", msg);
				request.setAttribute("atleta", atleta);
					
				DiaTreinoNegocio negocio = new DiaTreinoNegocio();
				List<DiaTreino> lista = new ArrayList<DiaTreino>();
				if(atleta.getIdPessoa() != 0){
					try {						
						lista = negocio.carregaDiasTreino(atleta.getIdTpEquipe());
						
						AtletaNegocio atletaNegocio = new AtletaNegocio();
						List<Integer> listaDias = atletaNegocio
								.buscaDiasTreinoAtleta(atleta.getIdPessoa());
						for (DiaTreino dia : lista) {
							if (listaDias != null
									&& listaDias.contains(dia.getIdDiaTreino())) {
								dia.setSelecionado(true);
							}
						}
					} catch (Exception ex) {
						msg = ex.getMessage();
					}					
				}else{
					try {						
						lista = negocio.carregaDiasTreino(atleta.getIdTpEquipe());

						for (DiaTreino dia : lista) {
							for (String a : diasTreino) {
								if(a.equals(String.valueOf(dia.getIdDiaTreino())))
									dia.setSelecionado(true);
							}
						}
						request.setAttribute("listaDiasTreinos", lista);
					} catch (Exception ex) {
						msg = ex.getMessage();
					}	
				}
				request.setAttribute("listaDiasTreinos", lista);
			} else {
				request.setAttribute("msgSucesso", msgSucesso);
			}

			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			GrauParentescoNegocio negocioGrau = new GrauParentescoNegocio();
			List<GrauParentesco> listaGraus = negocioGrau.listaGraus();

			TurnoNegocio turnoNegocio = new TurnoNegocio();
			List<Turno> listaTurnos = turnoNegocio.listaTurnos();
			
			SexoNegocio sexoNegocio = new SexoNegocio();
			List<Sexo> listaSexo = sexoNegocio.listaSexo();

			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaGrauParentesco", listaGraus);
			request.setAttribute("listaTurnos", listaTurnos);
			request.setAttribute("listaSexo", listaSexo);
			
			retorno = String.format("%s/SecretariaNovoAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoAtleta";

		} else if ("buscarResponsaveisVinculacao".equals(action)) {
			ResponsavelNegocio responsavelNegocio = new ResponsavelNegocio();
			List<Responsavel> listaResponsaveis = null;
			String msg = "";

			String pagina = request.getParameter("pagina");

			String atleta = request.getParameter("idAtleta");
			int idAtleta = Integer.parseInt(atleta);

			try {
				listaResponsaveis = responsavelNegocio
						.buscarRespNaoVinculado(idAtleta);
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			Map<Integer, String> listaParentesco = new GrauParentescoNegocio()
					.listaGrausObject();

			Map<String, Object> lista = new LinkedHashMap<String, Object>();
			lista.put("idAtleta", atleta);
			lista.put("pagina", pagina);
			lista.put("listaResponsaveis", listaResponsaveis);
			lista.put("grauParentesco", listaParentesco);

			String json = new Gson().toJson(lista);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			request.setAttribute("msgErro", msg);

		} else if ("vincularResponsavel".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			int idResponsavel = 0;
			int idGrauParentesco = 0;

			boolean exception = false;

			String pagina = request.getParameter("pagina");

			try {
				idResponsavel = Integer.parseInt(request
						.getParameter("responsavel"));
			} catch (Exception ex) {
				msg = "Favor selecionar um respons�vel";
				exception = true;
			}
			try {
				idGrauParentesco = Integer.parseInt(request
						.getParameter("grauParentesco"));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o campo 'Grau de Parentesco'.";
				exception = true;
			}

			int idAtleta = Integer.parseInt(request.getParameter("idAtleta"));
			Atleta atleta = new Atleta();

			AtletaNegocio negocio = new AtletaNegocio();

			try {
				if (!exception) {
					if (negocio.vincularResponsavel(idAtleta, idResponsavel,
							idGrauParentesco)) {
						msgSucesso = "Respons�vel vinculado com sucesso!";
					}
				}
				atleta = negocio.buscarAtleta(idAtleta);
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			if (!"1".equals(pagina)) {
				EquipesNegocio negocioEquipe = new EquipesNegocio();
				List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

				GrauParentescoNegocio negocioGrau = new GrauParentescoNegocio();
				List<GrauParentesco> listaGraus = negocioGrau.listaGraus();

				TurnoNegocio turnoNegocio = new TurnoNegocio();
				List<Turno> listaTurnos = turnoNegocio.listaTurnos();

				DiaTreinoNegocio negocioDiaTreino = new DiaTreinoNegocio();
				List<DiaTreino> listaDiaTreino = new ArrayList<DiaTreino>();

				try {
					listaDiaTreino = negocioDiaTreino.carregaDiasTreino(atleta
							.getIdTpEquipe());
					List<Integer> listaDias = negocio
							.buscaDiasTreinoAtleta(atleta.getIdPessoa());

					for (DiaTreino dia : listaDiaTreino) {
						if (listaDias != null
								&& listaDias.contains(dia.getIdDiaTreino())) {
							dia.setSelecionado(true);
						}
					}

					request.setAttribute("listaDiasTreinos", listaDiaTreino);
				} catch (Exception ex) {
					msg = ex.getMessage();
				}
				
				SexoNegocio sexoNegocio = new SexoNegocio();
				List<Sexo> listaSexo = sexoNegocio.listaSexo();

				request.setAttribute("listaEquipes", listaEquipes);
				request.setAttribute("listaGrauParentesco", listaGraus);
				request.setAttribute("listaTurnos", listaTurnos);
				request.setAttribute("listaSexo", listaSexo);
				request.setAttribute("atleta", atleta);

				retorno = String.format("%s/SecretariaNovoAtleta.jsp",
						Constants.VIEW);
				servletRetorno = "/SecretariaController?action=jspNovoAtleta";
			} else {
				List<Atleta> lista = new ArrayList<Atleta>();
				try {
					lista = negocio.buscarAtletas(2);
				} catch (Exception ex) {
					msg = ex.getMessage();
				}

				request.setAttribute("listaAtletas", lista);
				retorno = String.format("%s/SecretariaBuscaAtleta.jsp",
						Constants.VIEW);
				servletRetorno = "/SecretariaController?action=jspBuscaAtleta";
			}

			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);

		} else if ("jspBuscaAtleta".equals(action)) {
			// Carregar p�gina Buscar Atleta
			AtletaNegocio negocio = new AtletaNegocio();
			List<Atleta> lista = new ArrayList<Atleta>();
			try {
				lista = negocio.buscarAtletas(2);
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			request.setAttribute("listaAtletas", lista);
			retorno = String.format("%s/SecretariaBuscaAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaAtleta";

		} else if ("editarAtleta".equals(action)) {
			int idAtleta = Integer.parseInt(request.getParameter("idAtleta"));
			Atleta atleta = new Atleta();

			AtletaNegocio negocio = new AtletaNegocio();

			try {
				atleta = negocio.buscarAtleta(idAtleta);
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			GrauParentescoNegocio negocioGrau = new GrauParentescoNegocio();
			List<GrauParentesco> listaGraus = negocioGrau.listaGraus();

			TurnoNegocio turnoNegocio = new TurnoNegocio();
			List<Turno> listaTurnos = turnoNegocio.listaTurnos();

			DiaTreinoNegocio negocioDiaTreino = new DiaTreinoNegocio();
			List<DiaTreino> listaDiaTreino = new ArrayList<DiaTreino>();

			try {
				listaDiaTreino = negocioDiaTreino.carregaDiasTreino(atleta
						.getIdTpEquipe());
				List<Integer> listaDias = negocio.buscaDiasTreinoAtleta(atleta
						.getIdPessoa());

				for (DiaTreino dia : listaDiaTreino) {
					if (listaDias != null
							&& listaDias.contains(dia.getIdDiaTreino())) {
						dia.setSelecionado(true);
					}
				}

				request.setAttribute("listaDiasTreinos", listaDiaTreino);
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}
			
			SexoNegocio sexoNegocio = new SexoNegocio();
			List<Sexo> listaSexo = sexoNegocio.listaSexo();

			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaGrauParentesco", listaGraus);
			request.setAttribute("listaTurnos", listaTurnos);
			request.setAttribute("listaSexo", listaSexo);
			request.setAttribute("atleta", atleta);
			
			retorno = String.format("%s/SecretariaNovoAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=editarAtleta&idAtleta="
					+ request.getParameter("idAtleta");

		} else if ("desativarAtleta".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			Atleta atleta = new Atleta();
			atleta.setIdPessoa(Integer.parseInt(request
					.getParameter("idAtleta")));

			AtletaNegocio negocio = new AtletaNegocio();
			List<Atleta> lista = new ArrayList<Atleta>();
			
			try {
				if (negocio.desativar(atleta)) {
					msgSucesso = "Atleta desativado com sucesso!";
				} else {
					msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				}
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			try {
				lista = negocio.buscarAtletas(2);
			} catch (Exception ex) {
				msg = ex.getMessage();
			}
			
			request.setAttribute("listaAtletas", lista);
			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			retorno = String.format("%s/SecretariaBuscaAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaAtleta";

		} else if ("ativarAtleta".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			Atleta atleta = new Atleta();
			atleta.setIdPessoa(Integer.parseInt(request
					.getParameter("idAtleta")));

			AtletaNegocio negocio = new AtletaNegocio();
			List<Atleta> lista = new ArrayList<Atleta>();

			try {
				if (negocio.ativar(atleta)) {
					msgSucesso = "Atleta ativado com sucesso!";
					if(negocio.inserirPendenciaAtleta(atleta.getIdPessoa())){
						msgSucesso = "Atleta ativado e pend�ncias inseridas com sucesso!";
					}else{
						msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
					}
				} else {
					msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				}
				lista = negocio.buscarAtletas(2);
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("listaAtletas", lista);
			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			retorno = String.format("%s/SecretariaBuscaAtleta.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaAtleta";

		} else if ("jspNovoDiaTreino".equals(action)) {
			// Carregar p�gina Novo Dia de Treino
			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			DiasSemanaNegocio negocioSemana = new DiasSemanaNegocio();
			List<DiasSemana> listaSemana = negocioSemana.listaSemana();

			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaSemana", listaSemana);
			retorno = String.format("%s/SecretariaNovoDiaTreino.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoDiaTreino";

		} else if ("inserirDiaTreino".equals(action)) {
			// Inserir novo dia de treino
			boolean exception = false;
			String msgSucesso = "";
			String msg = "";
			DiaTreino dia = new DiaTreino();
			DiaTreinoNegocio negocio = new DiaTreinoNegocio();

			String inicio = request.getParameter("hrInicio");
			String fim = request.getParameter("hrFim");
			Date hrInicio = null;
			Date hrFim = null;

			try {
				DateFormat formatter = new SimpleDateFormat("HH:mm");
				hrInicio = (Date) formatter.parse(inicio);
				hrFim = (Date) formatter.parse(fim);
			} catch (Exception ex) {
				msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				exception = true;
			}

			try {
				dia.setIdTpEquipe(Integer.parseInt(request
						.getParameter("tpEquipe")));
				dia.setIdDiaDaSemana(Integer.parseInt(request
						.getParameter("diaSemana")));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o Tipo de Equipe e/ou Dia da Semana!";
				exception = true;
			}

			if (!exception) {
				dia.setHrInicio(hrInicio);
				dia.setHrFim(hrFim);

				List<Object> listaValidacao = negocio.validaDados(dia);
				boolean valida = (boolean) listaValidacao.get(0);
				if (!valida) {
					msg = (String) listaValidacao.get(1);
				} else {
					try {
						if (negocio.inserir(dia)) {
							msgSucesso = "Dia de Treino salvo com sucesso!";
						} else {
							msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
						}
					} catch (Exception ex) {
						msg = ex.getMessage();
					}
				}
			}

			EquipesNegocio negocioEquipe = new EquipesNegocio();
			List<Equipes> listaEquipes = negocioEquipe.listaEquipes();

			DiasSemanaNegocio negocioSemana = new DiasSemanaNegocio();
			List<DiasSemana> listaSemana = negocioSemana.listaSemana();

			request.setAttribute("listaEquipes", listaEquipes);
			request.setAttribute("listaSemana", listaSemana);

			if (("").equals(msgSucesso)) {
				request.setAttribute("msgErro", msg);
			} else {
				request.setAttribute("msgSucesso", msgSucesso);
			}

			retorno = String.format("%s/SecretariaNovoDiaTreino.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaDiaTreino";

		} else if ("jspBuscaDiaTreino".equals(action)) {
			// Carregar p�gina Buscar Dias de Treinos
			DiaTreinoNegocio negocio = new DiaTreinoNegocio();
			List<DiaTreino> lista = new ArrayList<DiaTreino>();
			try {
				lista = negocio.buscaDiasTreino();
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			request.setAttribute("listaDiasTreinos", lista);
			retorno = String.format("%s/SecretariaBuscaDiaTreino.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaDiaTreino";

		} else if ("desativarDiaTreino".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			DiaTreino dia = new DiaTreino(Integer.parseInt(request
					.getParameter("idDiaTreino")));
			DiaTreinoNegocio negocio = new DiaTreinoNegocio();
			List<DiaTreino> lista = new ArrayList<DiaTreino>();

			try {
				if (negocio.desativar(dia)) {
					msgSucesso = "Dia de Treino excluido com sucesso!";
				} else {
					msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				}
				lista = negocio.buscaDiasTreino();
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("listaDiasTreinos", lista);
			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			retorno = String.format("%s/SecretariaBuscaDiaTreino.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaDiaTreino";

		} else if ("jspNovoUsuario".equals(action)) {
			// Carregar p�gina Novo Usuario
			PerfisNegocio negocio = new PerfisNegocio();
			List<Perfis> lista = negocio.listaPerfis();

			request.setAttribute("listaPerfis", lista);
			retorno = String.format("%s/SecretariaNovoUsuario.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoUsuario";

		} else if ("inserirUsuario".equals(action)) {
			// Inserir novo usu�rio
			boolean exception = false;
			String msg = "";
			String msgSucesso = "";
			Usuario usuario = new Usuario();
			UsuarioNegocio negocio = new UsuarioNegocio();

			try {
				String perfil = request.getParameter("perfil");
				usuario.setPerfil(Integer.parseInt(perfil));
			} catch (Exception ex) {
				msg = "Favor selecionar corretamente o Perfil do Usu�rio!";
				exception = true;
			}
			if (!exception) {
				// Dados do usu�rio
				String idUsuario = request.getParameter("idUsuario");
				usuario.setNome(request.getParameter("nome"));
				usuario.setCREF(request.getParameter("cref"));
				usuario.setEmail(request.getParameter("email"));
				usuario.setCelular(request.getParameter("telcelular"));
				usuario.setTelefone(request.getParameter("telresidencial"));

				try {
					if (!"".equals(idUsuario) && !"0".equals(idUsuario)) {
						usuario.setIdPessoa(Integer.parseInt(idUsuario));
					}
					// Valida dados do usu�rio
					List<Object> listaValidacao = negocio.validaDados(usuario);
					boolean valida = (boolean) listaValidacao.get(0);

					if (valida) {
						if (usuario.getIdPessoa() == 0) {
							if (negocio.inserir(usuario)) {
								msgSucesso = "Usu�rio cadastrado com sucesso!";
							} else {
								msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
							}
						} else {
							if (negocio.alterar(usuario)) {
								msgSucesso = "Usu�rio alterado com sucesso!";
							} else {
								msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
							}
						}
					} else {
						msg = (String) listaValidacao.get(1);
					}
				} catch (Exception ex) {
					msg = ex.getMessage();
				}
			}

			PerfisNegocio perfisNegocio = new PerfisNegocio();
			List<Perfis> lista = perfisNegocio.listaPerfis();

			request.setAttribute("listaPerfis", lista);
			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			if ("".equals(msgSucesso)) {
				request.setAttribute("usuario", usuario);
			}
			retorno = String.format("%s/SecretariaNovoUsuario.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaUsuario";

		} else if ("jspBuscaUsuario".equals(action)) {
			// Carregar p�gina Buscar Usuario
			UsuarioNegocio negocio = new UsuarioNegocio();
			List<Usuario> lista = new ArrayList<Usuario>();
			try {
				lista = negocio.buscarUsuarios();
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			request.setAttribute("listaUsuarios", lista);
			retorno = String.format("%s/SecretariaBuscaUsuario.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaUsuario";

		} else if ("editarUsuario".equals(action)) {
			int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
			Usuario usuario = new Usuario();

			UsuarioNegocio negocio = new UsuarioNegocio();

			try {
				usuario = negocio.buscarUsuario(idUsuario);
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}

			PerfisNegocio perfisNegocio = new PerfisNegocio();
			List<Perfis> lista = perfisNegocio.listaPerfis();

			request.setAttribute("listaPerfis", lista);
			request.setAttribute("usuario", usuario);
			retorno = String.format("%s/SecretariaNovoUsuario.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=editarUsuario&idUsuario="
					+ request.getParameter("idUsuario");

		} else if ("desativarUsuario".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			Usuario usuario = new Usuario();
			usuario.setIdPessoa(Integer.parseInt(request
					.getParameter("idUsuario")));

			UsuarioNegocio negocio = new UsuarioNegocio();
			List<Usuario> lista = new ArrayList<Usuario>();

			try {
				if (negocio.desativar(usuario)) {
					msgSucesso = "Usu�rio excluido com sucesso!";
				} else {
					msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				}
				lista = negocio.buscarUsuarios();
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("listaUsuarios", lista);
			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			retorno = String.format("%s/SecretariaBuscaUsuario.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaUsuario";

		} else if ("jspNovoResponsavel".equals(action)) {
			retorno = String.format("%s/SecretariaNovoResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspNovoResponsavel";

		} else if ("inserirResponsavel".equals(action)) {
			String msgSucesso = "";
			String msg = "";
			Responsavel responsavel;
			ResponsavelNegocio responsavelNegocio = new ResponsavelNegocio();
			ArrayList<Endereco> listaEnderecos = new ArrayList<Endereco>();
			Endereco enderecoItem;
			EnderecoNegocio enderecoNegocio = new EnderecoNegocio();

			try {
				// dados da pessoa
				String nome = request.getParameter("nome");
				String email = request.getParameter("email");
				String celular = request.getParameter("celular");

				// endereco residencial
				String endereco = request.getParameter("endereco");
				int numero = Integer.parseInt(request.getParameter("numero"));
				String complemento = request.getParameter("complemento");
				String bairro = request.getParameter("bairro");
				String estado = request.getParameter("estado");
				String cidade = request.getParameter("cidade");
				String telefone = request.getParameter("telefone");

				enderecoItem = new Endereco(endereco, numero, complemento,
						bairro, estado, cidade,
						TpEndereco.Residencial.getValor(), telefone);
				List<Object> listaValidacao = enderecoNegocio
						.validar(enderecoItem);

				if (!(boolean) listaValidacao.get(0)) {
					msg = (String) listaValidacao.get(1);
				} else {
					listaEnderecos.add(enderecoItem);

					// endereco comercial
					endereco = request.getParameter("enderecoCom");
					numero = Integer
							.parseInt(request.getParameter("numeroCom"));
					complemento = request.getParameter("complementoCom");
					bairro = request.getParameter("bairroCom");
					estado = request.getParameter("estadoCom");
					cidade = request.getParameter("cidadeCom");
					telefone = request.getParameter("telefoneCom");

					enderecoItem = new Endereco(endereco, numero, complemento,
							bairro, estado, cidade,
							TpEndereco.Comercial.getValor(), telefone);
					listaValidacao = enderecoNegocio.validar(enderecoItem);

					if (!(boolean) listaValidacao.get(0)) {
						msg = (String) listaValidacao.get(1);
					} else {
						listaEnderecos.add(enderecoItem);
						responsavel = new Responsavel(0, nome, email, celular,
								listaEnderecos);
						List<Object> listaValidacaoResponsavel = responsavelNegocio
								.validar(responsavel);

						if (!(boolean) listaValidacaoResponsavel.get(0)) {
							msg = (String) listaValidacaoResponsavel.get(1);
						} else {
							try {
								// verifica se é uma inser��o ou altera��o
								String idResponsavel = request
										.getParameter("idResponsavel");
								String idEnderecoResidencial = request
										.getParameter("idEnderecoResidencial");
								String idEnderecoComercial = request
										.getParameter("idEnderecoComercial");

								if (!"".equals(idResponsavel)
										&& !(idResponsavel == null)) {
									responsavel.setIdPessoa(Integer
											.parseInt(idResponsavel));
									responsavel
											.getEnderecos()
											.get(0)
											.setIdEndereco(
													Integer.parseInt(idEnderecoResidencial));
									responsavel
											.getEnderecos()
											.get(1)
											.setIdEndereco(
													Integer.parseInt(idEnderecoComercial));

									if (responsavelNegocio.alterar(responsavel)) {
										// seta os dados para que sejam
										// carregados na pagina de retorno
										msgSucesso = "Respons�vel alterado com sucesso!";
										Endereco enderecoResidencial = responsavel
												.getEnderecos().get(0)
												.getTpEndereco() == TpEndereco.Residencial
												.getValor() ? responsavel
												.getEnderecos().get(0)
												: responsavel.getEnderecos()
														.get(1);
										Endereco enderecoComercial = responsavel
												.getEnderecos().get(1)
												.getTpEndereco() == TpEndereco.Comercial
												.getValor() ? responsavel
												.getEnderecos().get(1)
												: responsavel.getEnderecos()
														.get(0);

										request.setAttribute("responsavel",
												responsavel);
										request.setAttribute(
												"enderecoResidencial",
												enderecoResidencial);
										request.setAttribute(
												"enderecoComercial",
												enderecoComercial);

									} else
										msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";

								} else {
									if (responsavelNegocio.inserir(responsavel))
										msgSucesso = "Respons�vel salvo com sucesso!";
									else
										msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
								}
							} catch (ParseException e) {
								msg = e.getMessage();
							} catch (Exception e) {
								msg = e.getMessage();
							}
						}
					}
				}
			} catch (NumberFormatException ex) {
				msg = "Favor informar corretamente o campo 'N�mero' dos endere�os";
			} catch (ParseException ex) {
				msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
			}

			request.setAttribute("msgSucesso", msgSucesso);
			request.setAttribute("msgErro", msg);
			retorno = String.format("%s/SecretariaNovoResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaResponsavel";

		} else if ("jspBuscaResponsavel".equals(action)) {
			ResponsavelNegocio responsavelNegocio = new ResponsavelNegocio();
			ArrayList<Responsavel> listaResponsaveis = null;
			String msg = "";

			try {
				listaResponsaveis = responsavelNegocio.buscarTodos();
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("msgErro", msg);
			request.setAttribute("listaResponsaveis", listaResponsaveis);
			retorno = String.format("%s/SecretariaBuscaResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaResponsavel";

		} else if ("desativarResponsavel".equals(action)) {
			ArrayList<Responsavel> listaResponsaveis = null;
			String msgSucesso = "";
			String msg = "";

			try {
				int idResponsavel = Integer.parseInt(request
						.getParameter("idResponsavel"));
				ResponsavelNegocio responsavelNegocio = new ResponsavelNegocio();

				// desativa o responsavel
				if (responsavelNegocio.desativar(idResponsavel)) {
					msgSucesso = "Respons�vel exclu�do com sucesso!";
				} else {
					msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
				}
				// busca os responsaveis denovo
				listaResponsaveis = responsavelNegocio.buscarTodos();

			} catch (Exception ex) {
				msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";
			}

			request.setAttribute("msgErro", msg);
			request.setAttribute("msgSucesso", msgSucesso);
			request.setAttribute("listaResponsaveis", listaResponsaveis);
			retorno = String.format("%s/SecretariaBuscaResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspBuscaResponsavel";

		} else if ("editarResponsavel".equals(action)) {
			String msg = "";
			Responsavel responsavel;
			ResponsavelNegocio responsavelNegocio = new ResponsavelNegocio();
			try {
				// dados da pessoa
				int idResponsavel = Integer.parseInt(request
						.getParameter("idResponsavel"));
				responsavel = responsavelNegocio.buscarPorId(idResponsavel);
				Endereco enderecoResidencial = responsavel.getEnderecos()
						.get(0).getTpEndereco() == TpEndereco.Residencial
						.getValor() ? responsavel.getEnderecos().get(0)
						: responsavel.getEnderecos().get(1);
				Endereco enderecoComercial = responsavel.getEnderecos().get(1)
						.getTpEndereco() == TpEndereco.Comercial.getValor() ? responsavel
						.getEnderecos().get(1) : responsavel.getEnderecos()
						.get(0);

				request.setAttribute("responsavel", responsavel);
				request.setAttribute("enderecoResidencial", enderecoResidencial);
				request.setAttribute("enderecoComercial", enderecoComercial);
			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("msgErro", msg);
			retorno = String.format("%s/SecretariaNovoResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=editarResponsavel&idResponsavel="
					+ request.getParameter("idResponsavel");

		} else if ("enviarEmailResponsavel".equals(action)) {
			String msg = "Ocorreu algum erro no sistema! Favor tentar novamente.";

			String email = request.getParameter("emailResponsavel");

			if (email.equals("") || email == null) {
				request.setAttribute("msgErro", msg);
				retorno = String.format("%s/SecretariaBuscaResponsavel.jsp",
						Constants.VIEW);
				servletRetorno = "/SecretariaController?action=jspBuscaResponsavel";
			} else {
				request.setAttribute("emailResponsavel", email);
				retorno = String.format(
						"%s/SecretariaEnviarEmailResponsavel.jsp",
						Constants.VIEW);
				servletRetorno = "/SecretariaController?action=enviarEmailResponsavel&emailResponsavel="
						+ email;
			}

		} else if ("enviarEmailIndividual".equals(action)) {
			String msg = "";
			String msgSucesso = "";
			String destinatario = request.getParameter("emailResponsavel");
			String assunto = request.getParameter("assunto");
			String msgEmail = request.getParameter("mensagemEmail");

			if (msgEmail.equals("") || msgEmail == null) {
				msg = "Informe corretamente o campo 'Mensagem'";
			} else {
				if (assunto.equals("") || assunto == null) {
					assunto = "Sem assunto";
				}

				try {
					JavaMailApp email = new JavaMailApp();
					email.enviarEmailResponsavel(destinatario, assunto,
							msgEmail);
					msgSucesso = "Email enviado para " + destinatario
							+ " com sucesso!";

				} catch (Exception ex) {
					msg = "Falha ao enviar email!";
				}
			}
			request.setAttribute("emailResponsavel", destinatario);
			request.setAttribute("msgSucesso", msgSucesso);
			request.setAttribute("msgErro", msg);
			retorno = String.format("%s/SecretariaEnviarEmailResponsavel.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=enviarEmailResponsavel&emailResponsavel="
					+ destinatario;

		} else if ("jspAnexarDocumentosAtleta".equals(action)) {
			String nomeAtleta = request.getParameter("nome");
			
			request.setAttribute("nomeAtleta", nomeAtleta);
			request.setAttribute("idPessoa", request.getParameter("idPessoa"));
			listarDocumentosAtleta(request);
			retorno = String.format("%s/SecretariaAnexarDocumentos.jsp",
					Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspAnexarDocumentosAtleta&idPessoa=" 
					+ request.getParameter("idPessoa") + "&nome=" + request.getParameter("nome");;

		} else if ("anexarDocumento".equals(action)) {
			String msgSucesso = "";
			String msg = "";
			Documento documento = new Documento();

			try {
				DocumentoNegocio documentoNegocio = new DocumentoNegocio();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

				@SuppressWarnings("deprecation")
				DiskFileUpload fu = new DiskFileUpload();
				@SuppressWarnings("deprecation")
				// pega uma lista com todos os itens do form
				List ListaItensFormulario = fu.parseRequest(request);
				Iterator i = ListaItensFormulario.iterator();
				// Itera a lista
				while (i.hasNext()) {
					FileItem item = (FileItem) i.next();
					String s = "";
					// se for um form field resgata o valor e insere no objeto;
					if (item.isFormField()) {
						if (item.getFieldName().equals("idPessoa")) {
							s = item.getString();
							if (!s.equals("") && s != null)
								documento.setIdPessoa(Integer.parseInt(s));
						} else if (item.getFieldName().equals("idTpDocumento")) {
							s = item.getString();
							if (!s.equals("") && s != null)
								documento.setTpDocumento(Integer.parseInt(s));
						} else if (item.getFieldName().equals("dtValidade")) {
							s = item.getString();
							if (!s.equals("") && s != null)
								documento.setValidade(df.parse(s));
							// documento.setValidade(new java.util.Date(s));
						}
						// se n�o for um form field é um arquivo
					} else {
						String path = getUploadPath(documento);
						InputStream in = item.getInputStream();
						String nmDocumento = nomearArquivo(
								documento.getTpDocumento(),
								documento.getIdPessoa(), item.getName());
						if (!nmDocumento
								.equals("Extens�o de arquivo inv�lida!")
								&& !nmDocumento
										.equals("Tipo de arquivo inv�lido!")) {
							path += "/" + nmDocumento;
							File arquivo = new File(path);

							FileOutputStream out = new FileOutputStream(arquivo);
							// l� o input e joga dentro do arquivo através de um
							// OutputStream
							int c;
							while ((c = in.read()) != -1)
								out.write(c);
							out.close();
//							EM DESENV: COLOCAR UM "\\" NA FRENTE DO NOME DA PASTA
							documento.setSrc("saatDocumentacaoAtletas"
									+ "/"
									+ String.valueOf(documento.getIdPessoa()
											+ "/" + nmDocumento));
							// documento.setSrc("\\\\..\\\\saatDocumentacaoAtletas"
							// +"\\\\" + String.valueOf(documento.getIdPessoa()
							// + "\\\\" + nmDocumento));
							msgSucesso = "Arquivo anexado com sucesso";
						} else {
							msg = nmDocumento;
						}
					}
				}
				
				if("".equals(msg)){
					int idDocumentoBanco = documentoNegocio.exists(documento);
					if (idDocumentoBanco != 0) {
						documento.setIdDocumento(idDocumentoBanco);
						if (!documentoNegocio.alterar(documento)) {
							msg = "Erro ao gravar documento no banco de dados!";
						}
					} else {
						if (!documentoNegocio.inserir(documento)) {
							msg = "Erro ao gravar documento no banco de dados!";
						}
					}
				}

			} catch (Exception ex) {
				msg = ex.getMessage();
			}

			request.setAttribute("idPessoa", documento.getIdPessoa());
			String msgRetorno = listarDocumentosAtleta(request);
			if(msgRetorno != null)
				msg = msgRetorno;
			request.setAttribute("msgErro", msg);
			if("".equals(msg))
				request.setAttribute("msgSucesso", msgSucesso);
			retorno = String.format("%s/SecretariaAnexarDocumentos.jsp",
					Constants.VIEW);
		} else if ("buscarAtletasVinculados".equals(action)) {
			String msg = "";
			int idResponsavel = Integer.parseInt(request.getParameter("idResponsavel"));
			List<Atleta> listaAtleta = new ArrayList<Atleta>();
			AtletaNegocio negocio = new AtletaNegocio();
			
			try{
				listaAtleta = negocio.buscarAtletasVinculados(idResponsavel);
			}catch(Exception ex){
				msg = ex.getMessage();
			}		
			
			List<String> listaGrauParentesco = new GrauParentescoNegocio().listaGrausString();

			Map<String, Object> lista = new LinkedHashMap<String, Object>();
			lista.put("listaAtleta", listaAtleta);
			lista.put("grauParentesco", listaGrauParentesco);
			
			String json = new Gson().toJson(lista);

		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    response.getWriter().write(json);
		    request.setAttribute("msgErro", msg);
		    
		} else if("jspRelatorioTreinos".equals(action)){			
			request.setAttribute("dataAtual", new Date());
			retorno = String.format("%s/RelatorioTreino.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspRelatorioTreinos";
			
		} else if("jspRelatorioConsultaMedica".equals(action)){			
			request.setAttribute("dataAtual", new Date());
			retorno = String.format("%s/RelatorioConsultaMedica.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspRelatorioConsultaMedica";
			
		}else if ("jspResulTorneio".equals(action)) {			
			TorneioNegocio negocio = new TorneioNegocio();
			List<Torneio> lista = new ArrayList<Torneio>();
			
			try {
				lista = negocio.buscaTorneiosFinalizados();
				if (lista.isEmpty()) {
					request.setAttribute("msgAlerta", "Nenhum resultado de torneio finalizado dispon�vel!");
				} else {
					request.setAttribute("listaTorneios", lista);
				}
			} catch (Exception ex) {
				request.setAttribute("msgErro", ex.getMessage());
			}
			retorno = String.format("%s/RelatorioResultTorneio.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspResulTorneio";
			
		} else if("jspFrequenciaTorneio".equals(action)){			
			request.setAttribute("dataAtual", new Date());
			retorno = String.format("%s/RelatorioFreqTorneio.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspFrequenciaTorneio";
			
		} else if("jspCalendario".equals(action)){
			retorno = String.format("%s/TecnicoCalendarioTorneio.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspCalendario";
		
		} else if("jspEnviarComunicado".equals(action)){
			retorno = String.format("%s/SecretariaEnviarComunicado.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspEnviarComunicado";
			
		}else if("enviarComunicado".equals(action)){
			String msgErro = "";
			String optEnvio = request.getParameter("optradio");
			String assunto = request.getParameter("assunto");
			String mensagem = request.getParameter("mensagemEmail");
			int envio = 0;
			
			try{
				envio = Integer.parseInt(optEnvio);
			}catch(Exception ex){
				msgErro = "Erro ao identificar os destinat�rios do email!";
			}
			
			if(msgErro.equals("") && envio != 0){
				ResponsavelNegocio negocio = new ResponsavelNegocio();
				try{
					if(negocio.enviarEmailResponsaveis(envio, assunto, mensagem)){
						request.setAttribute("msgSucesso", "Comunicado enviado com sucesso!");
					}
				}catch(Exception ex){
					msgErro = ex.getMessage();
				}
			}
			request.setAttribute("msgErro", msgErro);
			retorno = String.format("%s/SecretariaEnviarComunicado.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspEnviarComunicado";
			
		} else if("jspRelatorioBonificacao".equals(action)){
			retorno = String.format("%s/RelatorioBonificacao.jsp", Constants.VIEW);
			servletRetorno = "/SecretariaController?action=jspRelatorioBonificacao";
			
		}else {
			retorno = "/SecretariaController?action=jspPaginaInicialSecretaria";
			servletRetorno = "/SecretariaController?action=jspPaginaInicialSecretaria";
		}

		if (retorno != null) {
			session.setAttribute("pagina", servletRetorno);
			rd = getServletContext().getRequestDispatcher(retorno);
			rd.forward(request, response);
		}
	}

	private String listarDocumentosAtleta(HttpServletRequest request) {
		String msg = request.getParameter("msgErro");
		int idPessoa = 0;

		Documento termoDeCompromisso = null;
		Documento declaracaoMedica = null;
		Documento autorizacaoViagem = null;
		Documento autorizacaoImagem = null;
		Documento copiaRG = null;
		Documento copiaCPF = null;
		Documento fotoAtleta = null;

		try {
			idPessoa = Integer.parseInt(request.getAttribute("idPessoa")
					.toString());
			DocumentoNegocio documentoNegocio = new DocumentoNegocio();

			ArrayList<Documento> listaDocumento = documentoNegocio
					.buscarTodosAtleta(idPessoa);

			for (Documento documento : listaDocumento) {
				if (documento.getTpDocumento() == TpDocumento.termoDeCompromisso
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					termoDeCompromisso = documento;
				} else if (documento.getTpDocumento() == TpDocumento.declaracaoMedica
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					declaracaoMedica = documento;
				} else if (documento.getTpDocumento() == TpDocumento.autorizacaoDeViagem
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					autorizacaoViagem = documento;
				} else if (documento.getTpDocumento() == TpDocumento.autorizacaoDeImagem
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					autorizacaoImagem = documento;
				} else if (documento.getTpDocumento() == TpDocumento.copiaDoRG
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					copiaRG = documento;
				} else if (documento.getTpDocumento() == TpDocumento.copiaDoCPF
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					copiaCPF = documento;
				} else if (documento.getTpDocumento() == TpDocumento.fotoDoAtleta
						.getValor()) {
					// troca todos os '/' por '//' - importante p/ visualiza��o
					// no js
					documento.setSrc(documento.getSrc().replace("\\", "\\\\"));
					fotoAtleta = documento;
				}
			}

		} catch (ParseException ex) {
			msg = "idPessoa Inv�lido!";
		} catch (Exception ex) {
			msg = ex.getMessage();
		}

		request.setAttribute("idPessoa", idPessoa);
		request.setAttribute("termoDeCompromisso", termoDeCompromisso);
		request.setAttribute("declaracaoMedica", declaracaoMedica);
		request.setAttribute("autorizacaoViagem", autorizacaoViagem);
		request.setAttribute("autorizacaoImagem", autorizacaoImagem);
		request.setAttribute("copiaRG", copiaRG);
		request.setAttribute("copiaCPF", copiaCPF);
		request.setAttribute("fotoAtleta", fotoAtleta);
		return msg;
	}

	private String nomearArquivo(int tpDocumento, int idPessoa, String nmArquivo) {
		String nmDocumento = "";
		String[] explode = nmArquivo.split("\\.");
		String extensao = explode[(explode.length - 1)];
		List<String> extensoesValidas = new ArrayList<String>() {
			{
				add("jpg");
				add("jpeg");
				add("png");
				add("pdf");
				add("doc");
				add("docx");
				add("xls");
				add("xlsx");
			}
		};

		if (!extensao.equals("") || extensao != null) {
			if (!extensoesValidas.contains(extensao.toLowerCase()))
				return "Extens�o de arquivo inv�lida!";
		}

		// valida qual vai ser o nome do documento
		if (tpDocumento == TpDocumento.termoDeCompromisso.getValor())
			nmDocumento = "termo_compromisso_manual";
		else if (tpDocumento == TpDocumento.declaracaoMedica.getValor())
			nmDocumento = "declaracao_medica";
		else if (tpDocumento == TpDocumento.autorizacaoDeViagem.getValor())
			nmDocumento = "autorizacao_viagem_hospedagem";
		else if (tpDocumento == TpDocumento.autorizacaoDeImagem.getValor())
			nmDocumento = "autorizacao_imagem";
		else if (tpDocumento == TpDocumento.copiaDoRG.getValor())
			nmDocumento = "copia_rg";
		else if (tpDocumento == TpDocumento.copiaDoCPF.getValor())
			nmDocumento = "copia_cpf";
		else if (tpDocumento == TpDocumento.fotoDoAtleta.getValor())
			nmDocumento = "foto_atleta";
		else
			return "Tipo de arquivo inv�lido!";

		return String.valueOf(idPessoa) + "_" + nmDocumento + "." + extensao;
	}

	private String getUploadPath(Documento documento) {
		// String path =
		// getServletContext().getRealPath("..\\saatDocumentacaoAtletas" + "\\"
		// + String.valueOf(documento.getIdPessoa()));
		String path = getServletContext().getRealPath("/saatDocumentacaoAtletas/" + String.valueOf(documento.getIdPessoa() + "/"));
//		TRECHO DE CÓDIGO UTILIZADO EM DESENV
//				+ "\\..\\saatDocumentacaoAtletas" + "\\"
//				+ String.valueOf(documento.getIdPessoa());

		// verifica se a pasta do aluno esta criada
		if (criaDiretorio(path)) {
			return path;
		} else {
			return "";
		}
	}

	private boolean criaDiretorio(String path) {
		// Se a pasta n�o existir cria, caso n�o consiga criar retorna falso
		try {
			if (!Paths.get(path).toFile().exists()) {
				File dir = new File(path);
				if (dir.mkdir())
					return true;
				else
					return false;
			} else {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
	}

}
