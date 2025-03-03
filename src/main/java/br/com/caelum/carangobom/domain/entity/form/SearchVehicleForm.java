package br.com.caelum.carangobom.domain.entity.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SearchVehicleForm {
    private Long marcaId;
    private Integer year;
    private String model;
    private Double priceMin;
    private Double priceMax;
}
