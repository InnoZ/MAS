package readTrafficVorlaeufig;

public class TrafficPeopleMode {

	private int age;
	private int bike;
	private int pedestrian;
	private int car;
	private int pt;
	private Sex sex;

	public enum Sex {
		m, f
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setAge(String age) {
		this.age = Integer.parseInt(age);
	}

	public Sex getSexEnum() {
		return sex;
	}
	
	public String getSex() {
		return sex.toString();
	}
	
	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public void setSex(boolean sex) {
		if (sex == false)
			this.sex = Sex.m;
		else
			this.sex = Sex.f;
	}

	public int getBike() {
		return bike;
	}

	public void setBike(int bike) {
		this.bike = bike;
	}
	
	public void setBike(String bike) {
		this.bike = Integer.parseInt(bike);
	}
	
	public int getPedestrian() {
		return pedestrian;
	}

	public void setPedestrian(int pedestrian) {
		this.pedestrian = pedestrian;
	}
	
	public void setPedestrian(String pedestrian) {
		this.pedestrian = Integer.parseInt(pedestrian);
	}
	
	public int getCar() {
		return car;
	}

	public void setCar(int car) {
		this.car = car;
	}
	
	public void setCar(String car) {
		this.car = Integer.parseInt(car);
	}
	
	public int getPt() {
		return pt;
	}

	public void setPt(int pt) {
		this.pt = pt;
	}
	
	public void setPt(String pt) {
		this.pt = Integer.parseInt(pt);
	}

}	
