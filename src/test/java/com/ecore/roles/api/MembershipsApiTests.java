package com.ecore.roles.api;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.RestAssuredHelper.createMembership;
import static com.ecore.roles.utils.RestAssuredHelper.getMemberships;
import static com.ecore.roles.utils.TestData.DEFAULT_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.DEVELOPER_ROLE_UUID;
import static com.ecore.roles.utils.TestData.INVALID_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.ORDINARY_CORAL_LYNX_TEAM;
import static com.ecore.roles.utils.TestData.UUID_1;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.MembershipDto;

import static com.ecore.roles.MessageUtil.S_ALREADY_EXISTS;
import static com.ecore.roles.MessageUtil.S_S_NOT_FOUND;
import static com.ecore.roles.MessageUtil.INVALID_S_OBJECT;
import static com.ecore.roles.MessageUtil.PROV_USR_DOESNT_BELONG_PROV_TEAM;
import static com.ecore.roles.MessageUtil.BAD_REQUEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MembershipsApiTests {

    private final MembershipRepository membershipRepository;
    private final RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Autowired
    private Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    public MembershipsApiTests(MembershipRepository membershipRepository, RestTemplate restTemplate) {
        this.membershipRepository = membershipRepository;
        this.restTemplate = restTemplate;
    }

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        membershipRepository.deleteAll();
    }

    @Test
    void shouldCreateRoleMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        MembershipDto actualMembership = createDefaultMembership();

        assertThat(actualMembership.getId()).isNotNull();
        assertThat(actualMembership).isEqualTo(MembershipDto.fromModel(expectedMembership));
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenBodyIsNull() {
        createMembership(null)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenRoleIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenRoleIdIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(Role.builder().build());

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenUserIdIsNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setUserId(null);

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenTeamIdISNull() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setTeamId(null);

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenMembershipAlreadyExists() {
        createDefaultMembership();

        createMembership(DEFAULT_MEMBERSHIP())
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST,
                        format(S_ALREADY_EXISTS, Membership.class.getSimpleName()));
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenRoleDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(Role.builder().id(UUID_1).build());

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_NOT_FOUND,
                        format(S_S_NOT_FOUND, Role.class.getSimpleName(), UUID_1));
    }

    @Test
    void shouldFailToCreateRoleMembershipWhenTeamDoesNotExist() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, expectedMembership.getTeamId(), null, env);

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_NOT_FOUND,
                        format("Team %s not found", expectedMembership.getTeamId()));
    }

    @Test
    void shouldFailToAssignRoleWhenMembershipIsInvalid() {
        Membership expectedMembership = INVALID_MEMBERSHIP();
        mockGetTeamById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(), env);

        createMembership(expectedMembership)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST,
                        format(INVALID_S_OBJECT, Membership.class.getSimpleName()) + ". "
                                + PROV_USR_DOESNT_BELONG_PROV_TEAM);
    }

    @Test
    void shouldGetAllMemberships() {
        createDefaultMembership();
        Membership expectedMembership = DEFAULT_MEMBERSHIP();

        MembershipDto[] actualMemberships = getMemberships(expectedMembership.getRole().getId())
                .statusCode(RestAssuredHelper.HTTP_OK)
                .extract().as(MembershipDto[].class);

        assertThat(actualMemberships.length).isEqualTo(1);
        assertThat(actualMemberships[0].getId()).isNotNull();
        assertThat(actualMemberships[0]).isEqualTo(MembershipDto.fromModel(expectedMembership));
    }

    @Test
    void shouldGetAllMembershipsButReturnsEmptyList() {
        MembershipDto[] actualMemberships = getMemberships(DEVELOPER_ROLE_UUID)
                .statusCode(RestAssuredHelper.HTTP_OK)
                .extract().as(MembershipDto[].class);

        assertThat(actualMemberships.length).isEqualTo(0);
    }

    @Test
    void shouldFailToGetAllMembershipsWhenRoleIdIsNull() {
        getMemberships(null)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    private MembershipDto createDefaultMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, expectedMembership.getTeamId(), ORDINARY_CORAL_LYNX_TEAM(), env);

        return createMembership(expectedMembership)
                .statusCode(RestAssuredHelper.HTTP_CREATED)
                .extract().as(MembershipDto.class);
    }

}
