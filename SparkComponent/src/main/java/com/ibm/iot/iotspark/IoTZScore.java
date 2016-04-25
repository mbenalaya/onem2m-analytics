/**
 *****************************************************************************
 Copyright (c) 2016 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Jenny Wang - Initial Contribution
 Li Lin - Initial Contribution
 *****************************************************************************
 *
 */package com.ibm.iot.iotspark;

import java.io.Serializable;
import java.util.ArrayList;
import java.lang.IndexOutOfBoundsException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf;
import org.apache.commons.math3.distribution.*;



/**
 * The Z-Score algorithm uses the following formula to normalize a raw scores x:
 *
 *   z = (x- μ)/σ
 * where:
 *    μ is the population mean.
 *    σ is the standard deviation of the population.
 * 
 */
@SuppressWarnings("serial")
public class IoTZScore implements Serializable{
	private ArrayList<Double> entries = new ArrayList<Double>();
    private double mu = 0;    //z-score μ
    private double temp = 0;  // current sigma mean (xi- u)
    
    /*
     * The following ZScore params only keep user specified entries -- the window value.
     * The wmu and wtemp are calculated only based on the window size
     */
    private ArrayList<Double> wentries = new ArrayList<Double>();
    private double wmu = 0;    // window z-score μ
    private double wtemp = 0;  // window current sigma mean (xi- u)
    private int wsize = 0;
    

    public void setWindowSize(int size) {
    	wsize = size;
    	System.out.println("window size is " + wsize);
    }
    
    public void addEntry(double e) {
    	entries.add(e);
    }
    
    public void updateZScoreMeans(Double a) {
		entries.add(a);
		double delta = a - mu;
		mu += delta / entries.size();
		
		if (entries.size() > 0) {
			temp += delta * (a - mu);
		}
		
	}
    
    /*
     * ZScore based on window set its mu based on the entries
     * inside this window frame.
     * The value move left and mu will be recalculated every time
     * the window move right.
     */
    public void updateWindowZScoreMeans(Double a) {
    	
    	try {
	    	if (wentries.size() >= wsize ) {
	    		//move value left
	    		for (int i = 0; i < wsize -1; i++) {
	    			wentries.set(i, wentries.get(i+1));
	    		}
	    		//add the value a to the last slot
	    		wentries.set(wsize -1,  a);
	    	} else {
	    		wentries.add(a);
	    	}
    	}catch(IndexOutOfBoundsException e) {
    		e.printStackTrace();
    	}
    	
    	//need to recalculate wmu from scratch
    	wmu = 0;
    	wtemp = 0;

    	for (int j = 0; j < wentries.size(); j++) {
    		double delta = wentries.get(j) - wmu;
    		wmu += delta / wentries.size();
    		if (wentries.size() > 0) {
    			wtemp += delta * (wentries.get(j) - wmu);
    		}
    	}
		
	}
    
	public double zScore(Double x) {
//		double mu = 0;
//		double temp = 0;
//		
//		// calculate mu -- population mean
//		for (int i = 0; i < entries.size(); i++) {
//			double a = entries.get(i);
//			
//			double delta = a - mu;
//			mu += delta / (i + 1);
//			
//			if (i > 0) {
//				temp += delta * (a - mu);
//			}
//		}
		
		//calculate sigma -- standard deviation
		double sigma = Math.sqrt(temp / (entries.size()));
		if (sigma == 0) {
			sigma = 1;
		}

		//zscore
		double z = (x - mu)/sigma;
		updateZScoreMeans(x);
		return z;
	}
	
	public double windowZScore(Double x) {
		if (wentries.size() < wsize) {
			updateWindowZScoreMeans(x);
			return 0.0;
		}
		//calculate sigma -- standard deviation
		double sigma = Math.sqrt(wtemp / wentries.size());
		if (sigma == 0) {
			sigma = 1;
		}

		//zscore
		double z = (x - wmu)/sigma;
		updateWindowZScoreMeans(x);
		return z;
	}

    /**
     * The calculation is done here.
     * Example: From 1.96 (Z-score) to 0.95 % (P-value)
     * From -1.96 (Z-score) to -0.95%
     *
     * @return double a p value
     */
    public double calculatePvalue(double aZValue) {
        aZValue = aZValue / Math.sqrt(2.0);
        double lPvalue = 0.0;
        try {
            lPvalue = Erf.erf(aZValue);
        } catch (MathException e) {
            e.printStackTrace();
        }
        return lPvalue;
    }

    public double zScoreToPercentile(double zScore)
	{
		double percentile = 0;
		
		NormalDistribution dist = new NormalDistribution();
		percentile = dist.cumulativeProbability(zScore) * 100;
		return percentile;
	}
    
}
