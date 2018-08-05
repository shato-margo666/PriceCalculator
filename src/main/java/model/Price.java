package model;

import java.util.Date;

public class Price {

    private final long id;
    private final String productCode;
    private final int number;
    private final long department;
    /**
     * Begin of interval, inclusive
     */
    private Date begin;
    /**
     * End of interval, exclusive
     */
    private Date end;
    private final float value;

    public Price(long id, String productCode, int number, long department, Date begin, Date end, float value) {
        this.id = id;
        this.productCode = productCode;
        this.number = number;
        this.department = department;
        this.begin = begin;
        this.end = end;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getNumber() {
        return number;
    }

    public long getDepartment() {
        return department;
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }

    public float getValue() {
        return value;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}


