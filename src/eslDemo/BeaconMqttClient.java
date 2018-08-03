package eslDemo;
import org.eclipse.paho.client.mqttv3.MqttClient;  
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;  
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import eslDemo.MqttConnNotify.ConnectionNotify;

public class BeaconMqttClient 
{  
    private String mHostAddr = "tcp://api.ieasygroup.com:61613";  
    private String mPublishTopic = "kbeacon/publish/D0330A000180";  
    private  String mUserName = "kkmtest";
    private  String mPassWord = "testpassword";
    
    private MqttClient mClient;  
    private MqttConnectOptions options;  
    private String clientid = "clientCloudSave";  
    
    private boolean IsConnected = false;
    private MqttConnNotify mQttNotify;
    private BeaconMqttPushCallback mBeaconCallback;
    
    public boolean isConnected()
    {
    	return IsConnected;
    }
    
    public void setConnectinInfo(String strHost, String strPublishTopic, String usrPwd, String password)
    {
    	mHostAddr = strHost;
    	mPublishTopic = strPublishTopic;
    	mUserName = usrPwd;
    	mPassWord = password;
    }
    
    public String getHostAddr()
    {
    	return mHostAddr;
    }
    
    public String getPublishTopic()
    {
    	return mPublishTopic;
    }
    
    public String getUserName()
    {
    	return mUserName;
    }
   
    public String getPassword()
    {
    	return mPassWord;
    }
    
    public void setConnected(boolean enable)
    {
    	IsConnected = enable;
    }
    
    public BeaconMqttClient( MqttConnNotify mqttNotify)
    {
    	
    	mQttNotify = mqttNotify;
    	
    	initParamaters();
    }
  
    private void initParamaters() 
    {  
        try {  
            // hostΪ��������clientid������MQTT�Ŀͻ���ID��һ����Ψһ��ʶ����ʾ��MemoryPersistence����clientid�ı�����ʽ��Ĭ��Ϊ���ڴ汣��  
        	clientid = clientid + Math.random();
        	mClient = new MqttClient(mHostAddr, clientid, new MemoryPersistence());  
            
            // MQTT����������  
            options = new MqttConnectOptions();  
            
            // �����Ƿ����session,�����������Ϊfalse��ʾ�������ᱣ���ͻ��˵����Ӽ�¼����������Ϊtrue��ʾÿ�����ӵ������������µ��������  
            options.setCleanSession(true);  
            
            // �������ӵ��û���  
            options.setUserName(mUserName);  
            
            // �������ӵ�����  
            options.setPassword(mPassWord.toCharArray()); 
            
            // ���ó�ʱʱ�� ��λΪ��  
            options.setConnectionTimeout(30);  
            
            // ���ûỰ����ʱ�� ��λΪ�� ��������ÿ��1.5*20���ʱ����ͻ��˷��͸���Ϣ�жϿͻ����Ƿ����ߣ������������û�������Ļ���  
            options.setKeepAliveInterval(20);  
            
            // ���ûص�  
            mBeaconCallback = new BeaconMqttPushCallback(this, mQttNotify);
            mClient.setCallback(mBeaconCallback);  
            //MqttTopic topic = client.getTopic(SUB_TOPIC);  
            
            //setWill�����������Ŀ����Ҫ֪���ͻ����Ƿ���߿��Ե��ø÷������������ն˿ڵ�֪ͨ��Ϣ    
            //options.setWill(topic, "close".getBytes(), 2, true);  
            
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }   
    
    public synchronized void connect()
    {
    	try { 
    		mClient.connect(options); 
	        
	        mQttNotify.connectionNotify(ConnectionNotify.CONN_NTF_CONNECED);
	        
	        IsConnected = true;
	        System.out.println("Connect to server complete");
	        
	        //������Ϣ  
	        int[] Qos  = {0};  
	        String[] topic1 = {mPublishTopic};  
	        mClient.subscribe(topic1, Qos); 
	        System.out.println("subscribe topic to server complete");
	        
	    } catch (Exception e) {  
	    	System.out.println("Connection to Mqtt server failed");
	        e.printStackTrace();
	    }
    }
    
    public void reNewSubscribe(String strPubActionTopic)
    {
    	if (IsConnected)
    	{
    		int[] Qos  = {0, 2};  
	        String[] topic1 = {mPublishTopic, strPubActionTopic};  
	        
	        try
	        {
	        	mClient.unsubscribe(mPublishTopic);
	        	mClient.subscribe(topic1, Qos);
	        	System.out.println("Update Subscribe success");
	        } catch (Exception e) {  
		        e.printStackTrace();
		    }
    	}
    }
    
    public synchronized boolean pubCommand2Gateway(String msg) 
    {   
    	String strGatewaySubaction = mBeaconCallback.getGatewaySubAction();
		if (strGatewaySubaction == null)
		{
			return false;
		}
		
    	if (IsConnected)
    	{
	        MqttMessage message = new MqttMessage(msg.getBytes());  
	        message.setQos(1);  
	        message.setRetained(false);  
	        try
	        {
	        	mClient.publish(strGatewaySubaction, message);  
	        	return true;
	        }
	        catch(Exception e)
	        {
		        e.printStackTrace();
		        return false;
	        }
    	}
    	
    	return false;
    }  
    
    public synchronized void disconnect()
    {
    	try { 
    		mClient.disconnect();
    		mClient.close();
    		
    		mBeaconCallback.connectionLost(null);
	    } catch (Exception e) {  
	    	System.out.println("disconnect from mqtt server");
	    }
    }
}
