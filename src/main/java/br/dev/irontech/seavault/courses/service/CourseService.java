package br.dev.irontech.seavault.courses.service;

import br.dev.irontech.seavault.common.error.NotFoundException;
import br.dev.irontech.seavault.common.page.PageRequest;
import br.dev.irontech.seavault.common.page.PageResponse;
import br.dev.irontech.seavault.courses.domain.Course;
import br.dev.irontech.seavault.courses.domain.CourseStatus;
import br.dev.irontech.seavault.courses.dto.CourseRequest;
import br.dev.irontech.seavault.courses.dto.CourseResponse;
import br.dev.irontech.seavault.courses.repo.CourseRepository;
import br.dev.irontech.seavault.files.domain.OwnerType;
import br.dev.irontech.seavault.files.dto.FileResponse;
import br.dev.irontech.seavault.files.service.FileService;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CourseService {

    private final CourseRepository courseRepository;
    private final ReferenceRepository referenceRepository;
    private final FileService fileService;

    public CourseService(CourseRepository courseRepository,
                         ReferenceRepository referenceRepository,
                         FileService fileService) {
        this.courseRepository = courseRepository;
        this.referenceRepository = referenceRepository;
        this.fileService = fileService;
    }

    @Transactional
    public CourseResponse create(UUID userId, CourseRequest req) {
        validateCatalog(req.catalogCourseId());
        Course c = new Course();
        c.userId = userId;
        apply(c, req);
        courseRepository.persist(c);
        return toResponse(c);
    }

    public CourseResponse get(UUID userId, UUID id) {
        return toResponse(requireOwned(userId, id));
    }

    public PageResponse<CourseResponse> list(UUID userId, PageRequest page) {
        List<CourseResponse> content = courseRepository.listActiveByUser(userId, page).stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, page, courseRepository.countActiveByUser(userId));
    }

    @Transactional
    public CourseResponse update(UUID userId, UUID id, CourseRequest req) {
        Course c = requireOwned(userId, id);
        validateCatalog(req.catalogCourseId());
        apply(c, req);
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Course c = requireOwned(userId, id);
        c.deletedAt = Instant.now();
        fileService.unlinkAll(OwnerType.COURSE, id);
    }

    @Transactional
    public void attachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.link(userId, fileId, OwnerType.COURSE, id);
    }

    @Transactional
    public void detachFile(UUID userId, UUID id, UUID fileId) {
        requireOwned(userId, id);
        fileService.unlink(userId, fileId, OwnerType.COURSE, id);
    }

    public List<FileResponse> listFiles(UUID userId, UUID id) {
        requireOwned(userId, id);
        return fileService.filesForOwner(userId, OwnerType.COURSE, id);
    }

    private void apply(Course c, CourseRequest req) {
        c.name = req.name();
        c.catalogCourseId = req.catalogCourseId();
        c.institution = req.institution();
        c.modality = req.modality();
        c.workloadHours = req.workloadHours();
        c.startDate = req.startDate();
        c.completionDate = req.completionDate();
        c.status = req.status() == null ? CourseStatus.PLANEJADO : req.status();
        c.notes = req.notes();
    }

    private void validateCatalog(UUID catalogCourseId) {
        if (catalogCourseId != null && referenceRepository.findCourseById(catalogCourseId).isEmpty()) {
            throw new NotFoundException("Curso de catalogo nao encontrado: " + catalogCourseId);
        }
    }

    private Course requireOwned(UUID userId, UUID id) {
        return courseRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new NotFoundException("Curso nao encontrado: " + id));
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.id, c.name, c.catalogCourseId, c.institution, c.modality,
                c.workloadHours, c.startDate, c.completionDate, c.status, c.notes, c.createdAt, c.updatedAt);
    }
}
