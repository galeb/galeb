package io.galeb.router.cluster;

import java.util.List;

public interface ExternalData {
    
    @SuppressWarnings("unused")
    enum Generic {
        NULL(new ExternalData() {}),
        EMPTY(new ExternalData() { public String getValue() { return ""; }}),
        UNDEF(new ExternalData() { public String getValue() { return "UNDEF"; }}),
        ZERO(new ExternalData() { public String getValue() { return "0"; }});

        private final ExternalData node;
        Generic(final ExternalData node) {
            this.node = node;
        }

        public ExternalData instance() {
            return node;
        }
    }

    default String getKey() { return null; }

    default void setKey(String key) {}

    default String getValue() { return null; }

    default void setValue(String value) {}

    default boolean isDir() { return false; }

    default void setDir(boolean dir) {}

    default List<ExternalData> getNodes() { return null; }

    default void setNodes(List<ExternalData> nodes) {}

}
