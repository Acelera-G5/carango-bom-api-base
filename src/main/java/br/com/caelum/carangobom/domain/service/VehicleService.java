package br.com.caelum.carangobom.domain.service;

import br.com.caelum.carangobom.domain.entity.Marca;
import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import br.com.caelum.carangobom.domain.entity.form.VehicleForm;
import br.com.caelum.carangobom.domain.repository.MarcaRepository;
import br.com.caelum.carangobom.domain.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VehicleService {

    private MarcaRepository marcaRepository;
    private VehicleRepository vehicleRepository;

    @Autowired
    public VehicleService(MarcaRepository marcaRepository, VehicleRepository vehiceRepository){
        this.marcaRepository = marcaRepository;
        this.vehicleRepository = vehiceRepository;
    }

    private void setMarcaOnVehicle(Vehicle vehicle, Long marcaId) throws NotFoundException {
        Optional<Marca> marca = marcaRepository.findById(marcaId);
        if(!marca.isPresent()){
            throw new NotFoundException("Marca not found");
        }
        vehicle.setMarca(marca.get());
    }

    public Vehicle createVehicle(VehicleForm vehicle) throws NotFoundException {
        this.setMarcaOnVehicle(vehicle, vehicle.getMarcaId());
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(VehicleForm vehicle, Long vehicleId) throws NotFoundException {
        Optional<Vehicle> optionalSavedVehicle = this.vehicleRepository.findById(vehicleId);
        if(!optionalSavedVehicle.isPresent()){
            throw new NotFoundException("Vehicle not found");
        }
        Vehicle savedVehicle = optionalSavedVehicle.get();
        this.setMarcaOnVehicle(vehicle, vehicle.getMarcaId());
        savedVehicle.setMarca(vehicle.getMarca());
        savedVehicle.setModel(vehicle.getModel());
        savedVehicle.setPrice(vehicle.getPrice());
        savedVehicle.setYear(vehicle.getYear());
        return this.vehicleRepository.save(savedVehicle);
    }

    public Page<Vehicle> listVehicle(Pageable pageable, SearchVehicleForm searchVehicleForm) {
        return this.vehicleRepository.getAll(pageable, searchVehicleForm);
    }

    public Vehicle getVehicleById(Long id) throws NotFoundException {
        Optional<Vehicle> vehicle = this.vehicleRepository.findById(id);
        if(vehicle.isPresent()){
            return vehicle.get();
        }else {
            throw new NotFoundException("Vehicle not found");
        }
    }

    public void deleteVehicleById(Long id) throws NotFoundException {
        this.vehicleRepository.deleteVehicle(id);
    }
}
