package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.CreateStudentDTO;
import com.openclassrooms.etudiant.dto.StudentDTO;
import com.openclassrooms.etudiant.dto.UpdateStudentDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class StudentServiceTest {

    private static final Long ID = 1L;
    private static final String FIRST_NAME = "Tom";
    private static final String LAST_NAME = "Sawyer";
    private static final String UPDATED_FIRST_NAME = "Tom-Updated";
    private static final String UPDATED_LAST_NAME = "Sawyer-Updated";

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    public void test_get_all_students() {
        // GIVEN
        Student student = new Student();
        student.setId(ID);
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        when(studentRepository.findAll()).thenReturn(List.of(student));

        // WHEN
        List<StudentDTO> result = studentService.getAllStudents();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ID);
        assertThat(result.get(0).getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.get(0).getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    public void test_get_student_by_id() {
        // GIVEN
        Student student = new Student();
        student.setId(ID);
        student.setFirstName(FIRST_NAME);
        student.setLastName(LAST_NAME);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        when(studentRepository.findById(ID)).thenReturn(Optional.of(student));

        // WHEN
        StudentDTO result = studentService.getStudentById(ID);

        // THEN
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(LAST_NAME);
    }

    @Test
    public void test_create_student() {
        // GIVEN
        CreateStudentDTO dto = new CreateStudentDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);

        Student savedStudent = new Student();
        savedStudent.setId(ID);
        savedStudent.setFirstName(FIRST_NAME);
        savedStudent.setLastName(LAST_NAME);
        savedStudent.setCreatedAt(LocalDateTime.now());
        savedStudent.setUpdatedAt(LocalDateTime.now());

        // Save retourne une entite avec metadonnees generees (id/horodatages).
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        // WHEN
        StudentDTO result = studentService.createStudent(dto);

        // THEN
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(LAST_NAME);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    public void test_update_student() {
        // GIVEN
        UpdateStudentDTO dto = new UpdateStudentDTO();
        dto.setFirstName(UPDATED_FIRST_NAME);
        dto.setLastName(UPDATED_LAST_NAME);

        Student existingStudent = new Student();
        existingStudent.setId(ID);
        existingStudent.setFirstName(FIRST_NAME);
        existingStudent.setLastName(LAST_NAME);
        existingStudent.setCreatedAt(LocalDateTime.now());
        existingStudent.setUpdatedAt(LocalDateTime.now());

        Student updatedStudent = new Student();
        updatedStudent.setId(ID);
        updatedStudent.setFirstName(UPDATED_FIRST_NAME);
        updatedStudent.setLastName(UPDATED_LAST_NAME);
        updatedStudent.setCreatedAt(existingStudent.getCreatedAt());
        updatedStudent.setUpdatedAt(LocalDateTime.now());

        when(studentRepository.findById(ID)).thenReturn(Optional.of(existingStudent));
        // Le service modifie l'entite chargee avant de la sauvegarder.
        when(studentRepository.save(existingStudent)).thenReturn(updatedStudent);

        // WHEN
        StudentDTO result = studentService.updateStudent(ID, dto);

        // THEN
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(result.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        verify(studentRepository).findById(ID);
        verify(studentRepository).save(existingStudent);
    }

    @Test
    public void test_delete_student() {
        // GIVEN
        when(studentRepository.existsById(ID)).thenReturn(true);

        // WHEN
        studentService.deleteStudent(ID);

        // THEN
        verify(studentRepository).existsById(ID);
        verify(studentRepository).deleteById(ID);
    }

    @Test
    public void test_get_student_by_id_not_found_throws_runtime_exception() {
        // GIVEN
        when(studentRepository.findById(ID)).thenReturn(Optional.empty());

        // WHEN / THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
        () -> studentService.getStudentById(ID));

        assertThat(exception.getMessage()).isEqualTo("Student not found");
    }

    @Test
    public void test_update_student_not_found_throws_runtime_exception() {
        // GIVEN
        UpdateStudentDTO dto = new UpdateStudentDTO();
        dto.setFirstName(UPDATED_FIRST_NAME);
        dto.setLastName(UPDATED_LAST_NAME);

        when(studentRepository.findById(ID)).thenReturn(Optional.empty());

        // WHEN / THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> studentService.updateStudent(ID, dto));

        assertThat(exception.getMessage()).isEqualTo("Student not found");
    }

    @Test
    public void test_delete_student_not_found_throws_runtime_exception() {
        // GIVEN
        when(studentRepository.existsById(ID)).thenReturn(false);

        // WHEN / THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> studentService.deleteStudent(ID));

        assertThat(exception.getMessage()).isEqualTo("Student not found");
    }
}