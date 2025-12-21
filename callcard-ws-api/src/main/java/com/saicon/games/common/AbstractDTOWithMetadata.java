package com.saicon.games.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for DTOs that support metadata - extracted from gameserver_v3
 */
public abstract class AbstractDTOWithMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> metadata;

    public AbstractDTOWithMetadata() {
        this.metadata = new HashMap<>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
}
