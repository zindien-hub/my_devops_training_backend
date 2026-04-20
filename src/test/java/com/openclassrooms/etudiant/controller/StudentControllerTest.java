package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.CreateStudentDTO;
import com.openclassrooms.etudiant.dto.UpdateStudentDTO;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class StudentControllerTest {

    private static final String URL = "/api/students";
    private static final String FIRST_NAME = "Tom";
    private static final String LAST_NAME = "Sawyer";
    private static final String UPDATED_FIRST_NAME = "Tom-Updated";
    private static final String UPDATED_LAST_NAME = "Sawyer-Updated";

    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Doe";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    private String jwtToken;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // Connecte la datasource Spring au conteneur MySQL temporaire.
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("jwt.secret", () -> "test-secret-key-at-least-32-characters-long");
        registry.add("jwt.expiration-ms", () -> "3600000");
    }

    @BeforeEach
    public void setUp() {
        // Create a test user and get a valid JWT for protected routes
        User user = new User();
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        userService.register(user);
        jwtToken = userService.login(LOGIN, PASSWORD);
    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void getAllStudentsSuccessful() throws Exception {
        // GIVEN
        CreateStudentDTO createStudentDTO = new CreateStudentDTO();
        createStudentDTO.setFirstName(FIRST_NAME);
        createStudentDTO.setLastName(LAST_NAME);

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // WHEN / THEN
        mockMvc.perform(get(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$[0].lastName").value(LAST_NAME));
    }

    @Test
    public void getStudentByIdSuccessful() throws Exception {
        // GIVEN
        CreateStudentDTO createStudentDTO = new CreateStudentDTO();
        createStudentDTO.setFirstName(FIRST_NAME);
        createStudentDTO.setLastName(LAST_NAME);

        String response = mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Reutilise l'id cree pour verifier le endpoint GET par id.
        Long studentId = objectMapper.readTree(response).get("id").asLong();

        // WHEN / THEN
        mockMvc.perform(get(URL + "/" + studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME));
    }

    @Test
    public void createStudentSuccessful() throws Exception {
        // GIVEN
        CreateStudentDTO createStudentDTO = new CreateStudentDTO();
        createStudentDTO.setFirstName(FIRST_NAME);
        createStudentDTO.setLastName(LAST_NAME);

        // WHEN / THEN
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(LAST_NAME));
    }

    @Test
    public void updateStudentSuccessful() throws Exception {
        // GIVEN
        CreateStudentDTO createStudentDTO = new CreateStudentDTO();
        createStudentDTO.setFirstName(FIRST_NAME);
        createStudentDTO.setLastName(LAST_NAME);

        String createResponse = mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Lit l'id depuis la creation pour tester la mise a jour d'un etudiant existant.
        Long studentId = objectMapper.readTree(createResponse).get("id").asLong();

        UpdateStudentDTO updateStudentDTO = new UpdateStudentDTO();
        updateStudentDTO.setFirstName(UPDATED_FIRST_NAME);
        updateStudentDTO.setLastName(UPDATED_LAST_NAME);

        // WHEN / THEN
        mockMvc.perform(put(URL + "/" + studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(updateStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentId))
                .andExpect(jsonPath("$.firstName").value(UPDATED_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(UPDATED_LAST_NAME));
    }

    @Test
    public void deleteStudentSuccessful() throws Exception {
        // GIVEN
        CreateStudentDTO createStudentDTO = new CreateStudentDTO();
        createStudentDTO.setFirstName(FIRST_NAME);
        createStudentDTO.setLastName(LAST_NAME);

        String createResponse = mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(createStudentDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Lit l'id depuis la creation pour que DELETE cible une entite persistante reelle.
        Long studentId = objectMapper.readTree(createResponse).get("id").asLong();

        // WHEN / THEN
        mockMvc.perform(delete(URL + "/" + studentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}