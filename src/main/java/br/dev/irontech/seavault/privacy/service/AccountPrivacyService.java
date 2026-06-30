package br.dev.irontech.seavault.privacy.service;

import br.dev.irontech.seavault.auth.domain.User;
import br.dev.irontech.seavault.auth.domain.UserStatus;
import br.dev.irontech.seavault.auth.repo.RefreshTokenRepository;
import br.dev.irontech.seavault.auth.repo.UserRepository;
import br.dev.irontech.seavault.certificates.repo.CertificateRepository;
import br.dev.irontech.seavault.certificates.service.CertificateService;
import br.dev.irontech.seavault.companies.repo.CompanyRepository;
import br.dev.irontech.seavault.companies.service.CompanyService;
import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.courses.repo.CourseRepository;
import br.dev.irontech.seavault.courses.service.CourseService;
import br.dev.irontech.seavault.documents.repo.DocumentRepository;
import br.dev.irontech.seavault.documents.service.DocumentService;
import br.dev.irontech.seavault.files.repo.FileLinkRepository;
import br.dev.irontech.seavault.files.repo.FileRepository;
import br.dev.irontech.seavault.privacy.dto.AccountExport;
import br.dev.irontech.seavault.profile.repo.ProfileRepository;
import br.dev.irontech.seavault.vessels.repo.VesselRepository;
import br.dev.irontech.seavault.vessels.service.VesselService;
import br.dev.irontech.seavault.voyages.repo.VoyageRepository;
import br.dev.irontech.seavault.voyages.service.VoyageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class AccountPrivacyService {

    private static final Logger LOG = Logger.getLogger(AccountPrivacyService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileRepository profileRepository;
    private final DocumentRepository documentRepository;
    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final VesselRepository vesselRepository;
    private final CompanyRepository companyRepository;
    private final VoyageRepository voyageRepository;
    private final FileLinkRepository fileLinkRepository;
    private final FileRepository fileRepository;
    private final DocumentService documentService;
    private final CertificateService certificateService;
    private final CourseService courseService;
    private final VesselService vesselService;
    private final CompanyService companyService;
    private final VoyageService voyageService;

    public AccountPrivacyService(UserRepository userRepository,
                                 RefreshTokenRepository refreshTokenRepository,
                                 ProfileRepository profileRepository,
                                 DocumentRepository documentRepository,
                                 CertificateRepository certificateRepository,
                                 CourseRepository courseRepository,
                                 VesselRepository vesselRepository,
                                 CompanyRepository companyRepository,
                                 VoyageRepository voyageRepository,
                                 FileLinkRepository fileLinkRepository,
                                 FileRepository fileRepository,
                                 DocumentService documentService,
                                 CertificateService certificateService,
                                 CourseService courseService,
                                 VesselService vesselService,
                                 CompanyService companyService,
                                 VoyageService voyageService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.profileRepository = profileRepository;
        this.documentRepository = documentRepository;
        this.certificateRepository = certificateRepository;
        this.courseRepository = courseRepository;
        this.vesselRepository = vesselRepository;
        this.companyRepository = companyRepository;
        this.voyageRepository = voyageRepository;
        this.fileLinkRepository = fileLinkRepository;
        this.fileRepository = fileRepository;
        this.documentService = documentService;
        this.certificateService = certificateService;
        this.courseService = courseService;
        this.vesselService = vesselService;
        this.companyService = companyService;
        this.voyageService = voyageService;
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = requireActiveUser(userId);
        Instant now = Instant.now();
        user.email = "deleted-" + userId + "@anonymized.invalid";
        user.name = "Usuario removido";
        user.status = UserStatus.INATIVO;
        user.deletedAt = now;
        profileRepository.softDeleteByUser(userId, now);
        documentRepository.softDeleteByUser(userId, now);
        certificateRepository.softDeleteByUser(userId, now);
        courseRepository.softDeleteByUser(userId, now);
        vesselRepository.softDeleteByUser(userId, now);
        companyRepository.softDeleteByUser(userId, now);
        voyageRepository.softDeleteByUser(userId, now);
        fileLinkRepository.softDeleteByFileOwner(userId, now);
        fileRepository.softDeleteByUser(userId, now);
        refreshTokenRepository.revokeActiveByUser(userId, now);
        LOG.infof("Conta %s anonimizada e desativada (LGPD art. 18)", userId);
    }

    public AccountExport exportData(UUID userId) {
        User user = requireActiveUser(userId);
        AccountExport.AccountInfo info = new AccountExport.AccountInfo(
                user.name,
                user.email,
                user.plan != null ? user.plan.name() : null,
                user.createdAt,
                user.termsAcceptedAt);
        Object profile = profileRepository.findActiveByUserId(userId).orElse(null);
        return new AccountExport(info, profile,
                documentService.listAllForUser(userId),
                certificateService.listAllForUser(userId),
                courseService.listAllForUser(userId),
                vesselService.listAllForUser(userId),
                companyService.listAllForUser(userId),
                voyageService.listAllForUser(userId),
                Instant.now());
    }

    private User requireActiveUser(UUID userId) {
        User user = userRepository.findById(userId);
        if (user == null || user.deletedAt != null) {
            throw new NotFoundException("Conta nao encontrada");
        }
        return user;
    }
}
