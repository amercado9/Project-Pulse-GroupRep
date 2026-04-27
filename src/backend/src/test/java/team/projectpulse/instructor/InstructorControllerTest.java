package team.projectpulse.instructor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.common.Result;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = team.projectpulse.ProjectPulseApplication.class)
@AutoConfigureMockMvc
public class InstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstructorService instructorService;

    @Test
    @WithMockUser(roles = "admin")
    public void testFindInstructorById_Success() throws Exception {
        // Given
        InstructorDTO instructorDTO = new InstructorDTO(
                1L, "John", "Doe", "john.doe@example.com", true, "instructor",
                Map.of("Section A", Collections.singletonList(new TeamBriefDTO(1L, "Team Alpha")))
        );
        given(instructorService.getInstructorDetails(anyLong())).willReturn(instructorDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/instructors/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.supervisedTeamsBySection['Section A'][0].teamName").value("Team Alpha"));
    }
}
