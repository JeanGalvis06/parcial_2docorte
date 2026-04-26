package com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper;

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
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserPersistenceMapper {

  public UserPersistenceDto fromModelToDto(final UserModel user) {
    // Clean Code - Regla 14 (Ley de Deméter):
    // Cada línea encadena dos llamadas: user → getValue object → .value().
    // Por ejemplo: user.getId().value() navega al interior del value object UserId
    // para extraer el String. El mapper no debería acceder a los internals del value object;
    // debería existir un método user.getIdValue() o delegarse al propio objeto.
    // La Ley de Deméter dice: habla solo con tus amigos directos, no con los amigos de tus amigos.
    return new UserPersistenceDto(
        user.getId().value(),
        user.getName().value(),
        user.getEmail().value(),
        user.getPassword().value(),
        user.getRole().name(),
        user.getStatus().name(),
        null,
        null);
  }

  public UserEntity fromResultSetToEntity(final ResultSet resultSet) throws SQLException {
    return new UserEntity(
        resultSet.getString("id"),
        resultSet.getString("name"),
        resultSet.getString("email"),
        resultSet.getString("password"),
        resultSet.getString("role"),
        resultSet.getString("status"),
        resultSet.getString("created_at"),
        resultSet.getString("updated_at"));
  }

  public UserModel fromEntityToModel(final UserEntity entity) {
    return new UserModel(
        new UserId(entity.id()),
        new UserName(entity.name()),
        new UserEmail(entity.email()),
        UserPassword.fromHash(entity.password()),
        UserRole.fromString(entity.role()),
        UserStatus.fromString(entity.status()));
  }

  public UserModel fromResultSetToModel(final ResultSet resultSet) throws SQLException {
    return fromEntityToModel(fromResultSetToEntity(resultSet));
  }

  public List<UserModel> fromResultSetToModelList(final ResultSet resultSet) throws SQLException {
    final List<UserModel> users = new ArrayList<>();
    while (resultSet.next()) {
      users.add(fromResultSetToModel(resultSet));
    }
    return users;
  }
}