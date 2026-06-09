package com.duoc.gestion_guias;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiaDespacho {
    private String id;
    private String numeroGuia;
    private String transportista;
    private String fecha; 
    private String origen;
    private String destino;
    private String contenido;
    private String rutaArchivoLocal;
}