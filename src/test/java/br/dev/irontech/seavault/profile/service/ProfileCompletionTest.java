package br.dev.irontech.seavault.profile.service;

import br.dev.irontech.seavault.profile.domain.Profile;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileCompletionTest {

    private Profile empty() {
        return new Profile(); // todos os campos core nulos
    }

    @Test
    void emptyProfileIsZero() {
        assertEquals(0, ProfileCompletion.percentOf(empty()));
    }

    @Test
    void allCoreFieldsFilledIs100() {
        Profile p = empty();
        p.cir = "12345";
        p.cpf = "000.000.000-00";
        p.rg = "MG-1234567";
        p.nationality = "Brasileira";
        p.phone = "+5531999999999";
        p.emergencyContact = "Maria 31988887777";
        p.categoryId = UUID.randomUUID();
        assertEquals(100, ProfileCompletion.percentOf(p));
    }

    @Test
    void blankStringDoesNotCount() {
        Profile p = empty();
        p.cir = "   ";
        assertEquals(0, ProfileCompletion.percentOf(p));
    }

    @Test
    void partialFillRoundsToNearestPercent() {
        Profile p = empty();
        p.cir = "12345";
        p.cpf = "000.000.000-00";
        p.rg = "MG-1234567";
        // 3 de 7 = 42.857... → 43
        assertEquals(43, ProfileCompletion.percentOf(p));
    }
}
