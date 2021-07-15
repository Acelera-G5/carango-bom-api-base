package br.com.caelum.carangobom.domain.repository;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VehicleRepositoryMock implements VehicleRepository {

    private List<Vehicle> vehicleList = new ArrayList<Vehicle>();

    private List<Vehicle> filterVehiclesByMarcaId(List<Vehicle> vehicles, Long marcaId){
        return vehicles.stream().filter(vehicle -> vehicle.getMarca().getId().equals(marcaId)).collect(Collectors.toList());
    }
    private List<Vehicle> filterVehicles(List<Vehicle> vehicles, SearchVehicleForm searchVehicleForm){
        List<Vehicle> filteredVehicles = vehicles;
        if(searchVehicleForm.getMarcaId() != null){
            filteredVehicles = this.filterVehiclesByMarcaId(filteredVehicles, searchVehicleForm.getMarcaId());
        }
        return filteredVehicles;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        vehicle.setId(1L);
        vehicleList.add(vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(Long vehicleId) {
        return this.vehicleList
                .stream()
                .filter(vehicle -> vehicle.getId().equals(vehicleId))
                .findFirst();
    }

    @Override
    public Page<Vehicle> getAll(Pageable pageable, SearchVehicleForm searchVehicleForm) {
        List<Vehicle> vehicles;
        if(pageable.isPaged()){
            vehicles = this.vehicleList.subList(
                    (int)pageable.getOffset(),
                    (int)(pageable.getOffset()+pageable.getPageSize())
            );
        }else{
            vehicles = this.vehicleList;
        }
        if(searchVehicleForm != null){
            vehicles = this.filterVehicles(vehicles, searchVehicleForm);
        }
        return new PageImpl(
                vehicles,
                pageable,
                vehicles.size()
        );

    }

    @Override
    public void deleteVehicle(Long id) throws NotFoundException {
        Optional<Vehicle> optionalVehicle = this.findById(id);
        if(optionalVehicle.isPresent()){
            this.vehicleList = this.vehicleList.stream().filter(vehicle -> vehicle.getId() != id).collect(Collectors.toList());
        }else{
            throw new NotFoundException("Vehicle not found");
        }

    }
}
