	<%@include file='../layout/head.jsp'%>
	<body>
	<div class="container">
		<div class="row clearfix">
			<div class="col-md-12 column">
			<!-- menu -->
				<%@include file='../layout/navigationFisioterapia.jsp'%>
				
				<div class="row clearfix">
					<div class="col-md-3 column">
					</div>
					<div class="col-md-6 column">
						<h3 class="text-center">
							Buscar Atleta
						</h3>
						<form role="form" action="SecretariaListaFisioterapia.jsp">
							<div class="form-group">
								<input type="text" class="form-control" id="search" />
							</div> <button type="submit" class="btn btn-default">Buscar</button>
						</form>
					</div>
					<div class="col-md-3 column">
					</div>
				</div>
				
				<div class="col-md-2 column">
				</div>
				<div class="col-md-8 column">
					<h3 class="text-center">
							Atletas Encontrados
					</h3>
					<table class="table">
					<thead>
						<tr>
							<th>
								Nome
							</th>
							<th>
							</th>
							<th>
							</th>
							<th>
							</th>
							<th>
							</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>
								Atleta 1
							</td>
							<td>
								<a>Anunciar Presen�a</a>
							</td>
							<td>
								<a>Incluir Parecer</a>
							</td>
							<td>
								<a>Ver Hist�rico</a>
							</td>
							<td>
								<a>Excluir</a>
							</td>
						</tr>
						<tr>
							<td>
								Atleta 2
							</td>
							<td>
								<a>Anunciar Presen�a</a>
							</td>
							<td>
								<a>Incluir Parecer</a>
							</td>
							<td>
								<a>Ver Hist�rico</a>
							</td>
							<td>
								<a>Excluir</a>
							</td>
						</tr>
						<tr>
							<td>
								Atleta 3
							</td>
							<td>
								<a>Anunciar Presen�a</a>
							</td>
							<td>
								<a>Incluir Parecer</a>
							</td>
							<td>
								<a>Ver Hist�rico</a>
							</td>
							<td>
								<a>Excluir</a>
							</td>
						</tr>
						<tr>
							<td>
								Atleta 4
							</td>
							<td>
								<a>Anunciar Presen�a</a>
							</td>
							<td>
								<a>Incluir Parecer</a>
							</td>
							<td>
								<a>Ver Hist�rico</a>
							</td>
							<td>
								<a>Excluir</a>
							</td>
						</tr>
						<tr>
							<td>
								Atleta 5<br>
							</td>
							<td>
								<a>Anunciar Presen�a</a>
							</td>
							<td>
								<a>Incluir Parecer</a>
							</td>
							<td>
								<a>Ver Hist�rico</a>
							</td>
							<td>
								<a>Excluir</a>
							</td>
						</tr>
					</tbody>
					</table>
					<ul class="pagination">
						<li>
							<a href="#">Anterior</a>
						</li>
						<li>
							<a href="#">1</a>
						</li>
						<li>
							<a href="#">2</a>
						</li>
						<li>
							<a href="#">3</a>
						</li>
						<li>
							<a href="#">4</a>
						</li>
						<li>
							<a href="#">5</a>
						</li>
						<li>
							<a href="#">Pr�ximo</a>
						</li>
					</ul>
				</div>
				<div class="col-md-2 column">
				</div>
			</div>
		</div>
	</div>	
	
	<%@include file="../layout/footer.jsp"%>
	
  </body>
</html>