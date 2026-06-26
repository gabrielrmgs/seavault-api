package br.dev.irontech.seavault.common.scan;

import java.time.LocalDate;
import java.util.UUID;

/** Projecao neutra de uma entidade que pode gerar alerta. Source-agnostica. */
public record DueItem(UUID userId, UUID sourceId, LocalDate dueDate, String title) {}
