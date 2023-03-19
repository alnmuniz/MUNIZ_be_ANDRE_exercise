package com.ecore.roles.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.web.dto.RoleDto;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RolesServiceImpl implements RolesService {

    public static final String DEFAULT_ROLE = "Developer";

    private final RoleRepository roleRepository;
    private final MembershipRepository membershipRepository;

    @Autowired
    public RolesServiceImpl(
            RoleRepository roleRepository,
            MembershipRepository membershipRepository) {
        this.roleRepository = roleRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public Role createRole(@NonNull Role r) {
        if (roleRepository.findByName(r.getName()).isPresent()) {
            throw new ResourceExistsException(Role.class);
        }
        return roleRepository.save(r);
    }

    @Override
    public Role getRole(@NonNull UUID rid) {
        return roleRepository.findById(rid)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, rid));
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public List<RoleDto> getRoles(UUID userId, UUID teamId) {
        List<Membership> memberships = membershipRepository.findByUserIdAndTeamId(userId, teamId);

        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException(Role.class, null);
        }

        ArrayList<RoleDto> roles = new ArrayList<>();

        memberships.stream().forEach(m -> roles.add(RoleDto.fromModel(m.getRole())));

        return roles;
    }
}
