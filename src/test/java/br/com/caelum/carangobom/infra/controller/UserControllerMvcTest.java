package br.com.caelum.carangobom.infra.controller;

import br.com.caelum.carangobom.infra.controller.request.AuthenticationRequest;
import br.com.caelum.carangobom.infra.controller.request.CreateUserRequest;
import br.com.caelum.carangobom.infra.controller.request.UpdatePasswordRequest;
import br.com.caelum.carangobom.infra.controller.response.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerMvcTest {

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
    @WithMockUser
    void shouldFailBeanValidationWhenInvalidPassword() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setPassword("");
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("password")));
    }

    @Test
    @WithMockUser
    void shouldFailBeanValidationWhenInvalidUsername() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("");
        request.setPassword("123456");
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("username")));
    }

    @Test
    @WithMockUser
    void shouldFailBeanValidationWhenAllFieldsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("");
        request.setPassword("");
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$..field", hasItems("username", "password")));
    }

    @Test
    void shouldUpdateUsersPassword() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("admin", "123456");
        String response = mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        AuthenticationResponse decoded = mapper.readValue(response, AuthenticationResponse.class);

        UpdatePasswordRequest requestUser = new UpdatePasswordRequest();
        requestUser.setOldPassword("123456");
        requestUser.setNewPassword("1234567");

        mockMvc.perform(
                put("/users/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUser))
                        .header("Authorization", decoded.getType() + " " + decoded.getToken())
        ).andDo(print()).andExpect(status().isOk());

        request = new AuthenticationRequest("admin", "123456");
        mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isBadRequest())
        .andDo(print());
    }

    @Test
    void shouldFailUpdateUsersPasswordWhenWrongOldPassword() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("admin", "123456");
        String response = mockMvc.perform(
                post("/auth").contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        AuthenticationResponse decoded = mapper.readValue(response, AuthenticationResponse.class);

        UpdatePasswordRequest requestUser = new UpdatePasswordRequest();
        requestUser.setOldPassword("wrong");
        requestUser.setNewPassword("123456");

        mockMvc.perform(
                put("/users/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUser))
                        .header("Authorization", decoded.getType() + " " + decoded.getToken())
        ).andDo(print()).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", is("The old password doesn't match")));
    }
}
