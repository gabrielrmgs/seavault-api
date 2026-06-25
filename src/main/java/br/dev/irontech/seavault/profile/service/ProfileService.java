package br.dev.irontech.seavault.profile.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.profile.domain.Profile;
import br.dev.irontech.seavault.profile.dto.ProfileResponse;
import br.dev.irontech.seavault.profile.dto.ProfileUpdateRequest;
import br.dev.irontech.seavault.profile.repo.ProfileRepository;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ReferenceRepository referenceRepository;

    public ProfileService(ProfileRepository profileRepository,
                          ReferenceRepository referenceRepository) {
        this.profileRepository = profileRepository;
        this.referenceRepository = referenceRepository;
    }

    @Transactional
    public ProfileResponse getOrCreate(UUID userId) {
        Profile profile = profileRepository.findActiveByUserId(userId)
                .orElseGet(() -> createEmpty(userId));
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse update(UUID userId, ProfileUpdateRequest req) {
        Profile profile = profileRepository.findActiveByUserId(userId)
                .orElseGet(() -> createEmpty(userId));

        validateCategory(req.categoryId());
        validateCategory(req.targetCategoryId());

        profile.cir = req.cir();
        profile.cpf = req.cpf();
        profile.rg = req.rg();
        profile.nationality = req.nationality();
        profile.phone = req.phone();
        profile.emergencyContact = req.emergencyContact();
        profile.categoryId = req.categoryId();
        profile.targetCategoryId = req.targetCategoryId();
        profile.completionPercent = ProfileCompletion.percentOf(profile);

        return toResponse(profile);
    }

    private Profile createEmpty(UUID userId) {
        Profile profile = new Profile();
        profile.userId = userId;
        profile.completionPercent = 0;
        profileRepository.persist(profile);
        return profile;
    }

    private void validateCategory(UUID categoryId) {
        if (categoryId != null && referenceRepository.findCategoryById(categoryId).isEmpty()) {
            throw new NotFoundException("Categoria não encontrada: " + categoryId);
        }
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(p.id, p.userId, p.cir, p.cpf, p.rg, p.nationality,
                p.phone, p.emergencyContact, p.categoryId, p.targetCategoryId, p.completionPercent);
    }
}
