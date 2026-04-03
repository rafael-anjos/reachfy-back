package com.project.social.controller;

import com.project.social.dto.LoginRequestDTO;
import com.project.social.dto.RegisterRequestDTO;
import com.project.social.infra.security.TokenService;
import com.project.social.models.Usuario;
import com.project.social.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body, HttpServletResponse response) {
        Usuario usuario = this.usuarioRepository
                .findByEmail(body.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais Invalidos"
                ));
        if(!passwordEncoder.matches(body.senha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = tokenService.generateToken(usuario);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        Optional<Usuario> usuario = this.usuarioRepository.findByEmail(body.email());

        if(usuario.isEmpty()) {
            Usuario newUsuario = new Usuario();
            newUsuario.setSenha(passwordEncoder.encode(body.senha()));
            newUsuario.setEmail(body.email());
            newUsuario.setNome(body.nome());
            this.usuarioRepository.save(newUsuario);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        return ResponseEntity.badRequest().build();
    }
}
