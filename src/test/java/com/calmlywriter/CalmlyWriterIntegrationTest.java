package com.calmlywriter;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class CalmlyWriterIntegrationTest {

  private static final String AUTH_COOKIE = "auth_token";

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void fullAuthAndDocumentFlow() throws Exception {
    String email = "writer@example.com";
    String password = "password123";

    MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"repeatPassword\":\"" + password + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(cookie().exists(AUTH_COOKIE))
        .andExpect(cookie().httpOnly(AUTH_COOKIE, true))
        .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
        .andReturn();

    MockCookie authCookie = authCookieFrom(registerResult);

    MvcResult createResult = mockMvc.perform(post("/api/documents")
            .cookie(authCookie))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Untitled"))
        .andReturn();

    long docId = extractId(createResult);

    mockMvc.perform(put("/api/documents/" + docId)
            .cookie(authCookie)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"My Story\",\"content\":\"Once upon a time.\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("My Story"))
        .andExpect(jsonPath("$.content").value("Once upon a time."));

    mockMvc.perform(get("/api/documents")
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("My Story"));

    mockMvc.perform(get("/api/documents/" + docId)
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("Once upon a time."));

    mockMvc.perform(get("/api/auth/me")
            .cookie(authCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));
  }

  @Test
  void duplicateRegistrationFails() throws Exception {
    String body = "{\"email\":\"dup@example.com\",\"password\":\"password123\",\"repeatPassword\":\"password123\"}";
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Email is already registered"));
  }

  @Test
  void invalidLoginFails() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"nobody@example.com\",\"password\":\"wrongpass1\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void userCannotAccessAnotherUsersDocument() throws Exception {
    MockCookie cookieA = registerAndGetCookie("usera@example.com");
    MockCookie cookieB = registerAndGetCookie("userb@example.com");

    MvcResult createResult = mockMvc.perform(post("/api/documents")
            .cookie(cookieA))
        .andExpect(status().isCreated())
        .andReturn();

    long docId = extractId(createResult);

    mockMvc.perform(get("/api/documents/" + docId)
            .cookie(cookieB))
        .andExpect(status().isNotFound());

    mockMvc.perform(delete("/api/documents/" + docId)
            .cookie(cookieB))
        .andExpect(status().isNotFound());
  }

  @Test
  void mismatchedPasswordsFail() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"mismatch@example.com\",\"password\":\"password123\",\"repeatPassword\":\"different123\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Passwords do not match"));
  }

  @Test
  void documentsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/documents"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutClearsAuthCookie() throws Exception {
    MockCookie authCookie = registerAndGetCookie("logout@example.com");

    mockMvc.perform(post("/api/auth/logout")
            .cookie(authCookie))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge(AUTH_COOKIE, 0));
  }

  private MockCookie registerAndGetCookie(String email) throws Exception {
    MvcResult result = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"repeatPassword\":\"password123\"}"))
        .andExpect(status().isCreated())
        .andReturn();
    return authCookieFrom(result);
  }

  private MockCookie authCookieFrom(MvcResult result) {
    String setCookie = result.getResponse().getHeader("Set-Cookie");
    String prefix = AUTH_COOKIE + "=";
    int start = setCookie.indexOf(prefix) + prefix.length();
    int end = setCookie.indexOf(';', start);
    String value = end == -1 ? setCookie.substring(start) : setCookie.substring(start, end);
    return new MockCookie(AUTH_COOKIE, value);
  }

  private long extractId(MvcResult result) throws Exception {
    Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    return id.longValue();
  }
}
