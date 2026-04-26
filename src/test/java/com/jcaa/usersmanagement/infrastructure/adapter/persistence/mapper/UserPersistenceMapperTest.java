package com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.dto.UserPersistenceDto;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity.UserEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// VIOLACIÓN Regla 11: se eliminó el javadoc de la clase que documentaba qué casos cubre.
@DisplayName("UserPersistenceMapper")
@ExtendWith(MockitoExtension.class)
class UserPersistenceMapperTest {

  private static final String ID = "u-001";
  private static final String NAME = "John Doe";
  private static final String EMAIL = "john@example.com";
  private static final String HASH = "$2a$12$abcdefghijklmnopqrstuO";
  private static final String ROLE = "ADMIN";
  private static final String STATUS = "ACTIVE";
  private static final String CREATED_AT = "2024-01-01 00:00:00";
  private static final String UPDATED_AT = "2024-01-02 00:00:00";

  @Mock private ResultSet resultSet;

  private UserModel userModel;
  private UserEntity userEntity;

  @BeforeEach
  void setUp() {
    userModel =
        new UserModel(
            new UserId(ID),
            new UserName(NAME),
            new UserEmail(EMAIL),
            UserPassword.fromHash(HASH),
            UserRole.ADMIN,
            UserStatus.ACTIVE);

    userEntity = new UserEntity(ID, NAME, EMAIL, HASH, ROLE, STATUS, CREATED_AT, UPDATED_AT);
  }

  @Test
  @DisplayName("fromModelToDto() maps all UserModel fields and sets null timestamps")
  void shouldMapModelToDto() {
    final UserPersistenceDto result = UserPersistenceMapper.fromModelToDto(userModel);

    assertAll(
        "fromModelToDto()",
        () -> assertEquals(ID, result.id(), "id"),
        () -> assertEquals(NAME, result.name(), "name"),
        () -> assertEquals(EMAIL, result.email(), "email"),
        () -> assertEquals(HASH, result.password(), "password"),
        () -> assertEquals(ROLE, result.role(), "role"),
        () -> assertEquals(STATUS, result.status(), "status"),
        () -> assertNull(result.createdAt(), "createdAt must be null"),
        () -> assertNull(result.updatedAt(), "updatedAt must be null"));
  }

  @Test
  @DisplayName("fromEntityToModel() maps all UserEntity fields to a domain UserModel")
  void shouldMapEntityToModel() {
    final UserModel result = UserPersistenceMapper.fromEntityToModel(userEntity);

    assertAll(
        "fromEntityToModel()",
        () -> assertEquals(ID, result.getId().value(), "id"),
        () -> assertEquals(NAME, result.getName().value(), "name"),
        () -> assertEquals(EMAIL, result.getEmail().value(), "email"),
        () -> assertEquals(UserRole.ADMIN, result.getRole(), "role"),
        () -> assertEquals(UserStatus.ACTIVE, result.getStatus(), "status"));
  }

  @Test
  @DisplayName("fromResultSetToEntity() reads all eight columns from the ResultSet")
  void shouldReadAllColumnsFromResultSet() throws SQLException {
    when(resultSet.getString("id")).thenReturn(ID);
    when(resultSet.getString("name")).thenReturn(NAME);
    when(resultSet.getString("email")).thenReturn(EMAIL);
    when(resultSet.getString("password")).thenReturn(HASH);
    when(resultSet.getString("role")).thenReturn(ROLE);
    when(resultSet.getString("status")).thenReturn(STATUS);
    when(resultSet.getString("created_at")).thenReturn(CREATED_AT);
    when(resultSet.getString("updated_at")).thenReturn(UPDATED_AT);

    final UserEntity result = UserPersistenceMapper.fromResultSetToEntity(resultSet);

    assertAll(
        "fromResultSetToEntity()",
        () -> assertEquals(ID, result.id(), "id"),
        () -> assertEquals(NAME, result.name(), "name"),
        () -> assertEquals(EMAIL, result.email(), "email"),
        () -> assertEquals(HASH, result.password(), "password"),
        () -> assertEquals(ROLE, result.role(), "role"),
        () -> assertEquals(STATUS, result.status(), "status"),
        () -> assertEquals(CREATED_AT, result.createdAt(), "createdAt"),
        () -> assertEquals(UPDATED_AT, result.updatedAt(), "updatedAt"));
  }

  @Test
  @DisplayName("fromResultSetToEntity() propagates SQLException when ResultSet read fails")
  void shouldPropagateExceptionFromResultSet() throws SQLException {
    when(resultSet.getString(anyString())).thenThrow(new SQLException("Column read failed"));

    assertThrows(
        SQLException.class,
        () -> UserPersistenceMapper.fromResultSetToEntity(resultSet),
        "must propagate SQLException when ResultSet throws on getString");
  }

  @Test
  @DisplayName("fromResultSetToModelList() returns an empty list when ResultSet has no rows")
  void shouldReturnEmptyListWhenResultSetIsEmpty() throws SQLException {
    when(resultSet.next()).thenReturn(false);

    final List<UserModel> result = UserPersistenceMapper.fromResultSetToModelList(resultSet);

    assertTrue(result.isEmpty(), "must return an empty list when ResultSet has no rows");
  }

  @Test
  @DisplayName("fromResultSetToModelList() returns one model per row in the ResultSet")
  void shouldReturnOneModelPerRow() throws SQLException {
    when(resultSet.next()).thenReturn(true, true, false);
    when(resultSet.getString("id")).thenReturn(ID, "u-002");
    when(resultSet.getString("name")).thenReturn(NAME, "Jane Doe");
    when(resultSet.getString("email")).thenReturn(EMAIL, "jane@example.com");
    when(resultSet.getString("password")).thenReturn(HASH, HASH);
    when(resultSet.getString("role")).thenReturn(ROLE, "MEMBER");
    when(resultSet.getString("status")).thenReturn(STATUS, "PENDING");
    when(resultSet.getString("created_at")).thenReturn(CREATED_AT, CREATED_AT);
    when(resultSet.getString("updated_at")).thenReturn(UPDATED_AT, UPDATED_AT);

    final List<UserModel> result = UserPersistenceMapper.fromResultSetToModelList(resultSet);

    assertEquals(2, result.size(), "must return one model per row in the ResultSet");
  }

  @Test
  @DisplayName("fromResultSetToModelList() propagates SQLException when a row read fails")
  void shouldPropagateExceptionDuringIteration() throws SQLException {
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString(anyString())).thenThrow(new SQLException("Row read failed"));

    assertThrows(
        SQLException.class,
        () -> UserPersistenceMapper.fromResultSetToModelList(resultSet),
        "must propagate SQLException when a row fails to be read");
  }
}