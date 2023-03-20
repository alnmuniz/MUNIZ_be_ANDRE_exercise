package com.ecore.roles.service;

import static com.ecore.roles.MessageUtil.S_ALREADY_EXISTS;
import static com.ecore.roles.MessageUtil.INVALID_S_OBJECT;
import static com.ecore.roles.utils.TestData.DEFAULT_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.DEVELOPER_ROLE;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.MembershipsServiceImpl;
import com.ecore.roles.utils.TestData;

@ExtendWith(MockitoExtension.class)
class MembershipsServiceTest {

    @InjectMocks
    private MembershipsServiceImpl membershipsService;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UsersService usersService;
    @Mock
    private TeamsService teamsService;

    @Test
    void shouldCreateMembership() {
        Membership newMembershipToCreate = TestData.NEW_MEMBERSHIP(false);
        Membership expectedMembership = TestData.NEW_MEMBERSHIP(true);

        when(roleRepository.findById(newMembershipToCreate.getRole().getId()))
                .thenReturn(Optional.ofNullable(DEVELOPER_ROLE()));
        when(membershipRepository.findByRoleIdAndUserIdAndTeamId(newMembershipToCreate.getRole().getId(),
                newMembershipToCreate.getUserId(),
                newMembershipToCreate.getTeamId()))
                        .thenReturn(Optional.empty());
        when(membershipRepository
                .save(newMembershipToCreate))
                        .thenReturn(expectedMembership);
        when(teamsService.isMemberOfTeam(newMembershipToCreate.getTeamId(),
                newMembershipToCreate.getUserId()))
                        .thenReturn(true);

        Membership actualMembership = membershipsService.assignRoleToMembership(newMembershipToCreate);

        assertNotNull(actualMembership);
        assertEquals(actualMembership, expectedMembership);
        verify(roleRepository).findById(expectedMembership.getRole().getId());
    }

    @Test
    void shouldFailToCreateMembershipWhenMembershipsIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.assignRoleToMembership(null));
    }

    @Test
    void shouldFailToCreateMembershipWhenItExists() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(membershipRepository.findByRoleIdAndUserIdAndTeamId(
                expectedMembership.getRole().getId(),
                expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals(format(S_ALREADY_EXISTS, Membership.class.getSimpleName()), exception.getMessage());
        verify(roleRepository, times(0)).getById(any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(0)).getTeam(any());
    }

    @Test
    void shouldFailToCreateMembershipWhenItHasInvalidRole() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.assignRoleToMembership(expectedMembership));

        assertEquals(format(INVALID_S_OBJECT, Role.class.getSimpleName()), exception.getMessage());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(roleRepository, times(0)).getById(any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(0)).getTeam(any());
    }

    @Test
    void shouldFailToGetMembershipsWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.getMemberships(null));
    }

}
