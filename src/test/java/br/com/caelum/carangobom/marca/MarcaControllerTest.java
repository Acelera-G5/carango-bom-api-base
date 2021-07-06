package br.com.caelum.carangobom.marca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.caelum.carangobom.domain.entity.Marca;
import br.com.caelum.carangobom.infra.controller.MarcaController;
import br.com.caelum.carangobom.infra.jpa.entity.MarcaJpa;
import br.com.caelum.carangobom.infra.jpa.repository.MarcaRepositoryJpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class MarcaControllerTest {

    private MarcaController marcaController;
    private UriComponentsBuilder uriBuilder;

    @Mock
    private MarcaRepositoryJpa marcaRepository;

    @BeforeEach
    public void configuraMock() {
        openMocks(this);

        marcaController = new MarcaController(marcaRepository);
        uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080");
    }

    @Test
    void deveRetornarListaQuandoHouverResultados() {
        List<Marca> marcas = List.of(
            new MarcaJpa(1L, "Audi"),
            new MarcaJpa(2L, "BMW"),
            new MarcaJpa(3L, "Fiat")
        );

        when(marcaRepository.findAllByOrderByNome())
            .thenReturn(marcas);

        List<Marca> resultado = marcaController.lista();
        assertEquals(marcas, resultado);
    }

    @Test
    void deveRetornarMarcaPeloId() {
        Marca audi = new MarcaJpa(1L, "Audi");

        when(marcaRepository.findById(1L))
            .thenReturn(Optional.of(audi));

        ResponseEntity<Marca> resposta = marcaController.id(1L);
        assertEquals(audi, resposta.getBody());
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    void deveRetornarNotFoundQuandoRecuperarMarcaComIdInexistente() {
        when(marcaRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ResponseEntity<Marca> resposta = marcaController.id(1L);
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    @Test
    void deveResponderCreatedELocationQuandoCadastrarMarca() {
        Marca nova = new MarcaJpa("Ferrari");

        when(marcaRepository.save(nova))
            .then(invocation -> {
                MarcaJpa marcaSalva = invocation.getArgument(0, MarcaJpa.class);
                marcaSalva.setId(1L);

                return marcaSalva;
            });

        ResponseEntity<Marca> resposta = marcaController.cadastra(nova, uriBuilder);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("http://localhost:8080/marcas/1", resposta.getHeaders().getLocation().toString());
    }

    @Test
    void deveAlterarNomeQuandoMarcaExistir() {
        MarcaJpa audi = new MarcaJpa(1L, "Audi");

        when(marcaRepository.findById(1L))
            .thenReturn(Optional.of(audi));

        ResponseEntity<Marca> resposta = marcaController.altera(1L, new MarcaJpa(1L, "NOVA Audi"));
        assertEquals(HttpStatus.OK, resposta.getStatusCode());

        Marca marcaAlterada = resposta.getBody();
        assertEquals("NOVA Audi", marcaAlterada.getNome());
    }

    @Test
    void naoDeveAlterarMarcaInexistente() {
        when(marcaRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ResponseEntity<Marca> resposta = marcaController.altera(1L, new MarcaJpa(1L, "NOVA Audi"));
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    @Test
    void deveDeletarMarcaExistente() {
        Marca audi = new MarcaJpa(1l, "Audi");

        when(marcaRepository.findById(1L))
            .thenReturn(Optional.of(audi));

        ResponseEntity<Marca> resposta = marcaController.deleta(1L);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(marcaRepository).delete(audi);
    }

    @Test
    void naoDeveDeletarMarcaInexistente() {
        when(marcaRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        ResponseEntity<Marca> resposta = marcaController.deleta(1L);
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());

        verify(marcaRepository, never()).delete(any());
    }

}