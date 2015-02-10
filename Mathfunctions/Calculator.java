package Mathfunctions;

public class Calculator {
	
	private double scalingfactor = 10;

	public double calculatePercentage(int n1, int n2) {
		return Math.round((((100.0 / n1) * n2) * 100)) / 100.0;
	}
	
	public double scale(int toBeScaled){
		return toBeScaled/this.scalingfactor;
	}
	
	public int scaleReverse(int toBeScaled){
		return (int) (toBeScaled*this.scalingfactor);

	}
}
