package com.david.tankservice;





public class Bullet {
	private String name = "";
	private int position_x =0;
	private int position_y =0 ;
	private char direction = 'L';
	public boolean isvalid = true;
	
	// 定义墙的类型
	
	

	public Bullet (String fromtank, int x, int y ,char direction) {
		this.position_x = x;
		this.setPosition_y(y);
		this.direction = direction;
		this.setName(fromtank);
	}
	
	
	
	
	public char getDirection() {
		return direction;
	}




	public void setDirection(char direction) {
		this.direction = direction;
	}




	public boolean isIsvalid() {
		return isvalid;
	}




	public void setIsvalid(boolean isvalid) {
		this.isvalid = isvalid;
	}




	public void MoveBullet () {
		if (position_x <=0 || position_y <=0) {
			isvalid = false;
		}
		if (isvalid) {
			if (direction == 'L') {
				position_x -=10;
				if (position_x <0) {
					isvalid = false;
				}
			}
			if (direction == 'R') {
				position_x +=10;
				if (position_x >620) {
					isvalid = false;
				}
			}
			if (direction == 'U') {
				position_y -=10;
				if (position_y <0) {
					isvalid = false;
				}
			}
			if (direction == 'D') {
				position_y +=10;
				if (position_y >620) {
					isvalid = false;
				}
			}
		}
	}




	public int getPosition_x() {
		return position_x;
	}




	public void setPosition_x(int position_x) {
		this.position_x = position_x;
	}
	
	public int getPosition_y() {
		return position_y;
	}




	public void setPosition_y(int position_y) {
		this.position_y = position_y;
	}




	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}

}
