package com.saicon.games.client.data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Stub DTO for decimal values - extracted from gameserver_v3
 */
public class DecimalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal value;
    private int scale;

    public DecimalDTO() {
    }

    public DecimalDTO(BigDecimal value) {
        this.value = value;
        this.scale = value != null ? value.scale() : 0;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
