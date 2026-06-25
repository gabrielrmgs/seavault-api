package br.dev.irontech.seavault.files.dto;

/** Transporte interno do download (bytes + cabecalhos); nunca serializado como JSON. */
public record FileDownload(
        byte[] content,
        String contentType,
        String originalName
) {}
