package ru.netology.mycloud.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;


import ru.netology.mycloud.dto.AuthRequestDTO;
import ru.netology.mycloud.dto.FileDTO;
import ru.netology.mycloud.dto.RenameFileDTO;
import ru.netology.mycloud.model.FileModel;
import ru.netology.mycloud.model.User;
import ru.netology.mycloud.repository.FileRepository;
import ru.netology.mycloud.repository.UserRepository;
import ru.netology.mycloud.service.FileService;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
@Disabled
public class FileTest {
    private static final String ENDPOINT_LOGIN = "/login";
    private static final String LOGIN = "test@test.ru";
    private static final String PASSWORD = "test1234567890";


    private static final String ENDPOINT_FILE = "/file";
    private static final String ENDPOINT_LIST = "/list";

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileService fileServiceImpl;

    ObjectMapper objectMapper = new ObjectMapper();

    MockMvc mockMvc;

    AuthToken authToken;

    User user;

    private final String AUTH_TOKEN_HEADER = "auth-token";
    private final String BEARER_TOKEN = "Bearer ";

    private final String PARAM_FILENAME = "filename";
    private static final String PATH_TEST_IMAGE = "./src/test/resources/img.png";
    private static final String NAME_TEST_IMAGE = "getOriginalFileMock.png";
    private static final String NAME_COLUMN_CONTROLLER_FILE = "file";
    private static final String NAME_COLUMN_CONTROLLER_FILE_UPDATE = "filename";

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
    public void beforeEach() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        user = new User();
        user.setId(1L);
        user.setLogin(LOGIN);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        userRepository.save(user);

        AuthRequestDTO request = new AuthRequestDTO(LOGIN, PASSWORD);

        MockHttpServletRequestBuilder postReq = MockMvcRequestBuilders.post(ENDPOINT_LOGIN)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        String auth = mockMvc.perform(postReq)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = objectMapper.readValue(auth, AuthToken.class);
        log.info(">>>authToken>" + authToken.getAuthToken());
    }


    @AfterEach
    public void afterEach() {
        fileRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAuthToken() {
        assertNotNull(authToken.getAuthToken());
    }


    //Файл
    private static MockMultipartFile getFileMock() throws IOException {
        return new MockMultipartFile(
                NAME_COLUMN_CONTROLLER_FILE,   //имя в форме передачи
                NAME_TEST_IMAGE,
                MediaType.IMAGE_PNG_VALUE,
                new FileInputStream(PATH_TEST_IMAGE)
        );
    }


    //Загрузка
    @Test
    void createTestPositive() throws Exception {
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .multipart(ENDPOINT_FILE)
                .file(getFileMock());

        MockHttpServletRequestBuilder request = builder
                .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                .param(PARAM_FILENAME, NAME_TEST_IMAGE)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());

        FileModel file = fileRepository.findFileByFilenameAndUser("getOriginalFileMock.png", user);
        assertNotNull(file);
    }

    @Test
    void createTestNegative() throws Exception {
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .multipart(ENDPOINT_FILE)
                .file(getFileMock());

        MockHttpServletRequestBuilder request = builder
                .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                .param(PARAM_FILENAME, "")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());


        FileModel file = fileRepository.findFileByFilenameAndUser("getOriginalFileMock.png", user);
        assertNull(file);
    }

    @Test
    void createTestNegativeFile() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(ENDPOINT_FILE)
                .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                .param(PARAM_FILENAME, "")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());


        FileModel file = fileRepository.findFileByFilenameAndUser(NAME_TEST_IMAGE, user);
        assertNull(file);
    }

    //Переименование
    @Test
    void updateTestPositive() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        MockMultipartFile multipartFile = getFileMock();
        fileServiceImpl.saveFile(multipartFile, NAME_TEST_IMAGE);

        String newFileName = "152.png";
        RenameFileDTO request = new RenameFileDTO(newFileName);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT_FILE)
                        .param(NAME_COLUMN_CONTROLLER_FILE_UPDATE, NAME_TEST_IMAGE)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        FileModel file = fileRepository.findFileByFilenameAndUser(newFileName, user);
        assertNotNull(file);
        assertEquals(newFileName, file.getFilename());
    }

    @Test
    void updateTestNegative() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        MockMultipartFile multipartFile = getFileMock();
        fileServiceImpl.saveFile(multipartFile, NAME_TEST_IMAGE);

        String newFileName = "152.png";
        RenameFileDTO request = new RenameFileDTO(newFileName);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT_FILE)
                        .param(NAME_COLUMN_CONTROLLER_FILE_UPDATE, NAME_TEST_IMAGE)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        FileModel file = fileRepository.findFileByFilenameAndUser(newFileName, user);
        assertNull(file);
    }

    @Test
    void updateTestNegativeFile() throws Exception {
        String newFileName = "152.png";
        RenameFileDTO request = new RenameFileDTO(newFileName);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT_FILE)
                        .param(NAME_COLUMN_CONTROLLER_FILE_UPDATE, NAME_TEST_IMAGE)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        FileModel file = fileRepository.findFileByFilenameAndUser(newFileName, user);
        assertNull(file);
    }

    @Test
    void updateTestNegativeFileName() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        MockMultipartFile multipartFile = getFileMock();
        fileServiceImpl.saveFile(multipartFile, NAME_TEST_IMAGE);

        String newFileName = "";
        RenameFileDTO request = new RenameFileDTO(newFileName);

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT_FILE)
                        .param(NAME_COLUMN_CONTROLLER_FILE_UPDATE, NAME_TEST_IMAGE)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    //Вывод - list

    @Test
    void testGetFilesList() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        MockMultipartFile multipartFile = getFileMock();
        fileServiceImpl.saveFile(multipartFile, NAME_TEST_IMAGE);
        fileServiceImpl.saveFile(multipartFile, "NAME_TEST_IMAGE_0.png");
        fileServiceImpl.saveFile(multipartFile, "NAME_TEST_IMAGE_1.png");

        String response = mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT_LIST)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                        .param("limit", String.valueOf(3)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<FileDTO> fileListResponses = objectMapper.readValue(response,
                new TypeReference<List<FileDTO>>() {
                });

        assertFalse(fileListResponses.isEmpty());
        assertEquals(3, fileListResponses.size());
    }

    @Test
    void testGetFilesListNegative() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT_LIST)
                        .param("limit", String.valueOf(3)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testGetFilesListPositiveLimitZero() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT_LIST)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                        .param("limit", String.valueOf(0)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testGetFilesListPositiveNoFiles() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(LOGIN);

        String response = mockMvc
                .perform(MockMvcRequestBuilders.get(ENDPOINT_LIST)
                        .header(AUTH_TOKEN_HEADER, BEARER_TOKEN + authToken.getAuthToken())
                        .param("limit", String.valueOf(3)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<FileDTO> fileListResponses = objectMapper.readValue(response,
                new TypeReference<List<FileDTO>>() {
                });

        assertTrue(fileListResponses.isEmpty());
    }
}