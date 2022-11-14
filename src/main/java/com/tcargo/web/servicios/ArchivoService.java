package com.tcargo.web.servicios;

import com.tcargo.web.errores.WebException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static com.tcargo.web.utiles.Textos.UPLOADS_FOLDER;

@Service
public class ArchivoService {

    @Value("${ruta_archivos}")
    private String archivos;

    Log log = LogFactory.getLog(getClass());

    public Resource cargar(String filename) throws WebException {
        Path ruta = ruta(filename);
        try {
            Resource recurso = new UrlResource(ruta.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new WebException("Error al cargar el archivo: " + ruta.toString());
            }
            return recurso;
        } catch (MalformedURLException e) {
            throw new WebException("Error al cargar el archivo. " + e.getMessage());
        }
    }

    public String copiar(MultipartFile archivo) throws WebException {
        try {
            Path ruta = ruta(archivo.getOriginalFilename());

            if (Files.exists(ruta)) {
                return editar(archivo);
            }

            String nombre = UUID.randomUUID().toString();
            if (archivo.getOriginalFilename() != null) {
                nombre += archivo.getOriginalFilename().replace(" ", "_").replace("#", "_").replace("*", "_");
            }

            Path raiz = ruta(nombre);
            Files.copy(archivo.getInputStream(), raiz);
            return nombre;
        } catch (IOException e) {
            log.error("Error al editar el archivo. " + e.getMessage());
            throw new WebException("Error al editar el archivo.");
        }
    }

    public String editar(MultipartFile archivo) throws WebException {
        try {
            Path ruta = ruta(archivo.getOriginalFilename());
            Files.copy(archivo.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);
            return archivo.getOriginalFilename();
        } catch (IOException e) {
            log.error("Error al editar el archivo. " + e.getMessage());
            throw new WebException("Error al editar el archivo.");
        }
    }

    public boolean eliminar(String nombre) {
        Path raiz = ruta(nombre);
        try {
            Files.deleteIfExists(raiz);
            return true;
        } catch (IOException e) {
            log.error("Error al eliminar el archivo. " + e.getMessage());
            return false;
        }
    }

    public void eliminar(List<String> nombres) {
        for (String nombre : nombres) {
            eliminar(nombre);
        }
    }

    public Path ruta(String nombre) {
        return Paths.get(archivos).resolve(nombre).toAbsolutePath();
    }

    public void eliminar() {
        FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
    }

}
