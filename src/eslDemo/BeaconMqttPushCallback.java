package eslDemo;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import pic2mqttdata.MTagType;

import eslDemo.MqttConnNotify.ActionNotify;
import eslDemo.MqttConnNotify.ConnectionNotify;

public class BeaconMqttPushCallback implements MqttCallback {  
    private static int DEF_REQ_DATA_MAX_LENGHT = 1024*60;
    BeaconMqttClient mClient;
    MqttConnNotify mMqttNotify;
    
    public static final int ERR_INVALID_INPUT = 1;
   	public static final int ERR_PARSE_SUCCESS = 0;
   	
   	private String mGatewaySubaction;    //gateway using this topic to receive command
   	private String mGatewayPubaction;    //gateway using this topic to send command ack
   	
   	public class EslObject
   	{
   		String mMacAddress;    //device id
   		int mRssi;
   		int mFaltStatus;
   		MTagType mEslType;
   		int mEslVoltage;
   		int mEslTemperature;
   		long mLastUpdateMsec;  //report time
   		int mEslVersion;
   		int mPictureID;
   		
   		int mExeCmdState;
   		
   		int mDownJsonNum;
   		int mDownFailNum;
   		int mDownSuccNum;
   		long mLastDownJsonTime;
   		long mCommandCause;
   	};
   	
   	public class EslShakeReq
   	{
   		public int mBuffDownMsgNum;
   		public int mAdvBuffDevNum;
   	};
   	private HashMap<String, EslObject> mDeviceMap = new HashMap<>();
    
    BeaconMqttPushCallback(BeaconMqttClient conn, MqttConnNotify mqttNotify){
    	mClient = conn;
    	mMqttNotify = mqttNotify;
    }
    
    public String getGatewaySubAction()
    {
    	return mGatewaySubaction;
    }

    public void connectionLost(Throwable cause) {  
        //connection lost, now reconnect
        System.err.println("MQTT client connection disconnected");
        mClient.setConnected(false);
        
        mGatewaySubaction = null;
        mGatewayPubaction = null;
        mMqttNotify.connectionNotify(ConnectionNotify.CONN_NTF_DISCONNECTED);
    }  
    
    public void deliveryComplete(IMqttDeliveryToken token) {
        
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面  
        handleMqttMsg(new String(message.getPayload()));
    }  
    
    
    protected void handleMqttMsg(String strMqttInfo)  {
		// TODO Auto-generated method stub	
		//parse jason object
		if (strMqttInfo == null){
			System.out.println("Receive invalid null data");
			return;
		}
		int nReqLen = strMqttInfo.length();			
		//parise request
		if (nReqLen > DEF_REQ_DATA_MAX_LENGHT){
			System.out.println("Receive an max length request, len:" + nReqLen);
			return;
		}

		parseJsonReq(strMqttInfo);
	}
		
	public static boolean isMacAddressValid(String strMacAddr)
	{
		if (strMacAddr == null || strMacAddr.length() != 12)
		{
			return false;
		}
		
		for (int j = 0; j < strMacAddr.length(); j++)
	    {
	    	char cMac = strMacAddr.charAt(j);
	    	if ((cMac >= '0' && cMac <= '9')
	    			|| (cMac >= 'A' && cMac <= 'F')
	    			|| (cMac >= 'a' && cMac <= 'f'))
	    	{
	    		continue;
	    	}
	    	else
	    	{
	    		return false;
	    	}
	    }
		
		return true;
	}
	
	public static final String DEF_BEACON_TYPE = "0";
	
