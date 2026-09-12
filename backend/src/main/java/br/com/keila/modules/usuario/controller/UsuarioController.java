package br.com.keila.modules.usuario.controller;

import br.com.keila.modules.usuario.dto.AtualizarUsuarioRequest;
import br.com.keila.modules.usuario.dto.CriarUsuarioRequest;
import br.com.keila.modules.usuario.dto.UsuarioResponse;
import br.com.keila.modules.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Restrito a ADMIN — ver regra em SecurityConfig (`/api/v1/usuarios/**`). */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<UsuarioResponse> alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return ResponseEntity.ok(usuarioService.alterarAtivo(id, valor));
    }
}
