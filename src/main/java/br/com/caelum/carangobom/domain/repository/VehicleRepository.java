package br.com.caelum.carangobom.domain.repository;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(Long vehicleId);
    Page<Vehicle> getAll(Pageable pageable, SearchVehicleForm searchVehicleForm);

    void deleteVehicle(Long id) throws NotFoundException;
}
