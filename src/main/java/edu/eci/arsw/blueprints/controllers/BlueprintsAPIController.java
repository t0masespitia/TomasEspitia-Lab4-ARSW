package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Operaciones CRUD sobre planos arquitectónicos (blueprints)")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    @Operation(
            summary = "Listar todos los blueprints",
            description = "Retorna el conjunto completo de blueprints registrados, sin importar el autor."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Consulta exitosa")
    })
    @GetMapping
    public ResponseEntity<edu.eci.arsw.blueprints.dto.ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> all = services.getAllBlueprints();
        return ResponseEntity.ok(edu.eci.arsw.blueprints.dto.ApiResponse.of(200, "execute ok", all));
    }

    @Operation(
            summary = "Listar blueprints por autor",
            description = "Retorna todos los blueprints pertenecientes a un autor específico."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "No existen blueprints para el autor indicado")
    })
    @GetMapping("/{author}")
    public ResponseEntity<edu.eci.arsw.blueprints.dto.ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Nombre del autor", example = "john")
            @PathVariable String author) {
        try {
            Set<Blueprint> bps = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(edu.eci.arsw.blueprints.dto.ApiResponse.of(200, "execute ok", bps));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(404, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Obtener un blueprint específico",
            description = "Retorna un blueprint identificado por autor y nombre."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<edu.eci.arsw.blueprints.dto.ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Nombre del autor", example = "john")
            @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", example = "house")
            @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(edu.eci.arsw.blueprints.dto.ApiResponse.of(200, "execute ok", bp));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(404, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Crear un nuevo blueprint",
            description = "Registra un nuevo blueprint con su autor, nombre y lista inicial de puntos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Blueprint creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "El blueprint ya existe o los datos son inválidos")
    })
    @PostMapping
    public ResponseEntity<edu.eci.arsw.blueprints.dto.ApiResponse<Blueprint>> add(
            @Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(201, "blueprint created", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(400, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Agregar un punto a un blueprint existente",
            description = "Añade un nuevo punto (x, y) al final de la lista de puntos de un blueprint."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202", description = "Punto agregado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Blueprint no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<edu.eci.arsw.blueprints.dto.ApiResponse<Void>> addPoint(
            @Parameter(description = "Nombre del autor", example = "john")
            @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", example = "house")
            @PathVariable String bpname,
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(202, "point added", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(edu.eci.arsw.blueprints.dto.ApiResponse.of(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }
}