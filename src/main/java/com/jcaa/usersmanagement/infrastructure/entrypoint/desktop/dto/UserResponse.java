package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto;

public record UserResponse(
    String id,
    String name,
    String email,
    String role,
    String status) {

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  public String getStatus() {
    return status;
  }
}