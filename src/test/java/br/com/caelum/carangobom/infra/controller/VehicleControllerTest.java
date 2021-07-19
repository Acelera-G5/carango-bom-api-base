package br.com.caelum.carangobom.infra.controller;

import br.com.caelum.carangobom.domain.entity.Vehicle;
import br.com.caelum.carangobom.infra.controller.request.CreateVehicleRequest;
import br.com.caelum.carangobom.infra.jpa.entity.MarcaJpa;
import br.com.caelum.carangobom.infra.jpa.entity.VehicleJpa;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class VehicleControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    private MarcaJpa createMarca(MarcaJpa marcaJpa){
        entityManager.persist(marcaJpa);
        return marcaJpa;
    }

    private VehicleJpa createVehicle(VehicleJpa vehicleJpa){
        entityManager.persist(vehicleJpa);
        return vehicleJpa;
    }

    @Test
    void shouldCreateAnVehicle() throws Exception {
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Audi"));
        String model = "Audi";
        double price = 200000;
        int year = 2010;
        Long marcaId = marcaJpa.getId();
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year,marcaId);
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/vehicle")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.model").value(model))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(price))
                .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(year))
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.id").value(marcaId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.nome").value(marcaJpa.getNome()))
                .andReturn();
    }

    @Test
    void shouldReturn404BecauseTheMarcaDoesntExists() throws Exception {
        String model = "Audi";
        double price = 200000;
        int year = 2010;
        Long marcaId =100L;
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year,marcaId);
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/vehicle")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturnTheErrorsWhenTheRequestIsInvalidOnCreateVehicle() throws Exception{
        String model = "";
        double price = 0;
        int year = -30;
        Long marcaId = -20L;
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year,marcaId);
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/vehicle")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    void souldUpdateTheVehicle() throws Exception {
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        MarcaJpa newMarcaJpa = this.createMarca(new MarcaJpa("Audi"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa));

        String model = "Audi";
        double price = 200000;
        int year = 2010;
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year, newMarcaJpa.getId());

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/vehicle/{id}",vehicleJpa.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(vehicleJpa.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.model").value(model))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(price))
                .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(year))
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.id").value(newMarcaJpa.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.nome").value(newMarcaJpa.getNome()))
                .andReturn();
    }

    @Test
    void shouldReturn404OnUpdateVehicleWhenVehicleDoesntExist() throws Exception{
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        String model = "Audi";
        double price = 200000;
        int year = 2010;
        Long vehicleId = 1000L;
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year, marcaJpa.getId());

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/vehicle/{id}",vehicleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void shouldReturn404OnUpdateVehicleWhenMarcaDoesntExist() throws Exception{
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa));
        String model = "Audi";
        double price = 200000;
        int year = 2010;
        Long fakeMarca = 11L;
        CreateVehicleRequest createVehicleRequest = new CreateVehicleRequest(model,price,year,fakeMarca);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/vehicle/{id}",vehicleJpa.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createVehicleRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void shouldReturnAllVehicles() throws Exception{
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        List<VehicleJpa> vehicleJpaList = Arrays.asList(
                createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford L",2003,25000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford M",2004,35000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford N",2006,45000.0,marcaJpa))
        );

        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true));
        for (int i = 0; i < vehicleJpaList.size(); i++) {
            resultActions
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.id").value(vehicleJpaList.get(i).getMarca().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.nome").value(vehicleJpaList.get(i).getMarca().getNome()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].id").value(vehicleJpaList.get(i).getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].model").value(vehicleJpaList.get(i).getModel()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].price").value(vehicleJpaList.get(i).getPrice()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].year").value(vehicleJpaList.get(i).getYear()));
        }
    }

    @Test
    void shouldReturnAllVehiclesPaginated() throws Exception{
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        List<VehicleJpa> vehicleJpaList = Arrays.asList(
                createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford L",2003,25000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford M",2004,35000.0,marcaJpa)),
                createVehicle(new VehicleJpa(null,"Ford N",2006,45000.0,marcaJpa))
        );

        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.first").value(true));
        for (int i = 0; i < 2; i++) {
            resultActions
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.id").value(vehicleJpaList.get(i).getMarca().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.nome").value(vehicleJpaList.get(i).getMarca().getNome()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].id").value(vehicleJpaList.get(i).getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].model").value(vehicleJpaList.get(i).getModel()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].price").value(vehicleJpaList.get(i).getPrice()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].year").value(vehicleJpaList.get(i).getYear()));
        }
    }

    @Test
    void shouldReturnVehiclesFilteredByMarcaId() throws Exception {
        MarcaJpa marcaJpa1 = this.createMarca(new MarcaJpa("Ford"));
        MarcaJpa marcaJpa2 = this.createMarca(new MarcaJpa("Audi"));
        List<VehicleJpa> vehicleJpaList = Arrays.asList(
                createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Ford L",2003,25000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Audi K",2004,35000.0,marcaJpa2)),
                createVehicle(new VehicleJpa(null,"Audi L",2006,45000.0,marcaJpa2))
        );

        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                                .queryParam("marcaId",marcaJpa2.getId().toString())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.first").value(true));
        List<VehicleJpa> subList = vehicleJpaList.subList(2,4);
        for (int i = 0; i < 2; i++) {
            resultActions
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.id").value(subList.get(i).getMarca().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.nome").value(subList.get(i).getMarca().getNome()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].id").value(subList.get(i).getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].model").value(subList.get(i).getModel()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].price").value(subList.get(i).getPrice()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].year").value(subList.get(i).getYear()));
        }
    }

    @Test
    void shouldReturnVehiclesFilteredByYear() throws Exception {
        MarcaJpa marcaJpa1 = this.createMarca(new MarcaJpa("Ford"));
        List<VehicleJpa> vehicleJpaList = Arrays.asList(
                createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Ford L",2003,25000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Audi K",2020,35000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Audi L",2020,45000.0,marcaJpa1))
        );

        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                                .queryParam("year","2020")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.first").value(true));
        List<VehicleJpa> subList = vehicleJpaList.subList(2,4);
        for (int i = 0; i < 2; i++) {
            resultActions
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.id").value(subList.get(i).getMarca().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.nome").value(subList.get(i).getMarca().getNome()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].id").value(subList.get(i).getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].model").value(subList.get(i).getModel()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].price").value(subList.get(i).getPrice()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].year").value(subList.get(i).getYear()));
        }
    }

    @Test
    void shouldReturnVehiclesFilteredByModel() throws Exception {
        MarcaJpa marcaJpa1 = this.createMarca(new MarcaJpa("Ford"));
        List<VehicleJpa> vehicleJpaList = Arrays.asList(
                createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Ford L",2003,25000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Audi K",2020,35000.0,marcaJpa1)),
                createVehicle(new VehicleJpa(null,"Audi L",2020,45000.0,marcaJpa1))
        );

        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                                .queryParam("model","Audi")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.empty").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.first").value(true));
        List<VehicleJpa> subList = vehicleJpaList.subList(2,4);
        for (int i = 0; i < 2; i++) {
            resultActions
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.id").value(subList.get(i).getMarca().getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].marca.nome").value(subList.get(i).getMarca().getNome()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].id").value(subList.get(i).getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].model").value(subList.get(i).getModel()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].price").value(subList.get(i).getPrice()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content["+i+"].year").value(subList.get(i).getYear()));
        }
    }

    @Test
    void shouldReturnAVehilceUsingId() throws Exception {
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa));
        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/{id}", vehicleJpa.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.id").value(vehicleJpa.getMarca().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.marca.nome").value(vehicleJpa.getMarca().getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(vehicleJpa.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.model").value(vehicleJpa.getModel()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(vehicleJpa.getPrice()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.year").value(vehicleJpa.getYear()));
    }

    @Test
    void shouldReturn404OnFindByIdWhenVehicleDoesntExist() throws Exception {
        Long id = 404L;
        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/vehicle/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void shouldDeleteAVehicleUsingId() throws Exception {
        MarcaJpa marcaJpa = this.createMarca(new MarcaJpa("Ford"));
        VehicleJpa vehicleJpa = createVehicle(new VehicleJpa(null,"Ford k",2002,15000.0,marcaJpa));
        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .delete("/vehicle/{id}", vehicleJpa.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void shouldReturn404OnDeleteWhenVehicleDoesntExist() throws Exception {
        Long id = 404L;
        ResultActions resultActions = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .delete("/vehicle/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .queryParam("page","0")
                                .queryParam("size","2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }



}
