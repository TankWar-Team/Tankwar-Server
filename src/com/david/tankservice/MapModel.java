package com.david.tankservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;








public class MapModel {

	public static MapModel instance;
	public static final int WIDTH = 25;
	public static final int HEIGHT = 25;
	public static final char BRICK = '1';
	public static final char STEEL = '2';
	public static final char SEA = '3';
	public static final char ICE = '4';
	public static final char FOREST = '5';
	public static final char STAR = '6';
	public static final char TANK = '7';
	public static int ticker;
	
	public HashMap <String, Tank> tanks = new HashMap<String, Tank>();
	public List<Bullet> bullets = new Vector<Bullet>();
	public char[][] wallmap = new char [WIDTH+1][HEIGHT+1];
	
	public boolean ismove = false;
	public static MapModel getInstance() {
		if (instance == null) {
			instance = new MapModel();
		}
		return instance;
		
	}
	
	public MapModel() {
			ReadMap("maps/tank3.map");
			UpdateThread timerthread = new UpdateThread();
			timerthread.start();		
	}
	
	public String toJsonStr() {
		String jsonstr = null;
		JSONObject result = new JSONObject();
		JSONArray  maparray = new JSONArray();
		JSONArray  tanklist = new JSONArray();
		JSONArray  bulletlist = new JSONArray();
		for (int row=0; row< 25; row++) {
			String line = "";
			for (int col=0; col<25; col++) {
				line = line + String.valueOf(wallmap[row][col]);
			}
			System.out.println("add map line " + line);
			maparray.put(line);
		}
		
		for (Entry<String, Tank> entry: tanks.entrySet()) {
			String name = entry.getKey();
			Tank tank = entry.getValue();
			
			JSONObject tanko = new JSONObject();
			
			try {
				tanko.put("Name", name);
				tanko.put("Direction", String.valueOf(tank.dir));
				tanko.put("X",tank.position_x);
				tanko.put("Y",tank.position_y);
				tanko.put("TargetX", tank.target_x);
				tanko.put("TargetY",  tank.target_y);
				tanko.put("God",tank.god);
				tanko.put("Life", tank.life);
				tanko.put("Blood",tank.blood);
				} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			tanklist.put(tanko);
		}
		
		for (int i=0; i< bullets.size(); i++) {
			
			Bullet bullet = (Bullet) bullets.get(i);
			System.out.println("add bullet " + bullet.getName());
			JSONObject o = new JSONObject();
			try {
				o.put("Name", bullet.getName());
				o.put("X", bullet.getPosition_x());
				o.put("Y",bullet.getPosition_y());
				o.put("Direction", String.valueOf(bullet.getDirection()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			bulletlist.put(o);
		}
		try {
			result.put("Event", "SyncMap");
			result.put("Map", maparray);
			result.put("Tanks", tanklist);
			result.put("Bullets", bulletlist);
			jsonstr = result.toString();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return jsonstr;
	}
	public void ReadMap( String fileName) {
		try {
			
			File file = new File(fileName);
	        FileReader fr = new FileReader(file);
	        BufferedReader br = new BufferedReader(fr);
	        String line;
	        System.out.println("Loading Tank war map from file " + fileName);
	        int lineid = 0;
	       
	        for (int i=0 ; i<25; i++) {
	        	for (int j=0;j<25;j++) {
	        		wallmap[i][j] = '0';
	        	}
	        }
	        System.out.println(" 01234567890123456789012345");
	        while((line = br.readLine()) != null){
	            //process the line
	        	
	        	System.out.println(lineid %10 + line);
	        	for( int i=0; i<WIDTH && i <line.length() ; i++) {
	        		char item = line.charAt(i) ;
	        		if (item <= '6' && item >= '0') {
	        			wallmap[i][lineid] = item;	
	        		}
	        		
	        	}
	        	lineid ++;
	        }
	        //close resources
	        br.close();
	        fr.close();
	    }catch(IOException e) {
	    	System.out.println("Can not read file:" + fileName);
	    	
	    }
	}
	
	
	private boolean checkBulletCollide(Bullet bullet) {
		int x = (bullet.getPosition_x())/25-1;
		int y =  (bullet.getPosition_y())/25 -1;
		
		if (x <0 || y <0) {
			return false;
		}
		
		System.out.println("Bullet: x:" + x + " y: " + y);
		Mqtt m = Mqtt.getInstance();
		if (wallmap [x][y] == BRICK) {
			
			// collide with brick
			wallmap [x][y] = '0';
			String msg = "{\"Event\":\"DestroyBrick\",\"Name\":\""+ bullet.getName() +"\",\"X\":\"" + bullet.getPosition_x() + "\", \"Y\":\""+ bullet.getPosition_y() + "\"}";
			m.SendMessage("/tank/notify",msg );
			System.out.println(msg);
			return true;
		}
		
		if (wallmap [x][y] == STEEL) {
			// collide with brick
			System.out.println("bullet hit steel");
			return true;
		}

		for (Entry<String, Tank> entry: tanks.entrySet()) {
			String name = entry.getKey();
			Tank tank1 = entry.getValue();
			if (bullet.getName().equals(name)) {
				continue; // this bullet sent out from tank with same name;
			}
			if (x == ((tank1.position_x)/25-1) && y == ((tank1.position_y )/25 -1)) {
				System.out.println("Destroy Tank!" + tank1.name);
				tank1.blood --;
				if (tank1.blood <=0) {
					tank1.blood = Tank.MAX_BLOOD;
					tank1.life --;
				}
				if (tank1.life <=0) {
					tank1.life = 0;
					this.tanks.remove(name);
				}
				// collide with tank
				String msg = "{\"Event\":\"DestroyTank\",\"Name\":\"" + tank1.name+"\", \"Life\":\""+tank1.life+"\",\"Blood\":\""+tank1.blood+"\", \"God\":\""+tank1.god+"\"}";
				m.SendMessage("/tank/notify",msg );
				return true;
			}
			
		}
		
		
		return false;
	}
	
	
	
	
	 
	private boolean checkTankWallCollide(Tank tank) {
		
		int nextx = (tank.position_x +24)/25 -1;
		int nexty = (tank.position_y +24)/25 -1;
		
		char item = wallmap[nextx][nexty];
		if (item == BRICK || item == STEEL || item == SEA) {
			System.out.println("tank " + tank.name + " collide wall X:" +(nextx+1) + "Y:" + (nexty+1)); 
			return true;
		}
		
		if (item == ICE) {
			switch (tank.dir) {
			case 'U':
				while (wallmap[nextx][nexty] == MapModel.ICE) {
					nexty --;
				}
				break;
			case 'D':
				while (wallmap[nextx][nexty] == MapModel.ICE) {
					nexty ++;
				}
				break;
			case 'L':
				while (wallmap[nextx][nexty] == MapModel.ICE) {
					nextx --;
				}
				
				break;
			case 'R':
				while (wallmap[nextx][nexty] == MapModel.ICE) {
					nextx ++;
				}
				break;
			}
			tank.target_x = nextx*25+25;
			tank.target_y = nexty*25+25;
		}		
		return false;
	}
	
	private boolean checkTankTankCollide(Tank tank) {
		for (Entry<String, Tank> entry: tanks.entrySet()) {
			String name = entry.getKey();
			Tank tank1 = entry.getValue();
			if (tank1.name.equals(tank.name)) {
				continue;  // same tank
			}
			if ((tank1.position_x +24 )/25 == (tank.position_x +24)/25 && (tank1.position_y+24)/25 == (tank.position_y+24)/25) {
				System.out.println("Tank " + tank.name + " collide with " + tank1.name);
				return true;// collide 
			}
		}
		
		return false;
	}
	
	public boolean checkMap () {
	//	System.out.println(ticker);
		for (Entry<String, Tank> entry: tanks.entrySet()) {
			String name = entry.getKey();
			Tank tank = entry.getValue();
			if (tank.islive && tank.blood>0) {
				int x = tank.position_x;
				int y = tank.position_y;
				if( tank.Move() ) {
					if (checkTankWallCollide (tank) || checkTankTankCollide(tank)) {  // collide with something
						tank.target_x = x/25*25;
						tank.target_y = y/25*25;
						tank.position_x = tank.target_x;
						tank.position_y = tank.target_y;
					}
					System.out.println("Ticker"+ ticker);
					System.out.println("Tank " + tank.name + " position X:" + (tank.position_x+24)/25 + "Y:" + (tank.position_y+24)/25 );	
						
				}
			}
		}
		

		Iterator<Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			Bullet bullet = (Bullet) it.next();  //TODO ? concurrent modification? where
			System.out.println("Bullet " + bullet.getName() + bullet.getDirection() +":"+ bullet.getPosition_x() + "," + bullet.getPosition_y() );
			bullet.MoveBullet();
			if (bullet.isvalid == false) {
				it.remove();
				continue;
			}
			boolean w = checkBulletCollide(bullet);
			if (w == true) {  
				it.remove();  // bullet collide with brick / steel or tank, remmove bullet from list;
			}
			
		}
		return false;
	}

	public class UpdateThread extends Thread {
		//Flush Screen
		public void run() {
			while(true) {
				ticker ++;
				//  repaint every 50 ms
				try {
					checkMap();
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	
}
