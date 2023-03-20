package com.ecore.roles.service;

import com.ecore.roles.model.Role;
import com.ecore.roles.web.dto.RoleDto;

import java.util.List;
import java.util.UUID;

public interface RolesService {

    Role createRole(Role role);

    Role getRole(UUID id);

    List<Role> getRoles();

    List<RoleDto> getRoles(UUID userId, UUID teamId);

}
