package br.com.caelum.carangobom.infra.controller;

import static org.junit.jupiter.api.Assertions.*;

import br.com.caelum.carangobom.infra.config.security.TokenService;
import br.com.caelum.carangobom.infra.controller.request.AuthenticationRequest;
import br.com.caelum.carangobom.infra.controller.request.CreateUserRequest;
import br.com.caelum.carangobom.infra.controller.request.UpdatePasswordRequest;
import br.com.caelum.carangobom.infra.controller.response.CreateUserResponse;
import br.com.caelum.carangobom.infra.controller.response.GetDetailedUserResponse;
import br.com.caelum.carangobom.infra.controller.response.GetUserResponse;
import java.util.List;
import java.util.Objects;

import br.com.caelum.carangobom.infra.controller.response.UpdatePasswordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private UserController userController;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setup() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("standard");
        request.setPassword("123456");

        userController.createUser(request, UriComponentsBuilder.newInstance());

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("admin");
        request2.setPassword("123456");

        userController.createUser(request2, UriComponentsBuilder.newInstance());
    }

    @Test
    void shouldCreateNewUsers() {
        assertEquals(2, userController.getUsers().size());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("standard2");
        request.setPassword("123456");

        ResponseEntity<CreateUserResponse> response = userController.createUser(request, UriComponentsBuilder.newInstance());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        CreateUserResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(request.getUsername(), responseBody.getUsername());

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("admin2");
        request2.setPassword("123456");

        ResponseEntity<CreateUserResponse> response2 = userController.createUser(request2, UriComponentsBuilder.newInstance());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        CreateUserResponse responseBody2 = response2.getBody();
        assertNotNull(responseBody2);
        assertEquals(request2.getUsername(), responseBody2.getUsername());

        assertEquals(4, userController.getUsers().size());
    }

    @Test
    void shouldDeleteAnUser() {
        List<GetUserResponse> users = userController.getUsers();

        ResponseEntity<Void> response = userController.deleteUser(users.get(0).getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        users = userController.getUsers();
        assertEquals(1, users.size());

        ResponseEntity<Void> response2 = userController.deleteUser(users.get(0).getId());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        users = userController.getUsers();
        assertEquals(0, users.size());
    }

    @Test
    void shouldFailWhenDeleteNonExistingUser() {
        ResponseEntity<Void> response = userController.deleteUser(100L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnDetailedUserInfo() {
        List<GetUserResponse> users = userController.getUsers();

        ResponseEntity<GetDetailedUserResponse> userResponse = userController.getDetailedUser(users.get(0).getId());
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());

        GetDetailedUserResponse response = userResponse.getBody();
        assertNotNull(response);
        assertEquals(users.get(0).getId(), response.getId());
        assertEquals(users.get(0).getUsername(), response.getUsername());

        ResponseEntity<GetDetailedUserResponse> userResponse2 = userController.getDetailedUser(users.get(1).getId());
        assertEquals(HttpStatus.OK, userResponse2.getStatusCode());

        GetDetailedUserResponse response2 = userResponse2.getBody();
        assertNotNull(response2);
        assertEquals(users.get(1).getId(), response2.getId());
        assertEquals(users.get(1).getUsername(), response2.getUsername());
    }

    @Test
    void shouldFailWhenRequestedNonExistingDetailedUser() {
        ResponseEntity<GetDetailedUserResponse> userResponse = userController.getDetailedUser(100L);
        assertEquals(HttpStatus.NOT_FOUND, userResponse.getStatusCode());
    }

    @Test
    void testUpdatePasswordSuccess() {
        AuthenticationRequest request = new AuthenticationRequest("admin", "123456");
        UsernamePasswordAuthenticationToken token = request.parseUsernamePasswordAuthenticationToken();
        String finalToken = tokenService.generateToken(authenticationManager.authenticate(token));

        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();
        updatePasswordRequest.setOldPassword("123456");
        updatePasswordRequest.setNewPassword("654321");

        ResponseEntity<UpdatePasswordResponse> response = userController.updatePassword(updatePasswordRequest, "Bearer " + finalToken);
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Password Successfully updated", Objects.requireNonNull(response.getBody()).getMessage());
    }
}
