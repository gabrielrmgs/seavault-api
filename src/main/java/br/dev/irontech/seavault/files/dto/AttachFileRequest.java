package br.dev.irontech.seavault.files.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttachFileRequest(@NotNull UUID fileId) {}
