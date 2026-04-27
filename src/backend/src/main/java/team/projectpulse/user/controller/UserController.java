package team.projectpulse.user.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.user.dto.InstructorSummary;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.dto.StudentUpdateDto;
import team.projectpulse.user.service.InstructorService;
import team.projectpulse.user.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final StudentService studentService;
    private final InstructorService instructorService;

    public UserController(StudentService studentService, InstructorService instructorService) {
        this.studentService = studentService;
        this.instructorService = instructorService;
    }

    // ── Students ─────────────────────────────────────────────────────────────

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<StudentSummary>> findAllStudents() {
        return Result.success(studentService.findAllStudents());
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<StudentDetail> getStudent(@PathVariable Long id) {
        return Result.success(studentService.findStudentById(id));
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success("Student deleted successfully.", null);
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public Result<Void> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto updateDto) {
        studentService.updateStudent(id, updateDto);
        return Result.success("Account updated successfully.", null);
    }

    // ── Instructors ───────────────────────────────────────────────────────────

    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<InstructorSummary>> findInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String teamName) {
        return Result.success(instructorService.searchInstructors(firstName, lastName, enabled, teamName));
    }

    @GetMapping("/instructors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<InstructorSummary> getInstructor(@PathVariable Long id) {
        return Result.success(instructorService.getInstructorById(id));
    }

    @PutMapping("/instructors/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> reactivateInstructor(@PathVariable Long id) {
        instructorService.reactivateInstructor(id);
        return Result.success("Instructor reactivated successfully.", null);
    }

    @PutMapping("/instructors/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deactivateInstructor(@PathVariable Long id) {
        instructorService.deactivateInstructor(id);
        return Result.success("Instructor deactivated successfully.", null);
    }
}
