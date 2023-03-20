package com.ecore.roles.service;

import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.RolesServiceImpl;
import com.ecore.roles.utils.TestData;
import com.ecore.roles.web.dto.RoleDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.lang.String.format;

import static com.ecore.roles.MessageUtil.S_NOT_FOUND;
import static com.ecore.roles.utils.TestData.DEVELOPER_ROLE;
import static com.ecore.roles.utils.TestData.UUID_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @InjectMocks
    private RolesServiceImpl rolesService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipsService membershipsService;

    @Test
    void shouldCreateRole() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.save(developerRole)).thenReturn(developerRole);

        Role role = rolesService.createRole(developerRole);

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    void shouldFailToCreateRoleWhenRoleIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.createRole(null));
    }

    @Test
    void shouldReturnRoleWhenRoleIdExists() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findById(developerRole.getId())).thenReturn(Optional.of(developerRole));

        Role role = rolesService.getRole(developerRole.getId());

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    void shouldFailToGetRoleWhenRoleIdDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
    }

    @Test
    void shouldGetRoleByUserIdAndTeamIdWhenExists() {
        Membership gianniDeveloper = TestData.DEFAULT_MEMBERSHIP();
        List<Membership> memberships = new ArrayList<Membership>();
        memberships.add(gianniDeveloper);

        when(membershipRepository.findByUserIdAndTeamId(
                gianniDeveloper.getUserId(),
                gianniDeveloper.getTeamId()))
                        .thenReturn(memberships);

        List<RoleDto> returnedRoles =
                rolesService.getRoles(
                        gianniDeveloper.getUserId(),
                        gianniDeveloper.getTeamId());

        assertNotNull(returnedRoles);
        assertEquals(returnedRoles.get(0).getName(), gianniDeveloper.getRole().getName());
    }

    @Test
    void shouldGetMoreThanOneRoleByUserIdAndTeamIdWhenExists() {
        Membership gianniDeveloper = TestData.DEFAULT_MEMBERSHIP();
        Membership gianniTester = TestData.GIANNI_TESTER_MEMBERSHIP();
        List<Membership> memberships = new ArrayList<Membership>();
        memberships.add(gianniDeveloper);
        memberships.add(gianniTester);

        when(membershipRepository.findByUserIdAndTeamId(
                gianniDeveloper.getUserId(),
                gianniDeveloper.getTeamId()))
                        .thenReturn(memberships);

        List<RoleDto> returnedRoles =
                rolesService.getRoles(
                        gianniDeveloper.getUserId(),
                        gianniDeveloper.getTeamId());

        assertNotNull(returnedRoles);
        assertThat(returnedRoles).hasSize(2);
        assertEquals(returnedRoles.get(0).getName(), gianniDeveloper.getRole().getName());
        assertEquals(returnedRoles.get(1).getName(), gianniTester.getRole().getName());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveRoleInATeam() {
        List<Membership> memberships = new ArrayList<Membership>();

        when(membershipRepository.findByUserIdAndTeamId(
                TestData.GIANNI_USER_UUID,
                TestData.ORDINARY_CORAL_LYNX_TEAM_UUID))
                        .thenReturn(memberships);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> rolesService.getRoles(
                        TestData.GIANNI_USER_UUID,
                        TestData.ORDINARY_CORAL_LYNX_TEAM_UUID));

        assertEquals(format(S_NOT_FOUND, Role.class.getSimpleName()), exception.getMessage());
    }
}
