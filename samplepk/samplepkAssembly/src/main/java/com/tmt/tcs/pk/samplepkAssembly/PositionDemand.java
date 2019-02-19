package com.tmt.tcs.pk.samplepkAssembly;

public class PositionDemand {
    public PositionDemand(double az, double el) {
        this.az = az;
        this.el = el;
    }

    public double getAz() {
        return az;
    }

    public void setAz(double az) {
        this.az = az;
    }

    public double getEl() {
        return el;
    }

    public void setEl(double el) {
        this.el = el;
    }

    private double az;
    private double el;

    @Override
    public String toString() {
        return "PositionDemand{" +
                "az=" + az +
                ", el=" + el +
                '}';
    }
}
