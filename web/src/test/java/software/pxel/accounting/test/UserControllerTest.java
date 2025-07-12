package software.pxel.accounting.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.pxel.accounting.dto.PageCacheDto;
import software.pxel.accounting.dto.email.EmailReadDto;
import software.pxel.accounting.dto.phone.PhoneReadDto;
import software.pxel.accounting.dto.user.UserReadDto;
import software.pxel.accounting.dto.user.UserSearchDto;
import software.pxel.accounting.service.UserService;

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
        UserReadDto user = new UserReadDto(1L, "Ashley", "01.07.1990",
                Set.of(new EmailReadDto("ashleykkk@gmail.com")), Set.of(new PhoneReadDto("72302978433")));
        PageCacheDto<UserReadDto> page = new PageCacheDto<>(List.of(user), 1, 10);

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
        UserReadDto user = new UserReadDto(1L, "Ashley", "01.07.1990",
                Set.of(new EmailReadDto("ashleykkk@gmail.com")), Set.of(new PhoneReadDto("72302978433")));
        PageCacheDto<UserReadDto> page = new PageCacheDto<>(List.of(user), 0, 10);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"));
    }

    @Test
    void searchUsers_FiltersByEmail() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", "01.07.1990",
                Set.of(new EmailReadDto("ashleykkk@gmail.com")), Set.of(new PhoneReadDto("72302978433")));
        PageCacheDto<UserReadDto> page = new PageCacheDto<>(List.of(user), 0, 10);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("emailData.value", "ashleykkk@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].emailData.value").value("ashleykkk@gmail.com"));
    }

    @Test
    void searchUsers_FiltersByDateOfBirth() throws Exception {
        LocalDate date = LocalDate.of(2000, 2, 27);
        UserReadDto user = new UserReadDto(1L, "Ashley", "01.07.1990",
                Set.of(new EmailReadDto("ashleykkk@gmail.com")), Set.of(new PhoneReadDto("72302978433")));
        PageCacheDto<UserReadDto> page = new PageCacheDto<>(List.of(user), 0, 10);

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
        PageCacheDto<UserReadDto> emptyPage = new PageCacheDto<>(List.of(), 0, 10);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void searchUsers_InvalidPagination_ReturnsFirstPageByDefault() throws Exception {
        UserReadDto user = new UserReadDto(1L, "Ashley", "01.07.1990",
                Set.of(new EmailReadDto("ashleykkk@gmail.com")), Set.of(new PhoneReadDto("72302978433")));
        PageCacheDto<UserReadDto> page = new PageCacheDto<>(List.of(user), 0, 10);

        when(userService.searchUsers(any(UserSearchDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("page", "-1")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ashley"));
    }
}