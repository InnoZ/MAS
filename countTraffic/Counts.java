package countTraffic;

import Mathfunctions.Calculator;

/**
 * A class to store counted agents in the simulation, sorted by their transport mode. 
 * @author yasemin
 *
 */
public class Counts {

	private int countCars = 0;
	private int countBikes = 0;
	private int countPT = 0;
	private int countWalk = 0;
	private Calculator calc = new Calculator();
	
	/**
	 * @return total number of counted cars, bikes and also participants of public transport and walkers.
	 */
	public int getTotal(){
		return calc.scaleReverse(this.countCars + this.countBikes + this.countPT + this.countWalk);
	}
	
	public int getCars() {
		return calc.scaleReverse(this.countCars);
	}
	
	 public void setCars(int counts){
		 this.countCars = counts;
	 }
  
	 public void raiseCarsByOne(){
		 this.countCars++;
	 }
	 
	public int getBikes() {
		return calc.scaleReverse(this.countBikes);
	}
	
	public void setBikes(int counts){
		 this.countBikes = counts;
	 }
	
	public void raiseBikesByOne(){
		this.countBikes++;
	}
	
	public int getPT() {
		return calc.scaleReverse(this.countPT);
	}

	public void setPT(int counts){
		 this.countPT = counts;
	 }
	
	public void raisePTByOne(){
		this.countPT++;
	}
	
	public int getWalk() {
		return calc.scaleReverse(this.countWalk);
	}

	public void setWalk(int counts){
		 this.countWalk = counts;
	 }
	
	public void raiseWalkByOne(){
		this.countWalk++;
	}
}
