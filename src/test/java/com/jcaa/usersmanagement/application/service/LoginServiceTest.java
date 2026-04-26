package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.service.dto.command.LoginCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("LoginService")
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  private static final String EMAIL = "john@example.com";
  private static final String PASSWORD = "SecurePass1";
  private static final String USER_ID = "u-001";
  private static final String USER_NAME = "John Arrieta";

  @Mock private GetUserByEmailPort getUserByEmailPort;

  private LoginService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service = new LoginService(getUserByEmailPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("execute() retorna el usuario cuando las credenciales son correctas y está activo")
  void shouldReturnUserWhenCredentialsAreValidAndUserIsActive() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
    final UserModel activeUser = buildUser(UserStatus.ACTIVE, UserRole.ADMIN, PASSWORD);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(activeUser));

    // Act
    final UserModel result = service.execute(command);

    // Assert
    assertNotNull(result);
    assertSame(activeUser, result);
  }

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando el email no existe")
  void shouldThrowWhenEmailNotFound() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando la contraseña es incorrecta")
  void shouldThrowWhenPasswordIsWrong() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, "WrongPass99");
    final UserModel user = buildUser(UserStatus.ACTIVE, UserRole.MEMBER, PASSWORD);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando el usuario no está ACTIVE")
  void shouldThrowWhenUserIsNotActive() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
    final UserModel pendingUser = buildUser(UserStatus.PENDING, UserRole.MEMBER, PASSWORD);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(pendingUser));

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  @Test
  @DisplayName("execute() lanza ConstraintViolationException cuando el command tiene campos inválidos")
  void shouldThrowWhenCommandIsInvalid() {
    // Arrange
    final LoginCommand command = new LoginCommand("no-es-email", "short");

    // Act & Assert
    assertThrows(ConstraintViolationException.class, () -> service.execute(command));
    verifyNoInteractions(getUserByEmailPort);
  }

  private static UserModel buildUser(
      final UserStatus status, final UserRole role, final String plainPassword) {
    return new UserModel(
        new UserId(USER_ID),
        new UserName(USER_NAME),
        new UserEmail(EMAIL),
        UserPassword.fromPlainText(plainPassword),
        role,
        status);
  }
}