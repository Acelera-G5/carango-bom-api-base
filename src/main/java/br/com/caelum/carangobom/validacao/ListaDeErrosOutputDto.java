package br.com.caelum.carangobom.validacao;

import java.util.ArrayList;
import java.util.List;

public class ListaDeErrosOutputDto {

    private List<ErroDeParametroOutputDto> erros = new ArrayList<>();

    public void adicionaErroEmParametro(String parametro, String mensagem) {
        erros.add(new ErroDeParametroOutputDto(parametro, mensagem));
    }

    public int getQuantidadeDeErros() {
        return erros.size();
    }

    public List<ErroDeParametroOutputDto> getErros() {
        return erros;
    }

    public void setErros(List<ErroDeParametroOutputDto> erros) {
        this.erros = erros;
    }
}
