package br.com.caelum.carangobom.infra.jpa.repository;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import br.com.caelum.carangobom.domain.repository.VehicleRepository;
import br.com.caelum.carangobom.infra.jpa.entity.VehicleJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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

    private Long countVehicles(SearchVehicleForm searchVehicleForm){
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQueryLong = criteriaBuilder.createQuery(Long.class);
        Root<VehicleJpa> vehicleJpaRoot = criteriaQueryLong.from(VehicleJpa.class);
        this.createFilterQuery(criteriaQueryLong, vehicleJpaRoot, searchVehicleForm);
        criteriaQueryLong.select(criteriaBuilder.count(vehicleJpaRoot));
        Query query = this.entityManager.createQuery(criteriaQueryLong);
        return (Long) query.getSingleResult();
    }

    private List<Vehicle> getAllVehicles(Query query){
        return query.getResultList();
    }

    private List<Vehicle> getVehiclePage(Query query, Pageable pageable){
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    private void filterByMarcaId(Root<VehicleJpa> root, CriteriaQuery<VehicleJpa> criteriaQuery, Long marcaId){
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        criteriaQuery.where(criteriaBuilder.equal(root.join("marca").get("id"), marcaId));
    }

    private void createFilterQuery(CriteriaQuery criteriaQuery, Root<VehicleJpa> root, SearchVehicleForm searchVehicleForm){
        if(searchVehicleForm == null){
            return;
        }
        if(searchVehicleForm.getMarcaId() != null){
            this.filterByMarcaId(root, criteriaQuery, searchVehicleForm.getMarcaId());
        }
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
    public Page<Vehicle> getAll(Pageable pageable, SearchVehicleForm searchVehicleForm) {
        CriteriaQuery<VehicleJpa> vehicleJpaCriteriaQuery = this.entityManager.getCriteriaBuilder().createQuery(VehicleJpa.class);
        Root<VehicleJpa> vehicleJpaRoot = vehicleJpaCriteriaQuery.from(VehicleJpa.class);
        this.createFilterQuery(vehicleJpaCriteriaQuery, vehicleJpaRoot, searchVehicleForm);

        Query query = this.entityManager.createQuery(vehicleJpaCriteriaQuery);

        List<Vehicle> vehicleList;
        if(pageable.isPaged()){
            vehicleList = this.getVehiclePage(query, pageable);
        }else{
            vehicleList = this.getAllVehicles(query);
        }

        Long countVehicles = this.countVehicles(searchVehicleForm);
        return new PageImpl<>(
                vehicleList,
                pageable,
                countVehicles
        );
    }

    @Override
    public void deleteVehicle(Long id) throws NotFoundException {
        try {
            VehicleJpa vehicleJpa = this.entityManager.getReference(VehicleJpa.class,id);
            this.entityManager.remove(vehicleJpa);
        }catch (EntityNotFoundException exception){
            throw new NotFoundException("Vehicle not found");
        }

    }
}
