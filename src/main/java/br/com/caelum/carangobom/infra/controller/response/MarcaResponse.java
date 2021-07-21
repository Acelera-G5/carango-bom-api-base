package br.com.caelum.carangobom.infra.controller.response;

import br.com.caelum.carangobom.domain.entity.Marca;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcaResponse {

	private Long id;
	private String nome;

	public MarcaResponse(Marca marca) {
		this.setId(marca.getId());
		this.setNome(marca.getNome());
	}
}
