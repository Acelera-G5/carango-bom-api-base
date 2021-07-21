package br.com.caelum.carangobom.infra.controller;

import br.com.caelum.carangobom.domain.entity.Marca;
import br.com.caelum.carangobom.domain.entity.exception.NotFoundException;
import br.com.caelum.carangobom.domain.service.MarcaService;
import br.com.caelum.carangobom.infra.controller.request.CreateMarcaRequest;
import br.com.caelum.carangobom.infra.controller.response.MarcaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "marcas")
public class MarcaController {

    private MarcaService marcaService;

    @Autowired
    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping
    @Transactional
    @Cacheable(value = "brands")
    public List<MarcaResponse> lista() {
        return marcaService
        		.findAllByOrderByNome()
        		.stream()
        		.map(MarcaResponse::new)
        		.collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponse> getMarcaById(@PathVariable Long id) {
        Optional<Marca> marca = marcaService.findById(id);
        if (marca.isPresent()) {
            return ResponseEntity.ok(new MarcaResponse(marca.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Transactional
    @CacheEvict(cacheNames = "brands", allEntries = true)
    public ResponseEntity<MarcaResponse> cadastra(@Valid @RequestBody CreateMarcaRequest marcaForm, UriComponentsBuilder uriBuilder) {
    	Marca marca = marcaService.create(marcaForm.toMarcaJpa());
        URI uri = uriBuilder.path("/marcas/{id}").buildAndExpand(marca.getId()).toUri();
        return ResponseEntity.created(uri).body(new MarcaResponse(marca));
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(cacheNames = "brands", allEntries = true)
    public ResponseEntity<MarcaResponse> altera(@PathVariable Long id, @Valid @RequestBody CreateMarcaRequest marcaForm) {
        try {
			Marca updatedMarca =this.marcaService.update(marcaForm.toMarcaJpa(), id);
			return ResponseEntity.ok(new MarcaResponse(updatedMarca));
		} catch (NotFoundException e) {
			return ResponseEntity.notFound().build();
		}
    }

    @DeleteMapping("/{id}")
    @Transactional
    @CacheEvict(cacheNames = "brands", allEntries = true)
    public ResponseEntity<Void> deleta(@PathVariable Long id) {
        try {
			marcaService.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (NotFoundException e) {
			return ResponseEntity.notFound().build();
		}
    }
}