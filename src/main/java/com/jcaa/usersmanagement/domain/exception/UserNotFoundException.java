package com.jcaa.usersmanagement.domain.exception;

public final class UserNotFoundException extends DomainException {

  private static final String USER_ID_NOT_FOUND_MESSAGE =
      "The user with id '%s' was not found.";

  private UserNotFoundException(final String message) {
    super(message);
  }

  public static UserNotFoundException becauseIdWasNotFound(final String userId) {
    return new UserNotFoundException(String.format(USER_ID_NOT_FOUND_MESSAGE, userId));
  }
}