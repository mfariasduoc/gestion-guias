package com.duoc.gestion_guias;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    private final GuiaService guiaService;

    public GuiaController(GuiaService guiaService) {
        this.guiaService = guiaService;
    }

    @PostMapping
    public GuiaDespacho crearGuia(@RequestBody GuiaDespacho guia) {
        return guiaService.crearGuia(guia);
    }

    @GetMapping
    public List<GuiaDespacho> obtenerTodas() {
        return guiaService.obtenerTodas();
    }
    // Endpoint para BUSCAR por transportista y fecha (GET http://localhost:8080/api/guias/buscar)
    @GetMapping("/buscar")
    public List<GuiaDespacho> buscar(@RequestParam String transportista, @RequestParam String fecha) {
        return guiaService.buscarPorFiltros(transportista, fecha);
    }

    // Endpoint para ACTUALIZAR (PUT http://localhost:8080/api/guias/{id})
    @PutMapping("/{id}")
    public GuiaDespacho actualizar(@PathVariable String id, @RequestBody GuiaDespacho guia) {
        return guiaService.actualizarGuia(id, guia);
    }

    // Endpoint para ELIMINAR (DELETE http://localhost:8080/api/guias/{id})
    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable String id) {
        boolean eliminado = guiaService.eliminarGuia(id);
        if (eliminado) {
            return "Guía eliminada correctamente.";
        } else {
            return "No se pudo eliminar la guía.";
        }
    }
}