package br.com.caelum.carangobom.infra.jpa.repository;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.PageableDummy;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import br.com.caelum.carangobom.infra.jpa.entity.MarcaJpa;
import br.com.caelum.carangobom.infra.jpa.entity.VehicleJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles(profiles = {"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VehicleRepositoryJpaTest {

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearDatabase(){
        this.entityManager.createQuery("delete from vehicle");
        this.entityManager.createQuery("delete from marca");
    }

    VehicleRepositoryJpa setup(){
        return new VehicleRepositoryJpa(this.entityManager);
    }

    MarcaJpa createMarca(MarcaJpa marca){
        this.entityManager.persist(marca);
        return marca;
    }

    VehicleJpa createVehicle(VehicleJpa vehicleJpa){
        this.entityManager.persist(vehicleJpa);
        return vehicleJpa;
    }

    @Test
    void shouldSaveAnVehicle(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        String model = "audi r8";
        int year = 2000;
        double price = 20000;
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa(null,"Audi"));
        VehicleJpa vehicleJpa = new VehicleJpa(null,model,year,price,marcaJpa);
        Vehicle savedVehicle = vehicleRepositoryJpa.save(vehicleJpa);
        assertNotNull(savedVehicle.getId());
        assertEquals(savedVehicle.getModel(), model);
        assertEquals(savedVehicle.getPrice(), price);
        assertEquals(savedVehicle.getYear(), year);
        assertEquals(savedVehicle.getMarca().getId(), marcaJpa.getId());
    }

    @Test
    void shouldSaveAnVehicleThatAlreadyExists(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        String model = "audi r8";
        int year = 2000;
        double price = 20000;
        double newPrice = 44444;
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa(null,"Audi"));
        VehicleJpa vehicleJpa = this.createVehicle(new VehicleJpa(null,model,year,price,marcaJpa));
        vehicleJpa.setPrice(newPrice);
        Vehicle savedVehicle = vehicleRepositoryJpa.save(vehicleJpa);
        assertEquals(vehicleJpa.getId(), savedVehicle.getId());
        assertEquals(savedVehicle.getModel(), model);
        assertEquals(savedVehicle.getPrice(), newPrice);
        assertEquals(savedVehicle.getYear(), year);
        assertEquals(savedVehicle.getMarca().getId(), marcaJpa.getId());
    }

    @Test
    void shouldThrowAnErrorWhenMarcaDontExistsOnSaveVehicle(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        String model = "audi r8";
        int year = 2000;
        double price = 20000;
        MarcaJpa marcaJpa = new MarcaJpa(400L,"Audi");
        VehicleJpa vehicleJpa = new VehicleJpa(null,model,year,price,marcaJpa);
        assertThrows(PersistenceException.class,()->vehicleRepositoryJpa.save(vehicleJpa));
    }

    @Test
    void shouldReturnAVehicleWhenFindById(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        String model = "audi r8";
        int year = 2000;
        double price = 20000;
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa(null,"Audi"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null, model, year, price, marcaJpa));
        Optional<Vehicle> optionalVehicle = vehicleRepositoryJpa.findById(vehicleJpa.getId());
        assertTrue(optionalVehicle.isPresent());
        assertNotNull(optionalVehicle.get().getId());
        assertEquals(model, optionalVehicle.get().getModel());
        assertEquals(price, optionalVehicle.get().getPrice());
        assertEquals(year, optionalVehicle.get().getYear());
        assertEquals(marcaJpa.getId(), optionalVehicle.get().getMarca().getId());
    }

    @Test
    void shouldReturnOptionalNullWhenNotFindById(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        Long vehicleId = 100L;
        Optional<Vehicle> optionalVehicle = vehicleRepositoryJpa.findById(vehicleId);
        assertFalse(optionalVehicle.isPresent());
    }

    @Test
    void shouldReturnAllVehicles(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        MarcaJpa marcaJpa = createMarca(new MarcaJpa(null,"Audi"));
        List<Vehicle> vehicles = Arrays.asList(
            createVehicle(new VehicleJpa(null, "Audi A", 2010, 10000.0, marcaJpa)),
            createVehicle(new VehicleJpa(null, "Audi B", 2011, 20000.0, marcaJpa)),
            createVehicle(new VehicleJpa(null, "Audi C", 2012, 30000.0, marcaJpa)),
            createVehicle(new VehicleJpa(null, "Audi D", 2013, 40000.0, marcaJpa)),
            createVehicle(new VehicleJpa(null, "Audi E", 2014, 50000.0, marcaJpa)),
            createVehicle(new VehicleJpa(null, "Audi F", 2016, 60000.0, marcaJpa))
        );
        Page<Vehicle> vehiclePage =  vehicleRepositoryJpa.getAll(Pageable.unpaged(),null);
        assertEquals(6, vehiclePage.getTotalElements());
        assertEquals(1, vehiclePage.getTotalPages());
        assertEquals(6, vehiclePage.getContent().size());
        for (int i = 0; i < vehiclePage.getSize(); i++) {
            assertEquals(vehicles.get(i).getId(), vehiclePage.getContent().get(i).getId());
            assertEquals(vehicles.get(i).getModel(), vehiclePage.getContent().get(i).getModel());
            assertEquals(vehicles.get(i).getMarca().getId(), vehiclePage.getContent().get(i).getMarca().getId());
            assertEquals(vehicles.get(i).getYear(), vehiclePage.getContent().get(i).getYear());
            assertEquals(vehicles.get(i).getPrice(), vehiclePage.getContent().get(i).getPrice());
        }
    }

    @Test
    void shouldReturnVehiclesFilteredByMarcaId(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        MarcaJpa marcaJpa1 = createMarca(new MarcaJpa(null,"Audi"));
        MarcaJpa marcaJpa2 = createMarca(new MarcaJpa(null,"Ford"));
        List<Vehicle> vehicles = Arrays.asList(
                createVehicle(new VehicleJpa(null, "Audi A", 2010, 10000.0, marcaJpa1)),
                createVehicle(new VehicleJpa(null, "Audi B", 2011, 20000.0, marcaJpa1)),
                createVehicle(new VehicleJpa(null, "Audi C", 2012, 30000.0, marcaJpa1)),
                createVehicle(new VehicleJpa(null, "Ford D", 2013, 40000.0, marcaJpa2)),
                createVehicle(new VehicleJpa(null, "Ford E", 2014, 50000.0, marcaJpa2)),
                createVehicle(new VehicleJpa(null, "Ford F", 2016, 60000.0, marcaJpa2))
        );
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(marcaJpa2.getId());
        Page<Vehicle> vehiclePage =  vehicleRepositoryJpa.getAll(Pageable.unpaged(),searchVehicleForm);
        assertEquals(3, vehiclePage.getTotalElements());
        assertEquals(1, vehiclePage.getTotalPages());
        assertEquals(3, vehiclePage.getContent().size());
        List<Vehicle> expectedFilteredVehicles = vehicles.subList(3,6);
        List<Vehicle> actualVehicles = vehiclePage.getContent();
        for (int i = 0; i < vehiclePage.getSize(); i++) {
            assertEquals(expectedFilteredVehicles.get(i).getId(), actualVehicles.get(i).getId());
            assertEquals(expectedFilteredVehicles.get(i).getModel(), actualVehicles.get(i).getModel());
            assertEquals(expectedFilteredVehicles.get(i).getMarca().getId(), actualVehicles.get(i).getMarca().getId());
            assertEquals(expectedFilteredVehicles.get(i).getYear(), actualVehicles.get(i).getYear());
            assertEquals(expectedFilteredVehicles.get(i).getPrice(), actualVehicles.get(i).getPrice());
        }
    }

    @Test
    void shouldReturnAllVehiclesPaginated(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        MarcaJpa marcaJpa = createMarca(new MarcaJpa(null,"Audi"));
        List<Vehicle> vehicles = Arrays.asList(
                createVehicle(new VehicleJpa(null, "Audi A", 2010, 10000.0, marcaJpa)),
                createVehicle(new VehicleJpa(null, "Audi B", 2011, 20000.0, marcaJpa)),
                createVehicle(new VehicleJpa(null, "Audi C", 2012, 30000.0, marcaJpa)),
                createVehicle(new VehicleJpa(null, "Audi D", 2013, 40000.0, marcaJpa)),
                createVehicle(new VehicleJpa(null, "Audi E", 2014, 50000.0, marcaJpa)),
                createVehicle(new VehicleJpa(null, "Audi F", 2016, 60000.0, marcaJpa))
        );

        Page<Vehicle> vehiclePage =  vehicleRepositoryJpa.getAll(new PageableDummy(0,3,null),null);
        assertEquals(6, vehiclePage.getTotalElements());
        assertEquals(2, vehiclePage.getTotalPages());
        assertEquals(3, vehiclePage.toList().size());
        for (int i = 0; i < vehiclePage.getSize(); i++) {
            assertEquals(vehicles.get(i).getId(), vehiclePage.getContent().get(i).getId());
            assertEquals(vehicles.get(i).getModel(), vehiclePage.getContent().get(i).getModel());
            assertEquals(vehicles.get(i).getMarca().getId(), vehiclePage.getContent().get(i).getMarca().getId());
            assertEquals(vehicles.get(i).getYear(), vehiclePage.getContent().get(i).getYear());
            assertEquals(vehicles.get(i).getPrice(), vehiclePage.getContent().get(i).getPrice());
        }
    }

    @Test
    void shouldDeleteAVehicle(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        String model = "audi r8";
        int year = 2000;
        double price = 20000;
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa(null,"Audi"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null, model, year, price, marcaJpa));
        assertDoesNotThrow(()->{
            vehicleRepositoryJpa.deleteVehicle(vehicleJpa.getId());
        });
        Vehicle deletedVehicle = this.entityManager.find(VehicleJpa.class, vehicleJpa.getId());
        assertNull(deletedVehicle);
    }

    @Test
    void shouldThrowNotFoundonDeleteAVehicleWhenVehicleDoesNotExists(){
        VehicleRepositoryJpa vehicleRepositoryJpa = setup();
        Long id = 404L;
        assertThrows(NotFoundException.class, ()->{
            vehicleRepositoryJpa.deleteVehicle(id);
        });
    }
}
