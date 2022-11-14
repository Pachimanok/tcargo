package com.tcargo.web.modelos;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class Base64MultipartFile implements MultipartFile {

    private String base64;
    private byte[] contenido;
    private String originalFileName;

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    @Override
    @NonNull
    public String getName() {
        return getOriginalFilename();
    }

    @Override
    @NonNull
    public String getOriginalFilename() {
        return originalFileName;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return contenido == null || contenido.length == 0;
    }

    @Override
    public long getSize() {
        return contenido.length;
    }

    @Override
    @NonNull
    public byte[] getBytes() {
        return getContenido();
    }

    @Override
    @NonNull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(contenido);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        try (FileOutputStream archivo = new FileOutputStream(dest)) {
            archivo.write(contenido);
        }
    }
}
