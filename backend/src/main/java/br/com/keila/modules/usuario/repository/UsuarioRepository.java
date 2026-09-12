package br.com.keila.modules.usuario.repository;

import br.com.keila.modules.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Usuario> findAllByOrderByNomeAsc();

    /** Usado no login por PIN: candidatos ativos com PIN cadastrado, comparados via BCrypt em memória. */
    List<Usuario> findByAtivoTrueAndPinHashIsNotNull();
}
