package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public final class EmailNotificationService {

  private static final String SUBJECT_CREATED = "Tu cuenta ha sido creada — Gestión de Usuarios";
  private static final String SUBJECT_UPDATED =
      "Tu cuenta ha sido actualizada — Gestión de Usuarios";

  private static final String USER_CREATED_TEMPLATE = "user-created.html";
  private static final String USER_UPDATED_TEMPLATE = "user-updated.html";

  private static final String TOKEN_NAME = "name";
  private static final String TOKEN_EMAIL = "email";
  private static final String TOKEN_PASSWORD = "password";
  private static final String TOKEN_ROLE = "role";
  private static final String TOKEN_STATUS = "status";

  private final EmailSenderPort emailSenderPort;

  public void notifyUserCreated(final UserModel user, final String plainPassword) {
    final Map<String, String> tokens = buildCreatedUserTokens(user, plainPassword);
    notifyUser(user, SUBJECT_CREATED, USER_CREATED_TEMPLATE, tokens);
  }

  public void notifyUserUpdated(final UserModel user) {
    final Map<String, String> tokens = buildUpdatedUserTokens(user);
    notifyUser(user, SUBJECT_UPDATED, USER_UPDATED_TEMPLATE, tokens);
  }

  public void sendNotificationWithFlag(
      final UserModel user, final boolean includePassword, final String plainPassword) {
    if (includePassword) {
      notifyUserCreated(user, plainPassword);
    } else {
      notifyUserUpdated(user);
    }
  }

  private void notifyUser(
      final UserModel user,
      final String subject,
      final String templateName,
      final Map<String, String> tokens) {
    final String template = loadTemplate(templateName);
    final String body = renderTemplate(template, tokens);
    final EmailDestinationModel destination = buildDestination(user, subject, body);

    sendOrLog(destination);
  }

  private static Map<String, String> buildCreatedUserTokens(
      final UserModel user, final String plainPassword) {
    return Map.of(
        TOKEN_NAME, user.getName().value(),
        TOKEN_EMAIL, user.getEmail().value(),
        TOKEN_PASSWORD, plainPassword,
        TOKEN_ROLE, user.getRole().name());
  }

  private static Map<String, String> buildUpdatedUserTokens(final UserModel user) {
    return Map.of(
        TOKEN_NAME, user.getName().value(),
        TOKEN_EMAIL, user.getEmail().value(),
        TOKEN_ROLE, user.getRole().name(),
        TOKEN_STATUS, user.getStatus().name());
  }

  private static EmailDestinationModel buildDestination(
      final UserModel user, final String subject, final String body) {
    return new EmailDestinationModel(
        user.getEmail().value(), user.getName().value(), subject, body);
  }

  private String loadTemplate(final String templateName) {
    final String path = "/templates/" + templateName;

    try (InputStream inputStream = openResourceStream(path)) {
      if (Objects.isNull(inputStream)) {
        throw EmailSenderException.becauseSendFailed(
            new IllegalStateException("Template not found: " + path));
      }

      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioException) {
      throw EmailSenderException.becauseSendFailed(ioException);
    }
  }

  InputStream openResourceStream(final String path) {
    return getClass().getResourceAsStream(path);
  }

  private static String renderTemplate(
      final String template, final Map<String, String> values) {
    String result = template;

    for (final Map.Entry<String, String> tokenEntry : values.entrySet()) {
      final String token = "{{" + tokenEntry.getKey() + "}}";
      result = result.replace(token, tokenEntry.getValue());
    }

    return result;
  }

  private void sendOrLog(final EmailDestinationModel destination) {
    try {
      emailSenderPort.send(destination);
    } catch (final EmailSenderException senderException) {
      log.log(
          Level.WARNING,
          "[EmailNotificationService] No se pudo enviar correo a: {0}. Causa: {1}",
          new Object[] {destination.getDestinationEmail(), senderException.getMessage()});
      throw senderException;
    }
  }
}