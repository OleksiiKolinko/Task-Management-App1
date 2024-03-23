package mate.academy.repository;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProviderString<T> {
    String getKey();

    Specification<T> getSpecificationString(String[] params);
}
