package mate.academy.repository;

public interface SpecificationProviderManager<T> {
    SpecificationProviderLong<T> getSpecificationProviderLong(String key);

    SpecificationProviderString<T> getSpecificationProviderString(String key);
}
