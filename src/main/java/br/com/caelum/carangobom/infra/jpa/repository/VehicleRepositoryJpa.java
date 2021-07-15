package br.com.caelum.carangobom.infra.jpa.repository;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.repository.VehicleRepository;
import br.com.caelum.carangobom.infra.jpa.entity.VehicleJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class VehicleRepositoryJpa implements VehicleRepository {

    private EntityManager entityManager;

    @Autowired
    VehicleRepositoryJpa(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    private VehicleJpa vehicleToVehicleJpa(Vehicle vehicle){
        return new VehicleJpa(vehicle);
    }

    private List<Vehicle> getAllVehicles(){
        return entityManager.createQuery("Select v from vehicle v").getResultList();
    }

    private List<Vehicle> getVehiclePage(Pageable pageable){
        Query query = entityManager.createQuery("From vehicle",VehicleJpa.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleJpa vehicleJpa = vehicleToVehicleJpa(vehicle);
        if(vehicle.getId() != null){
            this.entityManager.merge(vehicleJpa);
        }else {
            this.entityManager.persist(vehicleJpa);
        }
        return vehicleJpa;
    }

    @Override
    public Optional<Vehicle> findById(Long vehicleId) {
        VehicleJpa vehicleJpa = this.entityManager.find(VehicleJpa.class, vehicleId);
        return Optional.ofNullable(vehicleJpa);
    }

    @Override
    public Page<Vehicle> getAll(Pageable pageable) {
        List<Vehicle> vehicleList;
        if(pageable.isPaged()){
            vehicleList = this.getVehiclePage(pageable);
        }else{
            vehicleList = this.getAllVehicles();
        }
        Long countResult = entityManager.createQuery("Select count(v.id) From vehicle v", Long.class).getSingleResult();

        return new PageImpl<>(
                vehicleList,
                pageable,
                countResult
        );
    }
}
