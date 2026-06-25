package br.dev.irontech.seavault.reference.service;

import br.dev.irontech.seavault.common.error.BusinessException;
import br.dev.irontech.seavault.reference.dto.CategoryDto;
import br.dev.irontech.seavault.reference.dto.CourseDto;
import br.dev.irontech.seavault.reference.dto.GroupDto;
import br.dev.irontech.seavault.reference.dto.TypeDto;
import br.dev.irontech.seavault.reference.repo.ReferenceRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReferenceService {

    private final ReferenceRepository repo;

    public ReferenceService(ReferenceRepository repo) {
        this.repo = repo;
    }

    public List<GroupDto> groups() {
        return repo.listGroups().stream()
                .map(g -> new GroupDto(g.id, g.code, g.name, g.displayOrder))
                .toList();
    }

    public List<CategoryDto> categories(UUID groupId) {
        var entities = (groupId == null)
                ? repo.listAllCategories()
                : repo.listCategoriesByGroup(groupId);
        return entities.stream()
                .map(c -> new CategoryDto(c.id, c.groupId, c.code, c.name, c.progressionOrder))
                .toList();
    }

    public List<CourseDto> courses() {
        return repo.listCourses().stream()
                .map(c -> new CourseDto(c.id, c.code, c.name, c.workloadHours))
                .toList();
    }

    public List<TypeDto> types(String kind) {
        if (kind == null || kind.isBlank()) {
            throw new BusinessException("Parâmetro 'kind' é obrigatório");
        }
        return repo.listTypes(kind.toUpperCase()).stream()
                .map(t -> new TypeDto(t.id, t.kind, t.code, t.label))
                .toList();
    }
}
