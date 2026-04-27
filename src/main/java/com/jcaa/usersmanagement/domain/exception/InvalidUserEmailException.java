package com.jcaa.usersmanagement.domain.exception;

public final class InvalidUserEmailException extends DomainException {

  private static final String EMPTY_EMAIL_MESSAGE =
      "The user email must not be empty.";
  private static final String INVALID_EMAIL_FORMAT_MESSAGE =
      "The user email format is invalid: '%s'.";

  private InvalidUserEmailException(final String message) {
    super(message);
  }

  public static InvalidUserEmailException becauseValueIsEmpty() {
    return new InvalidUserEmailException(EMPTY_EMAIL_MESSAGE);
  }

  public static InvalidUserEmailException becauseFormatIsInvalid(final String email) {
    return new InvalidUserEmailException(String.format(INVALID_EMAIL_FORMAT_MESSAGE, email));
  }
}