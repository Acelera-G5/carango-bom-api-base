package br.com.caelum.carangobom.infra.controller;

import br.com.caelum.carangobom.infra.controller.request.AuthenticationRequest;
import br.com.caelum.carangobom.infra.controller.request.CreateUserRequest;
import br.com.caelum.carangobom.infra.controller.response.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController controller;

    @BeforeEach
    void setup() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        controller.createUser(request, UriComponentsBuilder.newInstance());
    }

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("unauthorized");
        request.setPassword("123456");

        mockMvc.perform(
                post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isForbidden());

        mockMvc.perform(delete("/users/1")).andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnStatusOkWhenAttemptToAccessGrantedEndpoints() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusOkWhenAuthenticated() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("admin", "123456");
        mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isOk()).andDo(print());
    }

    @Test
    void shouldReturnBadRequestWhenBadCredentials() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("wrong", "123456");
        mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isBadRequest()).andDo(print());
    }

    @Test
    void shouldCreateAnUserWhenAuthenticated() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("admin", "123456");
        String response = mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        AuthenticationResponse decoded = mapper.readValue(response, AuthenticationResponse.class);

        CreateUserRequest requestUser = new CreateUserRequest();
        requestUser.setUsername("standard");
        requestUser.setPassword("123456");

        mockMvc.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUser))
                        .header("Authorization", decoded.getType() + " " + decoded.getToken())
        ).andDo(print()).andExpect(status().isCreated());
    }

    @Test
    void shouldAcceptAllOriginsAndMethods() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(header().stringValues("Access-Control-Allow-Origin", "*"))
                .andExpect(header().stringValues("Access-Control-Allow-Methods", "*"));
    }
}
