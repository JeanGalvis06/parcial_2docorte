package com.jcaa.usersmanagement;

import com.jcaa.usersmanagement.infrastructure.config.DependencyContainer;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.UserManagementCli;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import java.util.Scanner;
import java.util.logging.Logger;

public final class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  private Main() {
  }

  public static void main(final String[] args) {
    LOGGER.info("Starting Users Management System...");
    runApplication();
  }

  private static void runApplication() {
    final DependencyContainer container = buildDependencyContainer();

    try (final Scanner scanner = buildScanner()) {
      final UserManagementCli cli = buildCli(container, scanner);
      cli.start();
    }
  }

  private static DependencyContainer buildDependencyContainer() {
    return new DependencyContainer();
  }

  private static Scanner buildScanner() {
    return new Scanner(System.in);
  }

  private static UserManagementCli buildCli(
      final DependencyContainer container, final Scanner scanner) {
    return new UserManagementCli(container.userController(), buildConsole(scanner));
  }

  private static ConsoleIO buildConsole(final Scanner scanner) {
    return new ConsoleIO(scanner, System.out);
  }
}