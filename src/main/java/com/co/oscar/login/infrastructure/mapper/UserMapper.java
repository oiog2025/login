package com.co.oscar.login.infrastructure.mapper;


import com.co.oscar.login.domain.User;
import com.co.oscar.login.infrastructure.entrypoints.dtos.UserDto;
import com.co.oscar.login.infrastructure.entrypoints.dtos.UserUpdateDto;
import com.co.oscar.login.infrastructure.persistence.jpa.UserEntity;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "email", target = "username")
    User toDomain(UserDto dto);

    @Mapping(source = "username", target = "email")
    UserDto toDto(User domain);

    @Mapping(source = "email", target = "username")
    User toDomain(UserEntity entity);

    @Mapping(source = "username", target = "email")
    UserEntity toEntity(User domain);

    List<User> toDomainList(List<UserEntity> entities);

    List<UserDto> toDtoList(List<User> domainUsers);

    @Mapping(source = "email", target = "username")
    User toDomain(UserUpdateDto dto);
}
