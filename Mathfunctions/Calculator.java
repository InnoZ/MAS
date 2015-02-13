package Mathfunctions;

public class Calculator {
	
	private double scalingfactor = 10;

	public double calculatePercentage(int n1, int n2) {
		return Math.round((((100.0 / n1) * n2) * 100)) / 100.0;
	}
	
	/**
	 * scale from person to agent
	 * @param d number of persons 
	 * @return number of agents
	 */
	public double scale(double d){
		return d/this.scalingfactor;
	}
	/**
	 * scale from agents to persons
	 * @param d number of agents 
	 * @return number of persons
	 */
	public int scaleReverse(double d){
		return (int) (d*this.scalingfactor);

	}
	
	/**
	 * @param n1
	 * @param n2
	 * @return n2 as % of n1
	 */
	private double percentage(int n1, int n2) {
		return Math.round((((100.0 / n1) * n2) * 100)) / 100.0;
	}

	/**
	 * @param a
	 * @param b
	 * @return (a-b) as percentage of a
	 */
	public double relativeDifference(int a, int b){
		int absoluteDifference = a - b;
		return percentage(a, absoluteDifference);	
	}
	
}
