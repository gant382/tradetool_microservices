package com.saicon.multiplayer.dto;

import java.io.Serializable;

/**
 * Stub DTO for key-value pairs - extracted from gameserver_v3
 */
public class KeyValueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String value;

    public KeyValueDTO() {
    }

    public KeyValueDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
