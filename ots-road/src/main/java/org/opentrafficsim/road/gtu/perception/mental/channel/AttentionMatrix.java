package org.opentrafficsim.road.gtu.perception.mental.channel;

import org.djutils.exceptions.Throw;

import Jama.Matrix;

/**
 * This class describes attention over channels, based on task demand per channel. Transition probabilities are based on demand
 * per channel, where drivers are assumed to keep perceiving the same channel by the demand of that channel alone. When total
 * demand is above 1, this means that the probability of switching to another channel is reduced. All transition probabilities
 * together result in an overall steady-state, which describes what fraction of time is spent on what channel.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttentionMatrix
{

    /** Mental task demand, i.e. desired fraction of time for perception, per channel. */
    private final double[] demand;

    /** Attention, i.e. fraction of time, per channel. */
    private double[] attention;

    /** Anticipation reliance per channel. */
    private double[] anticipationReliance;

    /**
     * Constructor which pre-calculates attention distribution assuming drivers are serial mono-taskers. The probability of
     * staying on a task is the task demand of the task, while the probability of switching is the complement. The task that
     * will then be switched to is selected by weighting them by their task demand. This creates a transition matrix in a Markov
     * chain. The steady-state of this Markov chain is the attention distribution.
     * @param demand level of mental task demand per channel.
     * @throws IllegalArgumentException when a demand value is below 0 or larger than or equal to 1
     */
    public AttentionMatrix(final double[] demand)
    {
        int n = demand.length;
        this.demand = new double[n];
        System.arraycopy(demand, 0, this.demand, 0, n);
        this.attention = new double[n];
        this.anticipationReliance = new double[n];

        double demandSum = 0.0;
        for (int i = 0; i < n; i++)
        {
            Throw.when(demand[i] < 0.0, IllegalArgumentException.class, "Demand must be >= 0");
            Throw.when(demand[i] >= 1.0, IllegalArgumentException.class, "Demand must be < 1");
            demandSum += demand[i];
        }
        if (demandSum == 0.0)
        {
            return;
        }
        if (demandSum <= 1.0)
        {
            this.attention = this.demand;
            return;
        }

        /*
         * matrix is the transition matrix in a Markov chain describing the probability of the next perception glance to be
         * towards channel j, given previous channel i.
         */
        Matrix matrix = new Matrix(n, n);
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                /*
                 * As we need a left-eigenvector (v*P = 1*v, v=eigenvector, P=matrix), we pre-transpose the data setting at (j,
                 * i). Note that left-eigenvectors are the transposed right-eigenvectors of matrix'. We do not care for how it
                 * is transposed.
                 */
                if (i == j)
                {
                    // probability to keep perceiving the same channel is the demand of the channel
                    matrix.set(j, i, demand[i]);
                }
                else if (demandSum > demand[i])
                {
                    /*
                     * The probability of a switch to another channel is 1 - TD(i). The relative probabilities of the other
                     * channels to be switched to, is proportional to the demand in these channels TD(j). These are normalized
                     * by the total sum of demand minus the demand of the channel we switch from (i.e. the sum of demand of the
                     * other channels), and scaled by the probability to switch 1 - TD(i).
                     */
                    matrix.set(j, i, (1 - demand[i]) * demand[j] / (demandSum - demand[i]));
                }
                else
                {
                    // there is only one channel with task demand, do not switch
                    matrix.set(j, i, 0.0);
                }
            }
        }

        /*
         * We use Jama to find the eigenvector of the transition matrix pertaining to the eigenvalue 1. Each Markov transition
         * matrix has an eigenvalue 1, and the pertaining eigenvector is the steady-state. This steady state is the distribution
         * of attention (in time) over the channels.
         */
        var ed = matrix.eig();
        double[] eigenValues = ed.getRealEigenvalues();
        // find the eigenvalue closest to 1 (these values are not highly exact)
        int eigenIndex = 0;
        double dMin = 1.0;
        for (int i = 0; i < n; i++)
        {
            double di = Math.abs(eigenValues[i] - 1.0);
            if (di < dMin)
            {
                dMin = di;
                eigenIndex = i;
            }
        }
        // obtain the eigenvector pertaining to the eigenvalue of 1
        double[][] v = ed.getV().getArray();
        double sumEigenVector = 0.0;
        for (int i = 0; i < n; i++)
        {
            this.attention[i] = v[i][eigenIndex];
            sumEigenVector += this.attention[i];
        }
        // normalize so it sums to 1
        for (int i = 0; i < n; i++)
        {
            this.attention[i] = this.attention[i] / sumEigenVector;
        }

        /*
         * Anticipation reliance per channel is the difference between the steady state (actual proportion of time we perceive a
         * channel) and the desired proportion of time to perceive a channel.
         */
        for (int i = 0; i < n; i++)
        {
            this.anticipationReliance[i] = this.demand[i] - this.attention[i];
        }
    }

    /**
     * Returns the fraction of time that is spent on channel <i>i</i>.
     * @param i index of channel.
     * @return fraction of time that is spent on channel <i>i</i>.
     */
    public double getAttention(final int i)
    {
        return this.attention[i];
    }

    /**
     * Returns the level of anticipation reliance for channel <i>i</i>. This is the fraction of time that is reduced from
     * perceiving channel <i>i</i>, relative to the desired fraction of time to perceive channel <i>i</i>.
     * @param i index of channel.
     * @return level of anticipation reliance for channel <i>i</i>.
     */
    public double getAnticipationReliance(final int i)
    {
        return this.anticipationReliance[i];
    }

    /**
     * Returns the deterioration of channel <i>i</i>. This is the anticipation reliance for channel <i>i</i>, divided by the
     * desired level of attention for channel <i>i</i>. This value is an indication of perception delay for the channel.
     * <p>
     * If demand for the channel is 0, this method returns 1.
     * @param i index of channel.
     * @return fraction of anticipation reliance over desired attention for channel <i>i</i>.
     */
    public double getDeterioration(final int i)
    {
        return this.demand[i] == 0.0 ? 1.0 : this.anticipationReliance[i] / this.demand[i];
    }

}