	public int parseJsonReq(String strMqttInfo)
	{
		
		try 
		{
			JSONObject cmdReq = JSONObject.fromObject(strMqttInfo);
			if (cmdReq == null)
			{
				System.out.println("Connection to Mqtt server failed");
				return 0;
			}
			
			//message type
			String strDataType = cmdReq.getString("msg");
			if (strDataType.equalsIgnoreCase("advdata"))
			{
				return handleBeaconRpt(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("alive"))
			{
				return handleShakeReq(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("dAck"))
			{
				return handleDownloadAck(cmdReq);
			}
			else
			{
				System.out.println("unknown scan response data");
				return ERR_INVALID_INPUT;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return ERR_INVALID_INPUT;
		}
	}
	
	public int handleShakeReq(JSONObject cmdReqAgent)
	{
		try 
		{
			//mac address
			String strGwAddress = cmdReqAgent.getString("gmac");
			if (strGwAddress == null)
			{
				return ERR_INVALID_INPUT;
			}
			strGwAddress = strGwAddress.toUpperCase();
			if (!isMacAddressValid(strGwAddress)){
				System.out.println("beacon mqtt input invalid error");
				return ERR_INVALID_INPUT;
			}
					
			//subaction
			String strNewSubTopic = cmdReqAgent.getString("subaction");
			if (strNewSubTopic == null || strNewSubTopic.equals(""))
			{
				System.out.println("unknown obj data");
				return ERR_INVALID_INPUT;
			}
			
			
			//pubaction
			String strNewPubTopic = cmdReqAgent.getString("pubaction");
			if (strNewPubTopic == null || strNewPubTopic.equals(""))
			{
				System.out.println("unknown obj data");
				return ERR_INVALID_INPUT;
			}
			
			if (mGatewayPubaction == null)
			{
				mGatewayPubaction = strNewPubTopic;
				mClient.reNewSubscribe(mGatewayPubaction);
				
				System.out.println("shake with Gateway success");
				mMqttNotify.connectionNotify(ConnectionNotify.CONN_SHAKE_SUCCESS);
			}
			else
			{
				EslShakeReq shakeReq = new EslShakeReq();
				shakeReq.mAdvBuffDevNum = cmdReqAgent.optInt("advDevices", -1);
				shakeReq.mBuffDownMsgNum = cmdReqAgent.optInt("downDevices", -1);
				mMqttNotify.actionNotify(MqttConnNotify.ActionNotify.MSG_SHAKE_REQs, 
						strGwAddress, shakeReq);
			}
			
			mGatewaySubaction = strNewSubTopic;
			mGatewayPubaction = strNewPubTopic;
		}
		catch (Exception e) 
		{
			return ERR_INVALID_INPUT;
		}
		
		return ERR_PARSE_SUCCESS;
	}
	
	
	//monitor all esl device status
	public int handleBeaconRpt(JSONObject cmdReqAgent)
	{		
		try 
		{
			//mac address
			String strGwAddress = cmdReqAgent.getString("gmac");
			strGwAddress = strGwAddress.toUpperCase();
			if (!isMacAddressValid(strGwAddress)){
				System.out.println("beacon mqtt input invalid error");
				return ERR_INVALID_INPUT;
			}
					
			//obj list
			JSONArray objArray = cmdReqAgent.getJSONArray("obj");
			if (objArray == null)
			{
				System.out.println("unknown obj data");
				return ERR_INVALID_INPUT;
			}
			

			//update mac
			for (int i = 0; i < objArray.size(); i++)
			{
				JSONObject obj = objArray.getJSONObject(i);
		
				//device mac address
				String strDevMac = obj.getString("dmac");
				if (strDevMac == null)
				{
					return ERR_INVALID_INPUT;
				}
				strDevMac = strDevMac.toUpperCase();
				if (!isMacAddressValid(strDevMac)){
					System.out.println("beacon mqtt input invalid error");
					return ERR_INVALID_INPUT;
				}
				

			    //rssi
				String strRssi = obj.getString("rssi");
				int nRssi = 0;
				if (strRssi != null)
				{
				    nRssi = Integer.valueOf(strRssi);
					if (nRssi >= 10){
						System.out.println("beacon mqtt input invalid error");
						return ERR_INVALID_INPUT;
					}
				}
				
				//esl type
				int nEslType = obj.getInt("eslType");
				
				//prase data
				if (!obj.has("type") || obj.getInt("type") != 64)
				{
					continue;
				}
			
				EslObject eslObj = mDeviceMap.get(strDevMac);
				ActionNotify nNotify = MqttConnNotify.ActionNotify.DEVICE_UPDATE;
				if (eslObj == null)
				{
					MTagType eslType = MTagType.MTagTypeFromID(nEslType);
					if (eslType == null){
						return ERR_INVALID_INPUT;
					}
					
					eslObj = new EslObject();
					eslObj.mMacAddress = strDevMac;
					eslObj.mEslType = eslType;
					nNotify = MqttConnNotify.ActionNotify.FOUND_DEVICE;
					mDeviceMap.put(strDevMac, eslObj);
				}
				
				eslObj.mRssi = nRssi;
				eslObj.mLastUpdateMsec = System.currentTimeMillis();
				eslObj.mEslVersion = obj.getInt("ver");
				eslObj.mFaltStatus = obj.getInt("stat");
				eslObj.mEslVoltage = obj.getInt("vatt");
				eslObj.mEslTemperature = obj.getInt("temp");
				eslObj.mPictureID = obj.getInt("picID");
				
				if (MqttConnNotify.ActionNotify.FOUND_DEVICE == nNotify)
				{
					System.out.println(getCurrentTime() + " Found new ESL,ID:" + strDevMac 
							+ ",Rssi:" + nRssi
							+ ",Version:" + (int)eslObj.mEslVersion
							+ ",EslType:" + eslObj.mEslType.getName()
							+ ",Voltage:" + (int)eslObj.mEslVoltage + "mV"
							+ ",Temperature:" + (int)eslObj.mEslTemperature + "℃"
							+ ",PictureID:" + (int)eslObj.mPictureID);
				}
				
				mMqttNotify.actionNotify(nNotify, strDevMac, eslObj);
			}
		} 
		catch (Exception e) 
		{
			return ERR_INVALID_INPUT;
		}

		return ERR_PARSE_SUCCESS;
	}
	
	private String getCurrentTime()
	{
		long nCurrentTime = System.currentTimeMillis();
		
		Date date = new Date(nCurrentTime);
        return DATE_FORMAT.format(date) + " ";
	}
	
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public int handleDownloadAck(JSONObject cmdReqAgent)
	{
		try 
		{
			//mac address
			String strDevMac = cmdReqAgent.getString("mac");
			if (strDevMac == null)
			{
				return ERR_INVALID_INPUT;
			}
			strDevMac = strDevMac.toUpperCase();
			if (!isMacAddressValid(strDevMac)){
				System.out.println("beacon mqtt input invalid error");
				return ERR_INVALID_INPUT;
			}
			
	
			String strResult = cmdReqAgent.getString("rslt");
			if (strResult == null)
			{
				return ERR_INVALID_INPUT;
			}
			
			int nSequence = cmdReqAgent.getInt("seq");
			
			String strCause = cmdReqAgent.getString("cause");
			if (strCause == null)
			{
				return ERR_INVALID_INPUT;
			}
			int nCause = Integer.valueOf(strCause);
			//found device
			EslObject eslObj = this.mDeviceMap.get(strDevMac);
			if (eslObj == null)
			{
				eslObj = new EslObject();
				eslObj.mMacAddress = strDevMac;
				mDeviceMap.put(strDevMac, eslObj);
			}
			eslObj.mCommandCause = nCause;
			
			if (strResult.equals("succ"))
			{
				if (nCause == 1)
				{
					System.out.println(getCurrentTime() + "download data to " + strDevMac + " success:" + nSequence);
					this.mMqttNotify.actionNotify(ActionNotify.MSG_DOWNLOAD_SUCCESS, strDevMac, eslObj);
				}
				else
				{
					System.out.println(getCurrentTime() + "execute command to " + strDevMac + " success:" + nSequence);
					this.mMqttNotify.actionNotify(ActionNotify.MSG_EXECUTE_SUCCESS, strDevMac, eslObj);
				}
			}
			else
			{
				System.out.println(getCurrentTime() + "execute command to " + strDevMac + " failed:" 
							+ nCause + ",seq:" + nSequence);
				this.mMqttNotify.actionNotify(ActionNotify.MSG_EXECUTE_FAIL, strDevMac, eslObj);
			}
		}
		catch (Exception e) 
		{
			return ERR_INVALID_INPUT;
		}
		
		return ERR_PARSE_SUCCESS;
	}
	
}