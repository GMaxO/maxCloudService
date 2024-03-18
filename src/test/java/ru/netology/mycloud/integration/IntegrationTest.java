package ru.netology.mycloud.integration;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.model.User;
import ru.netology.mycloud.repository.UserRepository;


import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
@Disabled
public class IntegrationTest {
    private static final String ENDPOINT = "/login";

    private static final String LOGIN = "test@test.ru";
    private static final String PASSWORD = "test1234567890";

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    PasswordEncoder passwordEncoder;

    ObjectMapper objectMapper = new ObjectMapper();

    MockMvc mockMvc;

    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("db.sql");

    @DynamicPropertySource
    private static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url=", POSTGRES_CONTAINER::getJdbcUrl);
        log.info(">> " + POSTGRES_CONTAINER.getJdbcUrl());
        dynamicPropertyRegistry.add("spring.datasource.username=", POSTGRES_CONTAINER::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password=", POSTGRES_CONTAINER::getPassword);
        dynamicPropertyRegistry.add("spring.liquibase.enabled=", () -> "true");
    }


    @BeforeEach
    public void beforeEach() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        User user = new User();
        user.setId(1L);
        user.setLogin(LOGIN);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        userRepository.save(user);
        log.info(">>>>>>>>>>>>>>> User saved!" + user.getLogin());
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthTokenPositive() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO(LOGIN, PASSWORD);

        MockHttpServletRequestBuilder postReq = MockMvcRequestBuilders.post(ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        String authToken = mockMvc.perform(postReq)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info("auth-token >>> " + authToken);
        assertNotNull(authToken);
        assertTrue(authToken.contains("auth-token"));
    }

    //Параметризованный тест - разные варианты авторизации
    @ParameterizedTest
    @MethodSource("sourceUser")
    void getAuthTokenNegative(String login, String password) throws Exception {
        AuthRequestDTO request = new AuthRequestDTO(login, password);

        MockHttpServletRequestBuilder postReq = MockMvcRequestBuilders.post(ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postReq)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    private static Stream<Arguments> sourceUser() {
        return Stream.of(
                Arguments.of(LOGIN, "b"),
                Arguments.of(LOGIN, ""),
                Arguments.of(null, "bssfs"),
                Arguments.of("b", PASSWORD),
                Arguments.of("b", ""),
                Arguments.of("b", null),
                Arguments.of(null, null),
                Arguments.of("", "")
        );
    }


}