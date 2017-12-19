package io.galeb.api.repository.custom;


import io.galeb.core.entity.RuleOrdered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("unused")
public interface RuleOrderedRepositoryCustom {

    RuleOrdered findOne(Long var1);

    Iterable<RuleOrdered> findAll(Sort var1);

    Page<RuleOrdered> findAll(Pageable var1);
}
