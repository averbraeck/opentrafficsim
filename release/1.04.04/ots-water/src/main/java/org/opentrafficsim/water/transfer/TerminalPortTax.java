package org.opentrafficsim.water.transfer;

public class TerminalPortTax
{

    private double feePortPerFullMove;

    private double feePortPerEmptyMove;

    private double feePortPerShipDWT;

    private String feeStrategy;

    private boolean usePortFeePerFullMove;

    private boolean usePortFeePerEmptyMove;

    private boolean usePortFeePerShipDWT;

    public TerminalPortTax(double feePortPerFullMove, double feePortPerEmptyMove, double feePortPerDWT, String feeStrategy)
    {

        this.feePortPerFullMove = feePortPerFullMove;
        this.feePortPerEmptyMove = feePortPerEmptyMove;
        this.feePortPerShipDWT = feePortPerDWT;
        this.feeStrategy = feeStrategy;

        if ("F".equals(feeStrategy))
        {
            this.usePortFeePerFullMove = true;
            this.usePortFeePerEmptyMove = false;
            this.usePortFeePerShipDWT = false;

        }
        if ("A".equals(feeStrategy))
        {
            this.usePortFeePerFullMove = true;
            this.usePortFeePerEmptyMove = true;
            this.usePortFeePerShipDWT = false;

        }
        if ("S".equals(feeStrategy))
        {
            this.usePortFeePerFullMove = false;
            this.usePortFeePerEmptyMove = false;
            this.usePortFeePerShipDWT = true;

        }
    }

    public double getFeePortPerShipDWT()
    {

        return feePortPerShipDWT;
    }

    public double getFeePortPerFullMove()
    {
        return feePortPerFullMove;
    }

    public double getFeePortPerEmptyMove()
    {
        return feePortPerEmptyMove;
    }

    public boolean isUsePortFeePerEmptyMove()
    {
        return usePortFeePerEmptyMove;
    }

    public boolean isUsePortFeePerFullMove()
    {
        return usePortFeePerFullMove;
    }

    public boolean isUsePortFeePerShipDWT()
    {
        return usePortFeePerShipDWT;
    }

    public String getFeeStrategy()
    {
        return feeStrategy;
    }
}
