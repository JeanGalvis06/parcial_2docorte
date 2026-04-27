package com.jcaa.usersmanagement.application.service.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserByIdQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;

public class UserApplicationMapper {

  private static final int ADMIN_ROLE_CODE = 1;
  private static final int MEMBER_ROLE_CODE = 2;
  private static final int REVIEWER_ROLE_CODE = 3;

  public static UserModel fromCreateCommandToModel(final CreateUserCommand command) {
    final String userId = command.id();
    final String userName = command.name();
    final String userEmail = command.email();
    final String userPassword = command.password();
    final String userRole = command.role();

    return UserModel.create(
        new UserId(userId),
        new UserName(userName),
        new UserEmail(userEmail),
        UserPassword.fromPlainText(userPassword),
        UserRole.fromString(userRole));
  }

  public static UserModel fromUpdateCommandToModel(
      final UpdateUserCommand command, final UserPassword currentPassword) {

    final UserPassword passwordToUse = resolvePassword(command, currentPassword);
    final String userEmail = command.email();

    return new UserModel(
        new UserId(command.id()),
        new UserName(command.name()),
        new UserEmail(userEmail),
        passwordToUse,
        UserRole.fromString(command.role()),
        UserStatus.fromString(command.status()));
  }

  public static UserId fromGetUserByIdQueryToUserId(final GetUserByIdQuery query) {
    return new UserId(query.id());
  }

  public static UserId fromDeleteCommandToUserId(final DeleteUserCommand command) {
    return new UserId(command.id());
  }

  public static int roleToCode(final String role) {
    final UserRole userRole = UserRole.fromString(role);

    return switch (userRole) {
      case ADMIN -> ADMIN_ROLE_CODE;
      case MEMBER -> MEMBER_ROLE_CODE;
      case REVIEWER -> REVIEWER_ROLE_CODE;
    };
  }

  private static UserPassword resolvePassword(
      final UpdateUserCommand command, final UserPassword currentPassword) {
    if (command.password() == null || command.password().isBlank()) {
      return currentPassword;
    }

    return UserPassword.fromPlainText(command.password());
  }
}