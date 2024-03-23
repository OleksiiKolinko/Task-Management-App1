package mate.academy.repository;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProviderLong<T> {
    String getKey();

    Specification<T> getSpecificationLong(Long[] params);

}
