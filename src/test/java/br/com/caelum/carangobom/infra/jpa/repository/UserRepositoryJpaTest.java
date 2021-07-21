package br.com.caelum.carangobom.infra.jpa.repository;

import br.com.caelum.carangobom.domain.entity.User;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.repository.UserRepository;
import br.com.caelum.carangobom.infra.controller.request.CreateUserRequest;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryJpaTest {

    @Autowired
    UserRepository userRepositoryJpa;

    @BeforeEach
    void setup() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("standard");
        request.setPassword("123456");

        userRepositoryJpa.save(request.toUser());

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("admin");
        request2.setPassword("123456");

        userRepositoryJpa.save(request2.toUser());
    }

    @Test
    void shouldCreateNewUser() {
        assertEquals(2, userRepositoryJpa.findAll().size());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("standard2");
        request.setPassword("123456");

        User response = userRepositoryJpa.save(request.toUser());

        assertNotNull(response);
        assertEquals(request.getUsername(), response.getUsername());

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setUsername("admin2");
        request2.setPassword("123456");

        User response2 = userRepositoryJpa.save(request2.toUser());

        assertNotNull(response2);
        assertEquals(request2.getUsername(), response2.getUsername());

        assertEquals(4, userRepositoryJpa.findAll().size());
    }

    @Test
    void shouldDeleteExistingUser() {
        assertDoesNotThrow(() -> {
            userRepositoryJpa.delete(userRepositoryJpa.findAll().get(0).getId());

            assertEquals(1, userRepositoryJpa.findAll().size());

            userRepositoryJpa.delete(userRepositoryJpa.findAll().get(0).getId());

            assertEquals(0, userRepositoryJpa.findAll().size());
        });
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAttemptToDeleteNonExistingUser() {
        assertThrows(NotFoundException.class, () -> {
            userRepositoryJpa.delete(100L);
        });
    }

    @Test
    void shouldReturnDetailedUsersInformation() {
        assertDoesNotThrow(() -> {
            User requestedUser = userRepositoryJpa.findAll().get(0);
            Optional<User> userResponse = userRepositoryJpa.findById(requestedUser.getId());
            assertNotNull(userResponse);
            assertTrue(userResponse.isPresent());
            assertEquals(requestedUser.getId(), userResponse.get().getId());
            assertEquals(requestedUser.getUsername(), userResponse.get().getUsername());
        });

        assertDoesNotThrow(() -> {
            User requestedUser = userRepositoryJpa.findAll().get(1);
            Optional<User> userResponse = userRepositoryJpa.findById(requestedUser.getId());
            assertNotNull(userResponse);
            assertTrue(userResponse.isPresent());
            assertEquals(requestedUser.getId(), userResponse.get().getId());
            assertEquals(requestedUser.getUsername(), userResponse.get().getUsername());

        });
    }

    @Test
    void shouldReturnEmptyObjectWhenRequestedNonExistingUserById() {
        assertFalse(userRepositoryJpa.findById(100L).isPresent());
    }

    @Test
    void shouldReturnUserByItsUsername() {
        Optional<User> userOptional = userRepositoryJpa.findByUsername("standard");
        assertNotNull(userOptional);
        assertTrue(userOptional.isPresent());
        assertEquals("standard", userOptional.get().getUsername());
    }

    @Test
    void shouldReturnEmptyObjectWhenRequestedNonExistingUserByUsername() {
        Optional<User> userOptional = userRepositoryJpa.findByUsername("incorrect");
        assertNotNull(userOptional);
        assertFalse(userOptional.isPresent());
    }
}
