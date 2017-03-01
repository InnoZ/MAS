package com.innoz.toolbox.utils.math.regression;

/**
 * 
 * An interface for different types of trend lines. These trend lines represent the regression of a data set. </br>
 * 
 * Source: <a href="http://stackoverflow.com/questions/17592139/trend-lines-regression-curve-fitting-java-library">Stackoverflow<a/>
 * 
 * @author dhosse
 *
 */
public interface TrendLine {

	public void setValues(double[] y, double[] x); // y ~ f(x)
    public double predict(double x); // get a predicted y for a given x
    public double getRSquare(); // get the coefficient of determination r^2

}