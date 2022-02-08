package com.david.tankservice;



import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;



public class Mqtt {
	private static Mqtt instance = null;
	private  MqttClient client = null;
	private  MqttClient pubclient = null;
	
	private  MqttConnectOptions mqttConnectOptions = null;

	
	/*
	 * 
	 * Message Protocol for Tank game :/tank/event
	 * 1. Event   From Terminal to Server:
	 * 	{"Event":"NewTank","Name":"David"}
	 *  {"Event":"TankMove","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150"}
	 * 	{"Event":"Fire", "Direction":"U/D/L/R", "X":"150", "Y":"150"}
	 * 
	 * 2. Notify  From Server to terminal:  "/tank/notify
	 * {"Event":"SyncMap","Map":["110000000","13400000"...], "Tanks":[{"Name":"coco","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150"}, 
	 * 		{"Name":"coco","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150"},{"Name":"coco","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150"}]}
	 * 
	 * {"Event":"NewTank","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150", "Life":"3","Blood":"10", "IsGod":"true"}
	 * {"Event":"TankMove","Name":"David","Direction":"U/D/L/R", "X":"150", "Y":"150","TargetX":"150","TargetY":"150", "Life":"3","Blood":"10", "IsGod":"false"}
	 * {"Event":"Fire", "Direction":"U/D/L/R", "X":"150", "Y":"150"}
	 * {"Event":"DestroyBrick", "X":"150", "Y":"150"}
	 * {"Event":"DestroyTank", "Name":"David", "X":"150", "Y":"150", "Life":"2", "Blood":"10"}
	 * 
	 * 3. message Peer to Peer    /tank/message
	 * {"From":"David", "Message":"Hello 123"}
	 *  
	 */
	
	public static Mqtt getInstance() {
			if (instance == null) {
				instance = new Mqtt("tankservice");		
			}
		return instance;
	}
	
	
	public Mqtt(String clientid) {
			mqttConnectOptions = new MqttConnectOptions();
			mqttConnectOptions.setCleanSession(true);
//			设置连接超时
			mqttConnectOptions.setMaxInflight(1000);
			mqttConnectOptions.setConnectionTimeout(300);
			mqttConnectOptions.setKeepAliveInterval(20);
//			mqttConnectOptions.setAutomaticReconnect(true);
//			设置持久化方式
			try {
				
			
				client = new org.eclipse.paho.client.mqttv3.MqttClient("tcp://broker.emqx.io:1883",clientid ,new MemoryPersistence());
				
				client.setCallback(new MqttCallbackExtended() {
	
					@Override
					public void connectionLost(Throwable arg0) {
						// TODO Auto-generated method stub
						//System.out.println("Connection lost, please restart");
						try {
							client.reconnect();
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
												
					}
	
					@Override
					public void deliveryComplete(IMqttDeliveryToken arg0) {
						// TODO Auto-generated method stub
		
					}
	
					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {
						// TODO Auto-generated method stub
						System.out.println("Receive message from " + topic +" message " + message.toString() );
						TankService.ProcessMqttMsg(topic, message.toString());
						
					}
	
					@Override
					public void connectComplete(boolean arg0, String arg1) {
						// TODO Auto-generated method stub
						System.out.println("Connected");
						try {
							client.subscribe("/tank/event", 0);
							client.subscribe("/tank/message", 0);
	//						client.subscribe("/tank/notify", 0);
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				});
				client.connect(mqttConnectOptions);
	
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void SendMessage( String topic, String msg) {
		if (client == null) {
			System.out.println("not initialized pubclient");
			return;  // not initialized yet
		}
		MqttMessage message = new MqttMessage(msg.getBytes());
		MqttTopic topic1 = client.getTopic(topic);
		try {
			MqttDeliveryToken token = topic1.publish(message);
			System.out.println("message sent " + topic + " message: " + msg );
			
          //  token.waitForCompletion();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
