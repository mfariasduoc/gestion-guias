package com.duoc.gestion_guias;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GuiaService {

    private final List<GuiaDespacho> listaGuias = new ArrayList<>();
    private final String CARPETA_EFS = "./almacenamiento_efs/";

    private final AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket}")
    private String nombreBucket;

    public GuiaService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public GuiaDespacho crearGuia(GuiaDespacho guia) {
        guia.setId(UUID.randomUUID().toString());
        
        try {
            // 1. GUARDAR EN LOCAL
            File directorio = new File(CARPETA_EFS);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            String nombreArchivo = "guia_" + guia.getNumeroGuia() + ".txt";
            File archivoFisico = new File(directorio, nombreArchivo);

            FileWriter escritor = new FileWriter(archivoFisico);
            escritor.write("=== GUÍA DE DESPACHO ===\n");
            escritor.write("ID: " + guia.getId() + "\n");
            escritor.write("Número: " + guia.getNumeroGuia() + "\n");
            escritor.write("Transportista: " + guia.getTransportista() + "\n");
            escritor.write("Fecha: " + guia.getFecha() + "\n");
            escritor.write("Contenido: " + guia.getContenido() + "\n");
            escritor.close();

            guia.setRutaArchivoLocal(archivoFisico.getAbsolutePath());

            // 2. SUBIDA AUTOMÁTICA A AWS S3
            String anio = guia.getFecha().substring(0, 4);
            String rutaS3 = anio + "/" + guia.getTransportista().replaceAll(" ", "_") + "/" + nombreArchivo;

            // subir el archivo al bucket
            s3Client.putObject(nombreBucket, rutaS3, archivoFisico);

        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo local", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo a AWS S3. Verifica las llaves o el nombre del bucket.", e);
        }

        listaGuias.add(guia);
        return guia;
    }

    public List<GuiaDespacho> buscarPorFiltros(String transportista, String fecha) {
        List<GuiaDespacho> filtradas = new ArrayList<>();
        for (GuiaDespacho g : listaGuias) {
            if (g.getTransportista().equalsIgnoreCase(transportista) && g.getFecha().equals(fecha)) {
                filtradas.add(g);
            }
        }
        return filtradas;
    }

    public GuiaDespacho actualizarGuia(String id, GuiaDespacho datosNuevos) {
    for (GuiaDespacho g : listaGuias) {
        if (g.getId().equals(id)) {
            // 1. ACTUALIZAR ATRIBUTOS EN MEMORIA
            g.setTransportista(datosNuevos.getTransportista());
            g.setOrigen(datosNuevos.getOrigen());
            g.setDestino(datosNuevos.getDestino());
            g.setContenido(datosNuevos.getContenido());
            
            try {
                // 2. REESCRIBIR EL ARCHIVO LOCAL EN EFS CON LOS NUEVOS DATOS
                String nombreArchivo = "guia_" + g.getNumeroGuia() + ".txt";
                File directorio = new File(CARPETA_EFS);
                File archivoFisico = new File(directorio, nombreArchivo);

                FileWriter escritor = new FileWriter(archivoFisico);
                escritor.write("=== GUÍA DE DESPACHO (ACTUALIZADA) ===\n");
                escritor.write("ID: " + g.getId() + "\n");
                escritor.write("Número: " + g.getNumeroGuia() + "\n");
                escritor.write("Transportista: " + g.getTransportista() + "\n");
                escritor.write("Fecha: " + g.getFecha() + "\n");
                escritor.write("Contenido: " + g.getContenido() + "\n");
                escritor.close();

                // 3. SUBIR EL ARCHIVO REEMPLAZADO A AWS S3
                String anio = g.getFecha().substring(0, 4);
                String rutaS3 = anio + "/" + g.getTransportista().replaceAll(" ", "_") + "/" + nombreArchivo;
                
                s3Client.putObject(nombreBucket, rutaS3, archivoFisico);

            } catch (IOException e) {
                throw new RuntimeException("Error al actualizar el archivo local en EFS", e);
            } catch (Exception e) {
                throw new RuntimeException("Error al actualizar el archivo en AWS S3", e);
            }

            return g;
        }
    }
    throw new RuntimeException("No se encontró la guía con el ID: " + id);
}

    public boolean eliminarGuia(String id) {
        return listaGuias.removeIf(g -> g.getId().equals(id));
    }
    public List<GuiaDespacho> obtenerTodas() {
    return this.listaGuias;
}

public String descargarContenidoDesdeS3(String id) {
    for (GuiaDespacho g : listaGuias) {
        if (g.getId().equals(id)) {
            try {
                String nombreArchivo = "guia_" + g.getNumeroGuia() + ".txt";
                String anio = g.getFecha().substring(0, 4);
                String rutaS3 = anio + "/" + g.getTransportista().replaceAll(" ", "_") + "/" + nombreArchivo;

                // descarga el objeto de S3 y procesa su contenido como texto
                return com.amazonaws.util.IOUtils.toString(
                    s3Client.getObject(nombreBucket, rutaS3).getObjectContent()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error al descargar el archivo desde AWS S3", e);
            }
        }
    }
    throw new RuntimeException("No se encontró la guía para descargar desde S3 con el ID: " + id);
}
}