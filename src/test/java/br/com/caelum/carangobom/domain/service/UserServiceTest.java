package br.com.caelum.carangobom.domain.service;


import br.com.caelum.carangobom.domain.config.security.TokenServiceMock;
import br.com.caelum.carangobom.domain.entity.User;
import br.com.caelum.carangobom.domain.entity.UserDummy;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.entity.exception.PasswordMismatchException;
import br.com.caelum.carangobom.domain.entity.form.UpdatePasswordForm;
import br.com.caelum.carangobom.domain.repository.UserRepositoryMock;
import static org.junit.jupiter.api.Assertions.*;

import br.com.caelum.carangobom.infra.config.security.TokenService;
import br.com.caelum.carangobom.infra.controller.request.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

class UserServiceTest {

    private final UserRepositoryMock userRepository = new UserRepositoryMock();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService = new TokenServiceMock();
    private final UserService userService = new UserService(userRepository, tokenService, passwordEncoder);

    @Test
    void shouldFailBeanValidationWhenFieldsBlankOrNull() {
        UserDummy request = new UserDummy(1L, "", "123456");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserDummy>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        request.setUsername("usuario");
        request.setPassword("");

        violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        request.setUsername(null);
        request.setPassword("123456");

        violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        request.setUsername("usuario");
        request.setPassword(null);

        violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassBeanValidationWhenBothFieldsFilled() {
        UserDummy request = new UserDummy(1L, "usuario", "123456");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserDummy>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldReturnAllCreatedUsers() {
        assertEquals(0, userService.findAll().size());

        UserDummy request = new UserDummy(1L, "standard user", "123456");

        userService.create(request);

        assertEquals(1, userService.findAll().size());

        UserDummy request2 = new UserDummy(2L, "premium user", "123456");

        userService.create(request2);

        assertEquals(2, userService.findAll().size());
    }

    @Test
    void shouldReturnDetailedUser() {
        String password = "123456";
        UserDummy request = new UserDummy(1L, "standard user", password);
        userService.create(request);

        assertDoesNotThrow(() -> {
            User user = userService.findById(0L);
            assertNotNull(user);
            assertEquals(0L, user.getId());
            assertEquals("standard user", user.getUsername());
            assertNotEquals(user.getPassword(), password);
            assertTrue(passwordEncoder.matches(password, user.getPassword()));
        });
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRequestedNonExistingUser() {
        UserDummy request = new UserDummy(1L, "standard user", "123456");

        userService.create(request);

        assertThrows(NotFoundException.class, () -> {
            userService.findById(1L);
        });
    }

    @Test
    void shouldSuccessfullyDeleteSpecificUser() {
        assertEquals(0, userService.findAll().size());

        UserDummy request = new UserDummy(1L, "standard user", "standard user");

        userService.create(request);

        assertEquals(1, userService.findAll().size());

        assertDoesNotThrow(() -> {
            userService.delete(0L);
            assertEquals(0, userService.findAll().size());
        });
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAttemptToDeleteNonExistingUser() {
        assertEquals(0, userService.findAll().size());

        assertThrows(NotFoundException.class, () -> userService.delete(0L));

        UserDummy request = new UserDummy(1L, "standard user", "123456");

        userService.create(request);

        assertEquals(1, userService.findAll().size());
        assertDoesNotThrow(() -> {
            userService.delete(0L);
            assertEquals(0, userService.findAll().size());
        });
        assertEquals(0, userService.findAll().size());
        assertThrows(NotFoundException.class, () -> userService.delete(0L));
    }

    @Test
    void shouldUpdateExistingUser() {
        UserDummy request = new UserDummy(1L, "standard user", "123456");

        userService.create(request);
        assertDoesNotThrow(() -> {
            User user = userService.findById(0L);
            user.setUsername("different");

            userService.update(user);

            user = userService.findById(0L);
            assertEquals("different", user.getUsername());
        });
    }

    @Test
    void shouldUpdatePasswordOfExistingUser() {
        UserDummy request = new UserDummy(1L, "standard user", "123456");
        userService.create(request);

        UsernamePasswordAuthenticationToken auth =
                new AuthenticationRequest("standard user", "123456").parseUsernamePasswordAuthenticationToken();

        String token = tokenService.generateToken(auth);

        assertDoesNotThrow(() -> {
            userService.updatePassword(new UpdatePasswordForm("123456", "newPassword"), "Bearer " + token);
            passwordEncoder.matches("newPassword", request.getPassword());
        });
    }

    @Test
    void shouldFailOnAttemptToChangeUsersPasswordWhenWrongOldPassword() {
        UserDummy request = new UserDummy(1L, "standard user", "123456");
        userService.create(request);

        UsernamePasswordAuthenticationToken auth =
                new AuthenticationRequest("standard user", "123456").parseUsernamePasswordAuthenticationToken();

        String token = tokenService.generateToken(auth);

        assertThrows(PasswordMismatchException.class, () -> {
            userService.updatePassword(new UpdatePasswordForm("wrong", "newPassword"), "Bearer " + token);
        });
    }

    @Test
    void shouldFailOnAttemptToChangePasswordOnNonExistingUser() {
        UsernamePasswordAuthenticationToken auth =
                new AuthenticationRequest("standard user", "123456").parseUsernamePasswordAuthenticationToken();

        String token = tokenService.generateToken(auth);

        assertThrows(NotFoundException.class, () -> {
            userService.updatePassword(new UpdatePasswordForm("wrong", "newPassword"), "Bearer " + token);
        });
    }
}
