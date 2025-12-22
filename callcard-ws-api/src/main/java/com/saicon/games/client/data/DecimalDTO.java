package com.saicon.games.client.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Stub DTO for decimal values - extracted from gameserver_v3
 */
public class DecimalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long value;
    private int scale;

    public DecimalDTO() {
    }

    public DecimalDTO(BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            this.scale = bigDecimal.scale();
            this.value = bigDecimal.unscaledValue().longValue();
        }
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    /**
     * Convert back to BigDecimal
     */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(BigInteger.valueOf(value), scale);
    }
}
