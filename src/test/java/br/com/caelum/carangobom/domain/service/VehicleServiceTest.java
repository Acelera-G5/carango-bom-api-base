package br.com.caelum.carangobom.domain.service;

import br.com.caelum.carangobom.domain.entity.Marca;
import br.com.caelum.carangobom.domain.entity.MarcaDummy;
import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.form.PageableDummy;
import br.com.caelum.carangobom.domain.entity.form.SearchVehicleForm;
import br.com.caelum.carangobom.domain.entity.form.VehicleForm;
import br.com.caelum.carangobom.domain.repository.MarcaRepository;
import br.com.caelum.carangobom.domain.repository.MarcaRepositoryMock;
import br.com.caelum.carangobom.domain.repository.VehicleRepository;
import br.com.caelum.carangobom.domain.repository.VehicleRepositoryMock;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VehicleServiceTest {

    private MarcaRepository marcaRepository = new MarcaRepositoryMock();
    private VehicleRepository vehicleRepository= new VehicleRepositoryMock();

    VehicleService setup(){
        return new VehicleService(this.marcaRepository, this.vehicleRepository);
    }

    VehicleForm createVehicle(String model, int year, double price, Long marcaId){
        return new VehicleForm(null,model,price,year,null, marcaId);
    }

    Marca createMarca(Marca marca){
        return this.marcaRepository.save(marca);
    }

    @Test
    void shouldCreateAVehicle() throws NotFoundException {
        String model = "Audi r8";
        int year = 1997;
        double price = 2000;
        VehicleService vehicleService = setup();
        Marca marca =  createMarca(new MarcaDummy(1L,"Audi"));
        VehicleForm vehicle = createVehicle(model, year, price, marca.getId());
        Vehicle savedVehicle = vehicleService.createVehicle(vehicle);
        assertEquals(1L, savedVehicle.getId());
        assertEquals(savedVehicle.getModel(), model);
        assertEquals(savedVehicle.getPrice(), price);
        assertEquals(savedVehicle.getYear(), year);
        assertEquals(savedVehicle.getMarca().getId(), marca.getId());
    }

    @Test
    void shouldReturnAErrorNotFoundAMarcaWhenCreatingVehicle(){
        String model = "Audi r8";
        int year = 1997;
        double price = 2000;
        Long marcaId = 404L;
        VehicleService vehicleService = setup();
        VehicleForm vehicle = createVehicle(model, year, price, marcaId);
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class,()->vehicleService.createVehicle(vehicle)
        );
        assertEquals("Marca not found", notFoundException.getMessage());
    }

    @Test
    void shouldReturnAnUpddateVehicle() throws NotFoundException {
        Marca marca =  createMarca(new MarcaDummy(1L,"Audi"));
        Vehicle savedVehicle = this.vehicleRepository.save(createVehicle("Audi",2010,1000, marca.getId()));
        String model = "Ford k";
        int year = 2016;
        double price = 3200;
        VehicleService vehicleService = setup();
        VehicleForm vehicle = createVehicle(model, year, price, marca.getId());
        Vehicle updatedVehicle = vehicleService.updateVehicle(vehicle, savedVehicle.getId());
        assertEquals(marca.getId(), updatedVehicle.getMarca().getId());
        assertEquals(year, updatedVehicle.getYear());
        assertEquals(price, updatedVehicle.getPrice());
        assertEquals(model, updatedVehicle.getModel());
    }

    @Test
    void shouldReturnAnErrorOnUpdateAVehicleWhenTheMarcaDoesntExists() throws NotFoundException {
        Long marcaId = 200L;
        Vehicle savedVehicle = this.vehicleRepository.save(createVehicle("Audi",2010,1000, marcaId));
        String model = "Ford k";
        int year = 2016;
        double price = 3200;
        VehicleService vehicleService = setup();
        VehicleForm vehicle = createVehicle(model, year, price, marcaId);
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class,
                ()->vehicleService.updateVehicle(vehicle,savedVehicle.getId())
        );
        assertEquals("Marca not found",notFoundException.getMessage());
    }

    @Test
    void shouldReturnAnErrorOnUpdateAVehicleWhenTheVehicleDoesntExists() throws NotFoundException {
        Marca marca =  createMarca(new MarcaDummy(1L,"Audi"));
        Long vehicleId = 200L;
        String model = "Ford k";
        int year = 2016;
        double price = 3200;
        VehicleService vehicleService = setup();
        VehicleForm vehicle = createVehicle(model, year, price, marca.getId());
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class,
                ()->vehicleService.updateVehicle(vehicle, vehicleId)
        );
        assertEquals("Vehicle not found",notFoundException.getMessage());
    }

    @Test
    void shouldReturnAListWithTheVehicles(){
        Marca marca = createMarca(new MarcaDummy(1L, "Audi"));
        List<VehicleForm> vehicles = Arrays.asList(
                createVehicle("Audi A", 2010, 1000.0, marca.getId()),
                createVehicle("Audi B", 2011, 2000.0, marca.getId()),
                createVehicle("Audi C", 2012, 3000.0, marca.getId()),
                createVehicle("Audi D", 2013, 4000.0, marca.getId()),
                createVehicle("Audi E", 2014, 5000.0, marca.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),null);
        assertEquals(vehicles,vehicleList.toList());
    }

    @Test
    void shouldReturnAPaginatedListWithTheVehicles(){
        Marca marca = createMarca(new MarcaDummy(1L, "Audi"));
        List<VehicleForm> vehicles = Arrays.asList(
                createVehicle("Audi A", 2010, 1000.0, marca.getId()),
                createVehicle("Audi B", 2011, 2000.0, marca.getId()),
                createVehicle("Audi C", 2012, 3000.0, marca.getId()),
                createVehicle("Audi D", 2013, 4000.0, marca.getId()),
                createVehicle("Audi E", 2014, 5000.0, marca.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        PageableDummy pageableDummy = new PageableDummy(0,3,null);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(pageableDummy,null);
        assertEquals(5,vehicleList.getTotalElements());
        assertEquals(3,vehicleList.toList().size());
        assertEquals(2, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(0,3),vehicleList.toList());
    }

    @Test
    void shouldReturnAPaginatedListWithTheVehiclesFilteredByMarcaId(){
        Marca marca1 = createMarca(new MarcaDummy(1L, "Audi"));
        Marca marca2 = createMarca(new MarcaDummy(2L, "Ford"));
        List<VehicleForm> vehicles = Arrays.asList(
                new VehicleForm(null,"Audi A",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi B",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi C",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Ford A",100000.0,2010, marca2, marca2.getId()),
                new VehicleForm(null,"Ford B",200000.0,2010, marca2, marca2.getId()),
                new VehicleForm(null,"Ford C",300000.0,2010, marca2, marca2.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(marca2.getId(),null, null, null,null);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),searchVehicleForm);
        assertEquals(3,vehicleList.getTotalElements());
        assertEquals(3,vehicleList.getContent().size());
        assertEquals(1, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(3,6),vehicleList.toList());
    }

    @Test
    void shouldReturnAPaginatedListWithTheVehiclesFilteredByYear(){
        Marca marca1 = createMarca(new MarcaDummy(1L, "Audi"));
        List<VehicleForm> vehicles = Arrays.asList(
                new VehicleForm(null,"Audi A",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi B",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi C",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi D",100000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Audi E",200000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Audi F",300000.0,2020, marca1, marca1.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(null, 2020,null, null, null);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),searchVehicleForm);
        assertEquals(3,vehicleList.getTotalElements());
        assertEquals(3,vehicleList.getContent().size());
        assertEquals(1, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(3,6),vehicleList.toList());
    }

    @Test
    void shouldReturnAPaginatedListWithTheVehiclesFilteredByModel(){
        Marca marca1 = createMarca(new MarcaDummy(1L, "Audi"));
        String model = "Audi";
        List<VehicleForm> vehicles = Arrays.asList(
                new VehicleForm(null,"Audi A",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi B",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi C",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Ford D",100000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford E",200000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford F",300000.0,2020, marca1, marca1.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(null, null, model, null, null);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),searchVehicleForm);
        assertEquals(3,vehicleList.getTotalElements());
        assertEquals(3,vehicleList.getContent().size());
        assertEquals(1, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(0,3),vehicleList.toList());
    }

    @Test
    void souldReturnAPaginatedListWithTheVehiclesFilteredByPriceGreaterThanOrEqual(){
        double priceMin = 200000.0;
        Marca marca1 = createMarca(new MarcaDummy(1L, "Audi"));
        List<VehicleForm> vehicles = Arrays.asList(
                new VehicleForm(null,"Audi A",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi B",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi C",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Ford D",100000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford E",200000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford F",300000.0,2020, marca1, marca1.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(null, null, null, priceMin, null);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),searchVehicleForm);
        assertEquals(2,vehicleList.getTotalElements());
        assertEquals(2,vehicleList.getContent().size());
        assertEquals(1, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(4,6),vehicleList.toList());
    }

    @Test
    void souldReturnAPaginatedListWithTheVehiclesFilteredByPriceLessThanOrEqual(){
        double priceMax = 190000.0;
        Marca marca1 = createMarca(new MarcaDummy(1L, "Audi"));
        List<VehicleForm> vehicles = Arrays.asList(
                new VehicleForm(null,"Audi A",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi B",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Audi C",100000.0,2010, marca1, marca1.getId()),
                new VehicleForm(null,"Ford D",100000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford E",200000.0,2020, marca1, marca1.getId()),
                new VehicleForm(null,"Ford F",300000.0,2020, marca1, marca1.getId())
        );
        vehicles.forEach((vehicleForm)->this.vehicleRepository.save(vehicleForm));
        VehicleService vehicleService = setup();
        SearchVehicleForm searchVehicleForm = new SearchVehicleForm(null, null, null, null, priceMax);
        Page<Vehicle> vehicleList = vehicleService.listVehicle(Pageable.unpaged(),searchVehicleForm);
        assertEquals(4,vehicleList.getTotalElements());
        assertEquals(4,vehicleList.getContent().size());
        assertEquals(1, vehicleList.getTotalPages());
        assertEquals(vehicles.subList(0,4),vehicleList.toList());
    }

    @Test
    void shouldGetAVehicleById() throws NotFoundException {
        Marca marca = createMarca(new MarcaDummy(null, "Audi"));
        Vehicle vehicle = this.vehicleRepository.save(
                new VehicleForm(null,"Audi R8",1000000.0,2021,marca,marca.getId())
        );
        VehicleService vehicleService = setup();
        Vehicle foundVehicle = vehicleService.getVehicleById(vehicle.getId());
        assertEquals(vehicle.getPrice(), foundVehicle.getPrice());
        assertEquals(vehicle.getYear(), foundVehicle.getYear());
        assertEquals(vehicle.getModel(), foundVehicle.getModel());
        assertEquals(vehicle.getMarca().getId(), foundVehicle.getMarca().getId());
    }

    @Test
    void shouldThrowNotFoundWhenFindVehicleById(){
        Long id = 1000L;
        VehicleService vehicleService = setup();
        assertThrows(NotFoundException.class,()->{
            Vehicle foundVehicle = vehicleService.getVehicleById(id);
        });
    }

    @Test
    void shouldDeleteAVehicleById() throws NotFoundException {
        Marca marca = createMarca(new MarcaDummy(null, "Audi"));
        Vehicle vehicle = this.vehicleRepository.save(
                new VehicleForm(null,"Audi R8",1000000.0,2021,marca,marca.getId())
        );
        VehicleService vehicleService = setup();
        vehicleService.deleteVehicleById(vehicle.getId());
        assertEquals(0,this.vehicleRepository.getAll(Pageable.unpaged(),null).getContent().size());
    }

    @Test
    void shouldThrowNotFoundOnDeleteAVehicleByIdWhenVehicleDoesNotExists(){
        Long id = 404L;
        VehicleService vehicleService = setup();
        assertThrows(NotFoundException.class,()->{
            vehicleService.deleteVehicleById(id);
        });
    }

}
