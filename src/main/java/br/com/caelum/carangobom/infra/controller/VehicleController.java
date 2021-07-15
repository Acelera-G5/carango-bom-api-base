package br.com.caelum.carangobom.infra.controller;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.service.VehicleService;
import br.com.caelum.carangobom.infra.controller.request.CreateVehicleRequest;
import br.com.caelum.carangobom.infra.controller.response.VehicleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping(path = "/vehicle")
public class VehicleController {
    @Autowired
    VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest createVehicleRequest,
            UriComponentsBuilder uriComponentsBuilder
    ){
        try{
            Vehicle createdVehicle = vehicleService.createVehicle(
                    createVehicleRequest.toVehicleForm()
            );
            URI uri = uriComponentsBuilder.path("/vehicle/{id}").buildAndExpand(createdVehicle.getId()).toUri();
            return ResponseEntity.created(uri).body(new VehicleResponse(createdVehicle));
        }catch (NotFoundException exception){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @Valid @RequestBody CreateVehicleRequest createVehicleRequest,
            @PathVariable Long id
    ) {
        try {
            Vehicle updatedVehicle = this.vehicleService.updateVehicle(createVehicleRequest.toVehicleForm(), id);
            return ResponseEntity.ok(new VehicleResponse(updatedVehicle));
        } catch (NotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public Page<VehicleResponse> getAllVehicles(Pageable pagination){
        return this.vehicleService.listVehicle(pagination).map(VehicleResponse::new);
    }
}
