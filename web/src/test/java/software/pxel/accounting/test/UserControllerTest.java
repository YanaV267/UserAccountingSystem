package software.pxel.accounting.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void searchUsers_ReturnsPaginatedResults() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", LocalDate.now(), Set.of("ashleykkk@gmail.com"), Set.of("72302978433"), BigDecimal.ZERO);
        Page<UserReadDto> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test User"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchUsers_FiltersByName() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", LocalDate.now(), Set.of("ashleykkk@gmail.com"), Set.of("72302978433"), BigDecimal.ZERO);
        Page<UserReadDto> page = new PageImpl<>(List.of(user));

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"));
    }

    @Test
    void searchUsers_FiltersByEmail() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", LocalDate.now(), Set.of("ashleykkk@gmail.com"), Set.of("72302978433"), BigDecimal.ZERO);
        Page<UserReadDto> page = new PageImpl<>(List.of(user));

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("email", "ashleykkk@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("ashleykkk@gmail.com"));
    }

    @Test
    void searchUsers_FiltersByDateOfBirth() throws Exception {
        LocalDate date = LocalDate.of(2000, 2, 27);
        UserReadDto user = new UserReadDto(1L, "Ashley", date, Set.of("ashleykkk@gmail.com"), Set.of("72302978433"), BigDecimal.ZERO);
        Page<UserReadDto> page = new PageImpl<>(List.of(user));

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("dateOfBirth", "2000-02-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dateOfBirth").value("2000-02-27"));
    }

    @Test
    void searchUsers_InvalidDateFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("dateOfBirth", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchUsers_EmptyResult_ReturnsEmptyPage() throws Exception {
        Page<UserReadDto> emptyPage = new PageImpl<>(List.of());

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void searchUsers_InvalidPagination_ReturnsFirstPageByDefault() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", LocalDate.now(), Set.of("ashleykkk@gmail.com"), Set.of("72302978433"), BigDecimal.ZERO);
        Page<UserReadDto> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("page", "-1")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ashley"));
    }
}