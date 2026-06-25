package br.dev.irontech.seavault.profile.service;

import br.dev.irontech.seavault.profile.domain.Profile;

/** Cálculo puro do percentual de completude do perfil (sem dependência de banco). */
public final class ProfileCompletion {

    private static final int CORE_FIELDS = 7;

    private ProfileCompletion() {}

    public static int percentOf(Profile p) {
        int filled = 0;
        if (hasText(p.cir)) filled++;
        if (hasText(p.cpf)) filled++;
        if (hasText(p.rg)) filled++;
        if (hasText(p.nationality)) filled++;
        if (hasText(p.phone)) filled++;
        if (hasText(p.emergencyContact)) filled++;
        if (p.categoryId != null) filled++;
        return (int) Math.round((double) filled / CORE_FIELDS * 100);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
