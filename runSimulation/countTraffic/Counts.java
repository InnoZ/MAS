package countTraffic;

public class Counts {

	int countCars;
	int countBikes;
	int countPT;
	int countWalk;
	
	public Counts(){
		this.countCars = 0;
		this.countBikes = 0;
		this.countPT = 0;
		this.countWalk = 0;
	}
	
	public int getCars(){
		return this.countCars;
	}
	
	public int getBikes(){
		return this.countBikes;
	}
	
	public int getPT(){
		return this.countPT;
	}
	
	public int getWalk(){
		return this.countWalk;
	}
}
