
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@include file='/layout/head.jsp'%>

<body>

	<%@include file='/layout/header.jsp'%>

	<div id="main" class="container-fluid">
		<div class="row">
			<%@include file='/layout/navigationSecretaria.jsp'%>
			<!--Start Content-->
			<div id="content" class="col-xs-12 col-sm-10">
				<div id="ajax-content">
					<%@include file="Mensagem.jsp"%>
					<div class="row">
						<div class="col-xs-12">
							<div class="box">
								<div class="box-header">
									<div class="box-name">
										<i class="fa  fa-tags"></i> <span>Gerenciamento de Retiradas de Uniformes <b>(<c:out value="${nomeAtleta}"/>)</b></span>
									</div>
									<div class="box-icons">
										<a class="expand-link"> <i class="fa fa-expand"></i>
										</a>
									</div>
									<div class="no-move"></div>
								</div>
								<div class="box-content clearfix">
									<form action="SecretariaController?action=jspRegistrarRetirada" method="post">
										<div class="col-md-12 form-group">
											<div class="col-md-3" align="right">
												<label for="dataQuadra" class="control-label">Data:</label>
											</div>
											<div class="col-md-9">
												<fmt:formatDate value="${dataAtual}" pattern="yyyy-MM-dd" var="data" />
												<input type="date" class="form-control" required style="width: 30%" name="dataRetirada"
													value="<c:out value="${data}"/>" id="dataRetirada" />
											</div>
											<table class="table">
												<thead>
													<tr>
														<th width="60%">Tipo</th>
														<th width="20%">Tamanho</th>
														<th width="20%">Quantidade</th>
													</tr>
												</thead>
												<tbody>
													<c:forEach var="uniforme" items="${listaUniformes}">
													<tr>
														<td><c:out value='${uniforme.getNome()}' /></td>
														<td>
															<select name="tamanho-${uniforme.getValor()}" class="form-control">
																<option value="0" selected>Selecione</option>
																<c:forEach var="tamanho" items="${listaTamanhos}">
																	<option value="${tamanho.getValor()}">
																		<c:out value="${tamanho.getNome()}"></c:out>
																	</option>
																</c:forEach>
															</select>
														</td>
														<td><input type="number" name="qtd-${uniforme.getValor()}"/></td>
													</tr>
												</c:forEach>
												</tbody>
											</table>
										</div>
										<div align="right">
											<button type="submit" class="btn btn-primary">Salvar</button>
										</div>
										<input type="hidden" value="${nomeAtleta}" name="nomeAtleta">
										<input type="hidden" value="${idPessoa}" name="idAtleta">
									</form>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!--End Content-->
		</div>
	</div>

	<%@include file="/layout/footer.jsp"%>


</body>
</html>

