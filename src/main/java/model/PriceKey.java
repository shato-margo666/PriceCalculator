package model;

import java.util.Objects;

public class PriceKey {
    private final String productCode;
    private final int number;
    private final long department;

    public PriceKey(String productCode, int number, long department) {
        this.productCode = productCode;
        this.number = number;
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceKey priceKey = (PriceKey) o;
        return number == priceKey.number &&
                department == priceKey.department &&
                Objects.equals(productCode, priceKey.productCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productCode, number, department);
    }
}
