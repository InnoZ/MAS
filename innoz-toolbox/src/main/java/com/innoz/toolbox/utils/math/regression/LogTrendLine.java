package com.innoz.toolbox.utils.math.regression;

public class LogTrendLine extends OLSTrendLine {
	
    @Override
    protected double[] xVector(double x) {
    	
        return new double[]{1,Math.log(x)};
        
    }

    @Override
    protected boolean logY() {
    	
    	return false;
    	
    }
}