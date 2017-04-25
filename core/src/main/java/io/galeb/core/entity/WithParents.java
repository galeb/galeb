package io.galeb.core.entity;

import java.util.Set;

public interface WithParents<T extends AbstractEntity<?>> {

    Set<T> getParents();

}
