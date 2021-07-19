package br.com.caelum.carangobom.infra.controller.request;

import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class SearchVehicleRequest {
    @Positive
    Long marcaId;

    @Positive
    Integer year;

    String model;

    Double priceMin;

    Double priceMax;

    public SearchVehicleForm toSearchVehicleForm(){
        return new SearchVehicleForm(marcaId,year, model,priceMin,priceMax);
    }
}
