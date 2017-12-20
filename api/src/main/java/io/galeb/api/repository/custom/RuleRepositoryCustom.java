package io.galeb.api.repository.custom;


import io.galeb.core.entity.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface RuleRepositoryCustom {

    Rule findOne(Long var1);

    Iterable<Rule> findAll(Sort var1);

    Page<Rule> findAll(Pageable var1);

    void delete(Long id);
}
