package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.UpdateUserUseCase;
import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByIdPort;
import com.jcaa.usersmanagement.application.port.out.UpdateUserPort;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.application.service.mapper.UserApplicationMapper;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public final class UpdateUserService implements UpdateUserUseCase {

  private final UpdateUserPort updateUserPort;
  private final GetUserByIdPort getUserByIdPort;
  private final GetUserByEmailPort getUserByEmailPort;
  private final EmailNotificationService emailNotificationService;
  private final Validator validator;

  @Override
  public UserModel execute(final UpdateUserCommand command) {
    validateCommand(command);

    log.info("Actualizando usuario id=" + command.id() + ", email=" + command.email() + ", nombre=" + command.name());

    final UserId userId = new UserId(command.id());
    final UserModel currentUser = findExistingUserOrFail(userId);
    final UserEmail newEmail = new UserEmail(command.email());

    ensureEmailIsNotTakenByAnotherUser(newEmail, userId);

    final UserModel userToUpdate =
        UserApplicationMapper.fromUpdateCommandToModel(command, currentUser.getPassword());

    return updateUserAndNotify(userToUpdate);
  }

  private UserModel updateUserAndNotify(final UserModel userToUpdate) {
    final UserModel updatedUser = updateUserPort.update(userToUpdate);
    notifyUserUpdated(updatedUser);
    return updatedUser;
  }

  private void notifyUserUpdated(final UserModel user) {
    emailNotificationService.notifyUserUpdated(user);
  }

  private void validateCommand(final UpdateUserCommand command) {
    final Set<ConstraintViolation<UpdateUserCommand>> violations = validator.validate(command);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private UserModel findExistingUserOrFail(final UserId userId) {
    return getUserByIdPort
        .getById(userId)
        .orElseThrow(() -> UserNotFoundException.becauseIdWasNotFound(userId.value()));
  }

  private void ensureEmailIsNotTakenByAnotherUser(final UserEmail newEmail, final UserId ownerId) {
    final var existingUser = getUserByEmailPort.getByEmail(newEmail);

    if (existingUser.isPresent() && !existingUser.get().getId().equals(ownerId)) {
      throw UserAlreadyExistsException.becauseEmailAlreadyExists(newEmail.value());
    }
  }
}