package com.david.tankservice;

import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TankService {
	
	public static int ticker = 0;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Mqtt mqtt = Mqtt.getInstance();
		MapModel map = MapModel.getInstance();
		
		


	}
	

/*
  {"Event":"NewTank","Name":"David"}
  {"Event":"TankMove","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150"}
  {"Event":"Fire", "Direction":"U/D/L/R", "X":"150", "Y":"150"}
*/	 
	public static void ProcessMqttMsg(String topic, String message) {
		System.out.println ("Topic:" + topic + " message: " + message);
		Mqtt m = Mqtt.getInstance();
		if (topic.equals("/tank/event")) {
			
			System.out.println("process mqtt " + topic +"," + message);
			MapModel map = MapModel.getInstance();
			
			JSONObject event;
			try {
				event = new JSONObject(message);
				if (event.get("Event").equals("TankMove")) {
					String name = event.getString("Name");
					String direction = event.getString("Direction");
					int X = Integer.parseInt(event.getString("X"));
					int Y = Integer.parseInt(event.getString("Y"));
					int targetX = Integer.parseInt(event.getString("TargetX"));
					int targetY = Integer.parseInt(event.getString("TargetY"));
			
					Tank tank = map.tanks.get(name);
					if (tank == null) { // can not find tank
						System.out.println("Can not find tank " + name);
						return;
					}
					if (tank.life == 0) {
						//tank is dead
						System.out.println("Tank " + name + " nolonger valid");
						return;
					}
					tank.dir = direction.charAt(0);
					tank.position_x = X;
					tank.position_y = Y;
					tank.target_x = targetX;
					tank.target_y = targetY;		
					System.out.printf("Tank move %s %d %d", name, targetX, targetY);
					String msg = "{\"Event\":\"TankMove\",\"Name\":\"" + tank.name+"\",\"Direction\":\"" + String.valueOf(tank.dir)+"\", \"X\":\""+ tank.position_x+"\", \"Y\":\""+tank.position_y+"\",\"TargetX\":\""+tank.target_x+"\",\"TargetY\":\""+tank.target_y+"\", \"Life\":\""+tank.life+"\",\"Blood\":\""+tank.blood+"\", \"God\":\""+tank.god+"\"}";
					m.SendMessage("/tank/notify",msg );
					
					//{"Event":"TankMove","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150", "Life":"3","Blood":"10", "IsGod":"false"}
					
				}
				if (event.get("Event").equals("NewTank")) {
					String name = event.getString("Name");
					System.out.println("Find newTank event");
					
					String direction = "U";
					int X = (int)( Math.random()*25);
					int Y = (int)( Math.random()*25);
					boolean find = false;
					for (int i=X; i<24; i++) {
						if (map.wallmap[i][Y] == '0') {
							// something on the position
							X = i;
							find = true;
							break;
						}
					}
					
					
					if (find == false) {
						for (int i=0; i<X; i++) {
							if (map.wallmap[i][Y] == '0') {
								// something on the position
								X = i;
								find = true;
								break;
							}
						}	
					}
					
					Tank tank = new Tank(name, X*25+25, Y*25+25);
					tank.dir = direction.charAt(0);
					tank.target_x = tank.position_x;
					tank.target_y = tank.position_y;	
					map.tanks.put(name, tank);  // add new tank to map
					
					String mapstr = map.toJsonStr();
					if (mapstr != null) {
						m.SendMessage("/tank/notify", mapstr);
					}
					
					
				}
				
				if (event.get("Event").equals("Fire")) {
					String name = event.getString("Name");
					char direction = event.getString("Direction").charAt(0);
					int X = Integer.parseInt(event.getString("X"));
					int Y = Integer.parseInt(event.getString("Y"));
					Bullet bullet = new Bullet(name, X, Y, direction);
					map.bullets.add(bullet);

					m.SendMessage("/tank/notify",message );					
				}
	
	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	

	
	

}

