package team.projectpulse.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final StudentService studentService;

    public UserController(StudentService studentService) {
        this.studentService = studentService;
    }

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
}
