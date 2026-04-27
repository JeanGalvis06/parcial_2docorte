package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.util.Objects;

public class UserValidationUtils {

  public static boolean isUserActive(final UserModel user) {
    return UserStatus.ACTIVE.equals(user.getStatus());
  }

  public static boolean isAdmin(final UserModel user) {
    return UserRole.ADMIN.equals(user.getRole());
  }

  public static boolean isValidEmail(final String email) {
    if (Objects.isNull(email) || email.isBlank()) {
      return false;
    }

    return email.contains("@") && email.contains(".");
  }

  public static boolean isValidPassword(final String password) {
    return Objects.nonNull(password) && password.length() >= 8;
  }

  public static boolean canPerformAction(final UserActionPermission permission) {
    return hasValidIdentity(permission)
        && hasAllowedStatus(permission.status())
        && permission.maxInactivityDays() >= 0;
  }

  private static boolean hasValidIdentity(final UserActionPermission permission) {
    return Objects.nonNull(permission.userId())
        && Objects.nonNull(permission.email());
  }

  private static boolean hasAllowedStatus(final UserStatus status) {
    return UserStatus.ACTIVE.equals(status) || UserStatus.PENDING.equals(status);
  }

  public record UserActionPermission(
      UserId userId,
      UserEmail email,
      UserStatus status,
      int maxInactivityDays) {

    public UserActionPermission {
      Objects.requireNonNull(userId, "User id cannot be null");
      Objects.requireNonNull(email, "User email cannot be null");
      Objects.requireNonNull(status, "User status cannot be null");
    }
  }
}