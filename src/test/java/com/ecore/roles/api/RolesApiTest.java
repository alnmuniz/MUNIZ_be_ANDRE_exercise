package com.ecore.roles.api;

import com.ecore.roles.model.Membership;
import com.ecore.roles.model.Role;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.RoleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Optional;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.RestAssuredHelper.createMembership;
import static com.ecore.roles.utils.RestAssuredHelper.createRole;
import static com.ecore.roles.utils.RestAssuredHelper.getRole;
import static com.ecore.roles.utils.RestAssuredHelper.getRoles;
import static com.ecore.roles.utils.RestAssuredHelper.sendRequest;
import static com.ecore.roles.utils.TestData.DEFAULT_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.GIANNI_TESTER_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.DEVELOPER_ROLE;
import static com.ecore.roles.utils.TestData.DEVOPS_ROLE;
import static com.ecore.roles.utils.TestData.GIANNI_USER_UUID;
import static com.ecore.roles.utils.TestData.ORDINARY_CORAL_LYNX_TEAM;
import static com.ecore.roles.utils.TestData.ORDINARY_CORAL_LYNX_TEAM_UUID;
import static com.ecore.roles.utils.TestData.PRODUCT_OWNER_ROLE;
import static com.ecore.roles.utils.TestData.TESTER_ROLE;
import static com.ecore.roles.utils.TestData.UUID_1;
import static io.restassured.RestAssured.when;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import static com.ecore.roles.MessageUtil.S_ALREADY_EXISTS;
import static com.ecore.roles.MessageUtil.S_S_NOT_FOUND;
import static com.ecore.roles.MessageUtil.S_NOT_FOUND;
import static com.ecore.roles.MessageUtil.BAD_REQUEST;
import static com.ecore.roles.MessageUtil.NOT_FOUND;;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RolesApiTest {

    private final RestTemplate restTemplate;
    private final RoleRepository roleRepository;

    private MockRestServiceServer mockServer;

    @Autowired
    private Environment env;

    @LocalServerPort
    private int port;

    @Autowired
    public RolesApiTest(RestTemplate restTemplate, RoleRepository roleRepository) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        Optional<Role> devOpsRole = roleRepository.findByName(DEVOPS_ROLE().getName());
        devOpsRole.ifPresent(roleRepository::delete);
    }

    @Test
    void shouldFailWhenPathDoesNotExist() {
        sendRequest(when()
                .get("/v1/role")
                .then())
                        .validate(RestAssuredHelper.HTTP_NOT_FOUND, NOT_FOUND);
    }

    @Test
    void shouldCreateNewRole() {
        Role expectedRole = DEVOPS_ROLE();

        RoleDto actualRole = createRole(expectedRole)
                .statusCode(RestAssuredHelper.HTTP_CREATED)
                .extract().as(RoleDto.class);

        assertThat(actualRole.getName()).isEqualTo(expectedRole.getName());
    }

    @Test
    void shouldFailToCreateNewRoleWhenNull() {
        createRole(null)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateNewRoleWhenMissingName() {
        createRole(Role.builder().build())
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateNewRoleWhenBlankName() {
        createRole(Role.builder().name("").build())
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToCreateNewRoleWhenNameAlreadyExists() {
        createRole(DEVELOPER_ROLE())
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST,
                        format(S_ALREADY_EXISTS, Role.class.getSimpleName()));
    }

    @Test
    void shouldGetAllRoles() {
        RoleDto[] roles = getRoles()
                .extract().as(RoleDto[].class);

        assertThat(roles)
                .hasSizeGreaterThanOrEqualTo(3)
                .contains(RoleDto.fromModel(DEVELOPER_ROLE()))
                .contains(RoleDto.fromModel(PRODUCT_OWNER_ROLE()))
                .contains(RoleDto.fromModel(TESTER_ROLE()));
    }

    @Test
    void shouldGetRoleById() {
        Role expectedRole = DEVELOPER_ROLE();

        getRole(expectedRole.getId())
                .statusCode(RestAssuredHelper.HTTP_OK)
                .body("name", equalTo(expectedRole.getName()));
    }

    @Test
    void shouldFailToGetRoleById() {
        getRole(UUID_1)
                .validate(RestAssuredHelper.HTTP_NOT_FOUND,
                        format(S_S_NOT_FOUND, Role.class.getSimpleName(), UUID_1));
    }

    @Test
    void shouldGetRoleByUserIdAndTeamId() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, ORDINARY_CORAL_LYNX_TEAM(), env);
        createMembership(expectedMembership)
                .statusCode(RestAssuredHelper.HTTP_CREATED);

        ArrayList<String> expectedApiResult = new ArrayList<>();
        expectedApiResult.add(expectedMembership.getRole().getName());

        getRole(expectedMembership.getUserId(), expectedMembership.getTeamId())
                .statusCode(RestAssuredHelper.HTTP_OK)
                .body("name", equalTo(expectedApiResult));
    }

    @Test
    void shouldGetMoreThanOneRoleByUserIdAndTeamId() {
        Membership expectedMembershipGianniTester = GIANNI_TESTER_MEMBERSHIP();

        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, ORDINARY_CORAL_LYNX_TEAM(), env);

        createMembership(expectedMembershipGianniTester)
                .statusCode(RestAssuredHelper.HTTP_CREATED);

        ArrayList<String> expectedApiResult = new ArrayList<>();

        /*
         * Developer role should be inserted by previous test shouldGetRoleByUserIdAndTeamId
         */
        expectedApiResult.add(DEVELOPER_ROLE().getName());

        expectedApiResult.add(expectedMembershipGianniTester.getRole().getName());

        getRole(expectedMembershipGianniTester.getUserId(), expectedMembershipGianniTester.getTeamId())
                .statusCode(RestAssuredHelper.HTTP_OK)
                .body("name", equalTo(expectedApiResult));
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingUserId() {
        getRole(null, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingTeamId() {
        getRole(GIANNI_USER_UUID, null)
                .validate(RestAssuredHelper.HTTP_BAD_REQUEST, BAD_REQUEST);
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenItDoesNotExist() {
        mockGetTeamById(mockServer, UUID_1, null, env);
        getRole(GIANNI_USER_UUID, UUID_1)
                .validate(RestAssuredHelper.HTTP_NOT_FOUND,
                        format(S_NOT_FOUND, Role.class.getSimpleName()));
    }
}
