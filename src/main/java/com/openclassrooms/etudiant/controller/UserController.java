package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.LoginResponseDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = new User();
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setLogin(registerDTO.getLogin());
        user.setPassword(registerDTO.getPassword());
        userService.register(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        String jwtToken = userService.login(loginRequestDTO.getLogin(), loginRequestDTO.getPassword());
        return ResponseEntity.ok(new LoginResponseDTO(jwtToken));
    }


}
