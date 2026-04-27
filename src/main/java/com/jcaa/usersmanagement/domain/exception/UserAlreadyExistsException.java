package com.jcaa.usersmanagement.domain.exception;

public final class UserAlreadyExistsException extends DomainException {

  private static final String EMAIL_ALREADY_EXISTS_MESSAGE =
      "A user with email '%s' already exists.";

  private UserAlreadyExistsException(final String message) {
    super(message);
  }

  public static UserAlreadyExistsException becauseEmailAlreadyExists(final String email) {
    return new UserAlreadyExistsException(String.format(EMAIL_ALREADY_EXISTS_MESSAGE, email));
  }
}