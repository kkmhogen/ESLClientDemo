package eslDemo;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import pic2mqttdata.MTagType;
import pic2mqttdata.Pic2MqttDataService;
import pic2mqttdata.bmp.Bmp2MqttDataService;
import pic2mqttdata.bmp.PicturePartRefreshStru;


import net.sf.json.JSONObject;

import eslDemo.BeaconMqttPushCallback.EslObject;
import eslDemo.BeaconMqttPushCallback.EslShakeReq;

public class EslPannel extends JPanel implements MqttEventNotify {
	private static int mMsgSequence = 101;
	private static final long serialVersionUID = 1L;

	private final static String CFG_MQTT_SRV_URL = "MqttSrvUrl";
	private final static String CFG_MQTT_PUBLISH_TOPIC = "MqttPublishTopic";
	private final static String LAST_LOAD_JSON_FILE_PATH = "LastJsonFilePath";
	private final static String LAST_LOAD_BMP_FILE_PATH = "LastBmpFilePath";
	private final static String CFG_MQTT_USR_NAME = "MqttUserName";
	private final static String CFG_MQTT_USR_PASSWORD = "MqttPassword";

	BeaconMqttClient mMqttClient; // mqtt connection
	BeaconMqttPushCallback mMqttMsgHandler;  //mqtt message handler
	
	private JButton buttonConn, buttonOpenJsonFile, buttonOpenBmpFile, buttonPushBmpMsg, buttonPushJsonMsg, 
		buttonQRCode, buttonPrintMac, buttonPrintBattLvls;
	private JTextField textMqttSrv, textGwID, textMqttUser, textMqttPwd,
			textJsonFile, textBmpFile, textDeviceID;
	private JTextArea textLogInfo;
	@SuppressWarnings("rawtypes")
	private JComboBox comboSizeTypeBox;

	boolean mShakeWithGwSucc = false;
	long mLastExeCmdTime = 0;

	JSONObject mJsonMsg = null;

	public static final int QRCODE_PICTURE_ID = 3;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EslPannel() {

		mMqttClient = new BeaconMqttClient(); 
    	mMqttMsgHandler = new BeaconMqttPushCallback(mMqttClient, this);
    	mMqttClient.setMsgHandler(mMqttMsgHandler);
    	
		this.setLayout(new GridLayout(3, 1)); // 网格式布局

		// mqtt address
		JPanel pannelMqttInfo = new JPanel();
		pannelMqttInfo.setLayout(new GridLayout(5, 1));
		this.add(pannelMqttInfo);
		JLabel labelMqttSrv = new JLabel("Address");
		this.textMqttSrv = new JTextField(30);
		JPanel pannelMqttSrv = new JPanel();
		pannelMqttSrv.add(labelMqttSrv);
		pannelMqttSrv.add(this.textMqttSrv);
		pannelMqttSrv.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelMqttInfo.add(pannelMqttSrv);

		// mqtt subsribe topic
		JLabel labelGwID = new JLabel("Subscribe");
		this.textGwID = new JTextField(30);
		JPanel pannelGwID = new JPanel();
		pannelGwID.add(labelGwID);
		pannelGwID.add(this.textGwID);
		pannelGwID.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelMqttInfo.add(pannelGwID);

		// user name
		JLabel labelMqttUser = new JLabel("User Name");
		this.textMqttUser = new JTextField(10);
		JPanel pannelUser = new JPanel();
		pannelUser.add(labelMqttUser);
		pannelUser.add(this.textMqttUser);
		pannelUser.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelMqttInfo.add(pannelUser);

		// user password
		JLabel labelMqttPwd = new JLabel("User Pwd");
		this.textMqttPwd = new JTextField(10);
		JPanel pannelPwd = new JPanel();
		pannelPwd.add(labelMqttPwd);
		pannelPwd.add(this.textMqttPwd);
		pannelPwd.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelMqttInfo.add(pannelPwd);

		// connect
		this.buttonConn = new JButton("Connect");
		JPanel pannelLogin = new JPanel();
		pannelLogin.add(this.buttonConn);
		pannelLogin.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelMqttInfo.add(pannelLogin);

		// log info
		this.textLogInfo = new JTextArea(10, 50);
		JScrollPane scroll = new JScrollPane(textLogInfo);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel pannelLogInfo = new JPanel();
		pannelLogInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
		pannelLogInfo.add(scroll);
		this.add(pannelLogInfo);
		
		
		// mac address
		JPanel devicePannel = new JPanel();
		devicePannel.setLayout(new GridLayout(5, 1));
		this.add(devicePannel);
		JLabel labelDeviceID = new JLabel("Device ID");
		this.textDeviceID = new JTextField(10);
		JPanel pannelDeviceID = new JPanel();
		pannelDeviceID.add(labelDeviceID);
		pannelDeviceID.add(this.textDeviceID);
		JLabel eslType = new JLabel("ESL Type");
		this.comboSizeTypeBox = new JComboBox(); // ESL type
		this.comboSizeTypeBox.addItem("29 1color");
		this.comboSizeTypeBox.addItem("29 3color");
		this.comboSizeTypeBox.addItem("42 1color");
		this.comboSizeTypeBox.addItem("42 3color");
		this.comboSizeTypeBox.addItem("21 1color");
		this.comboSizeTypeBox.addItem("21 3color");
		this.comboSizeTypeBox.addItem("22 3color");
		pannelDeviceID.add(eslType);
		pannelDeviceID.add(this.comboSizeTypeBox);
		pannelDeviceID.setLayout(new FlowLayout(FlowLayout.LEFT));
		devicePannel.add(pannelDeviceID);

		// download info
		this.textBmpFile = new JTextField(30);
		this.buttonOpenBmpFile = new JButton("OpenBmpFile");
		this.buttonPushBmpMsg = new JButton("Download");
		buttonPushBmpMsg.setEnabled(false);
		JPanel pannelOpenBmpFile = new JPanel();
		pannelOpenBmpFile.add(this.textBmpFile);
		pannelOpenBmpFile.add(this.buttonOpenBmpFile);
		pannelOpenBmpFile.add(this.buttonPushBmpMsg);
		pannelOpenBmpFile.setLayout(new FlowLayout(FlowLayout.LEFT));
		devicePannel.add(pannelOpenBmpFile);
		
		// download info
		this.textJsonFile = new JTextField(30);
		this.buttonOpenJsonFile = new JButton("OpenRawJson");
		this.buttonPushJsonMsg = new JButton("Download");
		buttonPushJsonMsg.setEnabled(false);
		JPanel pannelRowJsonFile = new JPanel();
		pannelRowJsonFile.add(this.textJsonFile);
		pannelRowJsonFile.add(this.buttonOpenJsonFile);
		pannelRowJsonFile.add(this.buttonPushJsonMsg);
		pannelRowJsonFile.setLayout(new FlowLayout(FlowLayout.LEFT));
		devicePannel.add(pannelRowJsonFile);
		
		//qr code download
		JPanel pannelDownload = new JPanel();
		this.buttonQRCode = new JButton("Add QR Code");
		this.buttonPrintMac = new JButton("Print Mac");
		this.buttonPrintBattLvls = new JButton("Add BattLvls");
		pannelDownload.add(this.buttonQRCode);
		pannelDownload.add(this.buttonPrintMac);
		pannelDownload.add(this.buttonPrintBattLvls);

		this.buttonQRCode.setEnabled(false);
		this.buttonPrintMac.setEnabled(false);
		this.buttonPrintBattLvls.setEnabled(false);
		
		pannelDownload.setLayout(new FlowLayout(FlowLayout.LEFT));
		devicePannel.add(pannelDownload);

		String strMqttSrv = EslConfig.getPropertyValue(CFG_MQTT_SRV_URL,
				mMqttClient.getHostAddr());
		this.textMqttSrv.setText(strMqttSrv);

		String strMqttPublishTopic = EslConfig.getPropertyValue(
				CFG_MQTT_PUBLISH_TOPIC, mMqttClient.getPublishTopic());
		this.textGwID.setText(strMqttPublishTopic);

		String strMqttUserName = EslConfig.getPropertyValue(CFG_MQTT_USR_NAME,
				mMqttClient.getUserName());
		this.textMqttUser.setText(strMqttUserName);

		String strMqttUserPassword = EslConfig.getPropertyValue(
				CFG_MQTT_USR_PASSWORD, mMqttClient.getPassword());
		this.textMqttPwd.setText(strMqttUserPassword);
		addClickListener();
	}

	private void addClickListener() {
		buttonConn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startConnectCloud();
			}
		});

		buttonOpenJsonFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openJsonFile();
			}
		});

		buttonPushJsonMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String strJsonFileName = textJsonFile.getText();
				if (strJsonFileName == null) {
					textLogInfo.append("File not exist");
					return;
				}
				
				downJsonFile2Device(strJsonFileName);
			}
		});
		
		buttonOpenBmpFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openBmpFile();
			}
		});

		buttonPushBmpMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String strJsonFileName = textBmpFile.getText();
				if (strJsonFileName == null) {
					textLogInfo.append("File not exist");
					return;
				}
				
				downBmpFile2Device(strJsonFileName);
			}
		});
		
		
		buttonQRCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downQRCodeToDevice();
			}
		});
		
		buttonPrintMac.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printMacToDevice();
			}
		});
		
		buttonPrintBattLvls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printBattLvlsToDevice();
			}
		});
	}
	
	private boolean downJsonFile2Device(String strFilePath) {
		String strJsonFileName = textJsonFile.getText();
		 if (strJsonFileName == null)
		 {
			textLogInfo.append("File not exist");
			return false;
		 }
		 
		try
		 {
		 File file = new File(strJsonFileName);
		 
		 //save config
		 String strDirectory = file.getParent();
		 EslConfig.savePropertyValue(LAST_LOAD_JSON_FILE_PATH, strDirectory);
	    
		 String encoding = "GBK";
		 if (file.isFile() && file.exists()) 
		 { 
	        InputStreamReader read = new InputStreamReader(
	        		new FileInputStream(file), encoding);//考虑到编码格式
	        BufferedReader bufferedReader = new BufferedReader(read);
	        String strJsonFileContent = bufferedReader.readLine();
	        read.close();

	        if (strJsonFileContent != null){
	        	JSONObject jsonMsg = JSONObject.fromObject(strJsonFileContent);
	        	if (textDeviceID.getText().length() == 12) {
		        	jsonMsg.put("mac", textDeviceID.getText());
				}
				jsonMsg.put("seq", mMsgSequence++);
				return mMqttClient.pubCommand2Gateway(jsonMsg.toString());
	        }
		 }
		 }catch (Exception excpt) {
 		   excpt.printStackTrace();
 		} 
		return false;
	}

	private boolean downBmpFile2Device(String strFilePath) {
		try {
			if (textDeviceID.getText().length() != 12) {
				textLogInfo.append("device id is null\r\n");
				return false;
			}

			int nESLType = this.comboSizeTypeBox.getSelectedIndex();
			MTagType tagType = MTagType.MTagTypeFromID(nESLType);
			if (tagType == null) {
				System.out.println("please select ESL Type");
				return false;
			}
			File file = new File(strFilePath);
			
			//save path
			String strDirectory = file.getParent();
			EslConfig.savePropertyValue(LAST_LOAD_BMP_FILE_PATH,
					strDirectory);
			
			//download file
			if (file.isFile() && file.exists()) {
				
				Pic2MqttDataService dataService = new Bmp2MqttDataService();
				String bmpZipString = dataService.getCompressData(file, tagType,
						124, 0);
				
				mJsonMsg = new JSONObject();
				mJsonMsg.put("msg", "dData");
				mJsonMsg.put("mac", textDeviceID.getText());
				mJsonMsg.put("seq", mMsgSequence++);
				mJsonMsg.put("auth1", "00000000");
				mJsonMsg.put("dType", "ascii");
				mJsonMsg.put("data", bmpZipString);

				mMqttClient.pubCommand2Gateway(mJsonMsg.toString());
			} else {
				System.out.println("File not exist");
			}
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}

		return false;
	}
	
	private void printMacToDevice() {
		try {
			String strBleID = textDeviceID.getText();
			if (strBleID.length() != 12) {
				System.out.println("device id length invalid");
				return;
			}
			
			//esl type
			MTagType tagType;
			EslObject eslObj = this.mMqttMsgHandler.getEslObjByID(strBleID);	
			if (eslObj == null || eslObj.mEslType == null)
			{
				tagType = MTagType.MTagTypeFromID(this.comboSizeTypeBox.getSelectedIndex());
				if (tagType == null) {
					System.out.println("please select ESL Type");
					return;
				}
			}else{
				tagType = eslObj.mEslType;
			}
			
			PrintTextCfg textCfg = new PrintTextCfg();
			textCfg.lcdType = tagType;
			
			//Overwrite the previous picture, if you only append text, then using MTagType.LcdColorTranspant
			textCfg.nBkgColor = MTagType.LcdColorWhite; 
			
			textCfg.nPictrueID = 1;
			textCfg.nPictureNode = 0;
			textCfg.nStartRow = 2;
			textCfg.nStartColumn = 2;
			textCfg.strPrintText = strBleID.toUpperCase();
			if (tagType.is3Color())
			{
				textCfg.nTextColor = MTagType.LcdColorRed;
			}else{
				textCfg.nTextColor = MTagType.LcdColorBlack;
			}
			textCfg.nTextBkgColor = MTagType.LcdColorWhite; 
			String strData = textCfg.objectToMessage();
						
			//send the json message to device
			mJsonMsg = new JSONObject();
			mJsonMsg.put("msg", "dData");
			mJsonMsg.put("mac", strBleID);
			mJsonMsg.put("seq", mMsgSequence++);
			mJsonMsg.put("auth1", "00000000");
			mJsonMsg.put("dType", "hex");
			mJsonMsg.put("data", strData);
			mMqttClient.pubCommand2Gateway(mJsonMsg.toString());
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
	}
	
	private void printBattLvlsToDevice()
	{
		try {
			String strBleID = textDeviceID.getText();
			if (strBleID.length() != 12) {
				System.out.println("device id length invalid");
				return;
			}
			
			//esl type
			MTagType tagType;
			int nBattLvls = 0;
			EslObject eslObj = this.mMqttMsgHandler.getEslObjByID(strBleID);	
			if (eslObj == null || eslObj.mEslType == null)
			{
				System.out.println("not found the device, cant not get voltage");
				return;
			}else{
				tagType = eslObj.mEslType;
				nBattLvls = eslObj.mEslVoltage;
			}
			
			//esl type
			PrintTextCfg textCfg = new PrintTextCfg();
			textCfg.lcdType = tagType;
			
			//ESL background color, if you only append text, then using MTagType.LcdColorTranspant
			//if the color = MTagType.LcdColorWhite, then previous picture will be erase by white color
			textCfg.nBkgColor = MTagType.LcdColorTranspant; 

			textCfg.nPictrueID = 1;
			textCfg.nPictureNode = 0;
			textCfg.nStartRow = 5;
			textCfg.nStartColumn = 2;
			textCfg.strPrintText = "Batt:" + nBattLvls + "mv";
			
			if (tagType.is3Color())
			{
				//text color
				textCfg.nTextColor = MTagType.LcdColorRed;
				
				//background color of text area
				//check if need low battery warning
				if (nBattLvls < 2500){
					textCfg.nTextBkgColor = MTagType.LcdColorRed;
				}else{
					textCfg.nTextBkgColor = MTagType.LcdColorWhite;
				}
			}else{
				textCfg.nTextColor = MTagType.LcdColorBlack;
				textCfg.nTextBkgColor = MTagType.LcdColorWhite;
			}
			String strData = textCfg.objectToMessage();
						
			//send the json message to device
			mJsonMsg = new JSONObject();
			mJsonMsg.put("msg", "dData");
			mJsonMsg.put("mac", strBleID);
			mJsonMsg.put("seq", mMsgSequence++);
			mJsonMsg.put("auth1", "00000000");
			mJsonMsg.put("dType", "hex");
			mJsonMsg.put("data", strData);
			mMqttClient.pubCommand2Gateway(mJsonMsg.toString());
			
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
		
		return;
	}
	
	private void downQRCodeToDevice() {
		try {
			if (textDeviceID.getText().length() != 12) {
				System.out.println("device id length invalid");
				return;
			}
			
			//get esl type
			int nESLType = this.comboSizeTypeBox.getSelectedIndex();
			MTagType tagType = MTagType.MTagTypeFromID(nESLType);
			if (tagType == null) {
				System.out.println("please select ESL Type");
				return;
			}
			
		    //create qrcode bmp file
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CreateQRCode.createQRcode(textDeviceID.getText().toUpperCase(), outputStream);

			// create refresh block head
			PicturePartRefreshStru refreshStru = new PicturePartRefreshStru();
			refreshStru.lcdType = tagType;
			if (refreshStru.lcdType == null) {
				return;
			}
			refreshStru.nPartRefreshBkgColor = MTagType.LcdColorTranspant;
			refreshStru.nPictrueID = QRCODE_PICTURE_ID;
			refreshStru.nPictureNode = 0;
			refreshStru.appendRefreshBlock(0, 0, CreateQRCode.QRCODE_WIDTH, CreateQRCode.QRCODE_WIDTH,
					refreshStru.lcdType.getWidth() - CreateQRCode.QRCODE_WIDTH - 2, 2);

			//Convert QR code pictures to the message format required by ESL.
			Pic2MqttDataService dataService = new Bmp2MqttDataService();
			ByteArrayInputStream qrCodeStream = new ByteArrayInputStream(
					outputStream.toByteArray());
			outputStream.close();
			String bmpData = dataService.getPartionalCompressData(qrCodeStream,
					refreshStru);
			qrCodeStream.close();

			//send the json message to device
			mJsonMsg = new JSONObject();
			mJsonMsg.put("msg", "dData");
			mJsonMsg.put("mac", textDeviceID.getText());
			mJsonMsg.put("seq", mMsgSequence++);
			mJsonMsg.put("auth1", "00000000");
			mJsonMsg.put("dType", "ascii");
			mJsonMsg.put("data", bmpData);
			mMqttClient.pubCommand2Gateway(mJsonMsg.toString());
		} catch (Exception excpt) {
			excpt.printStackTrace();
		}
	}
	
	public boolean openJsonFile() {
		JFileChooser fileChooser = new JFileChooser();
	    
	    String strLastDirectory = EslConfig.getPropertyValue(LAST_LOAD_JSON_FILE_PATH, ".");
	    fileChooser.setCurrentDirectory(new File(strLastDirectory));
	    fileChooser.setAcceptAllFileFilterUsed(false);
	
	    final String[]fileEName = { ".json", "JSON文件(*.json)" };
	    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
	    	public boolean accept(File file) 
	    	{ 
	    		if (file.getName().endsWith(fileEName[0]) || file.isDirectory()) 
	    		{
	    			return true;
	    		}
	   
	    		return false;
	    	}
	    	
	    	public String getDescription() 
	    	{
	    		return fileEName[1];
	    	}
	     });
	
	    if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(null, null))
	    {
		    String strJsonFile = fileChooser.getSelectedFile().getAbsolutePath();  
		    textJsonFile.setText(strJsonFile);
	    }
	    
	    return true;
	}

	public boolean openBmpFile() {
		JFileChooser fileChooser = new JFileChooser();

		String strLastDirectory = EslConfig.getPropertyValue(
				LAST_LOAD_BMP_FILE_PATH, ".");
		fileChooser.setCurrentDirectory(new File(strLastDirectory));
		fileChooser.setAcceptAllFileFilterUsed(false);

		final String[] fileEName = { ".bmp", "bmp file(*.bmp)" };
		fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File file) {
				if (file.getName().endsWith(fileEName[0]) || file.isDirectory()) {
					return true;
				}

				return false;
			}

			public String getDescription() {
				return fileEName[1];
			}
		});

		if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(null, null)) {
			String strJsonFile = fileChooser.getSelectedFile()
					.getAbsolutePath();
			textBmpFile.setText(strJsonFile);
		}

		return true;
	}

	private void startConnectCloud() {
		String strMqttSrvAddr = textMqttSrv.getText();
		String strMqttPublishTopic = textGwID.getText();
		String strMqttUser = textMqttUser.getText();
		String strMqttPwd = textMqttPwd.getText();

		if (!mMqttClient.isConnected()) {
			EslConfig.savePropertyValue(CFG_MQTT_USR_PASSWORD, strMqttPwd);
			EslConfig.savePropertyValue(CFG_MQTT_USR_NAME, strMqttUser);
			EslConfig.savePropertyValue(CFG_MQTT_PUBLISH_TOPIC,
					strMqttPublishTopic);
			EslConfig.savePropertyValue(CFG_MQTT_SRV_URL, strMqttSrvAddr);

			mMqttClient.setConnectinInfo(strMqttSrvAddr, strMqttPublishTopic,
					strMqttUser, strMqttPwd);
			mMqttClient.connect();
		}
	}

	@Override
	public void connectionNotify(ConnectionNotify connNtf) {
		// TODO Auto-generated method stub
		if (connNtf == ConnectionNotify.CONN_NTF_CONNECED) {
			buttonConn.setEnabled(false);

			textLogInfo.append("Mqtt Server connected\r\n");
		} else if (connNtf == ConnectionNotify.CONN_NTF_DISCONNECTED) {
			buttonConn.setEnabled(true);
			buttonPushBmpMsg.setEnabled(false);
			buttonPushJsonMsg.setEnabled(false);
			buttonQRCode.setEnabled(false);
			buttonPrintMac.setEnabled(false);
			buttonPrintBattLvls.setEnabled(false);
			textLogInfo.append("Mqtt Server disconnected\r\n");
			// wait and reconnect
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					startConnectCloud();
				}
			}, 10000);
		} else if (connNtf == ConnectionNotify.CONN_SHAKE_SUCCESS) {
			buttonPushBmpMsg.setEnabled(true);
			buttonPushJsonMsg.setEnabled(true);
			buttonQRCode.setEnabled(true);
			buttonPrintMac.setEnabled(true);
			buttonPrintBattLvls.setEnabled(true);
			mShakeWithGwSucc = true;
			textLogInfo.append("Gateway shake success\r\n");
		}
	}

	@Override
	public void actionNotify(ActionNotify downNtf, String strDeviceMac, Object obj) {
		// TODO Auto-generated method stub

		if (downNtf == ActionNotify.MSG_DOWNLOAD_SUCCESS) {
			if (obj != null) {
				EslObject eslObj = (EslObject) obj;
				eslObj.mDownJsonNum++;
				textLogInfo
						.append(getCurrentTime() + strDeviceMac
								+ ", download msg succ:" + eslObj.mDownJsonNum
								+ "\r\n");
			}
		} else if (downNtf == ActionNotify.FOUND_DEVICE) {
			if (obj != null) {
				EslObject eslObj = (EslObject) obj;
				textLogInfo.append(getCurrentTime() + eslObj.mMacAddress
						+ ", found device:" + eslObj.mEslType.getName() + ", rssi" + eslObj.mRssi + "\r\n");
			}
		} else if (downNtf == ActionNotify.DEVICE_UPDATE) {

		} else if (downNtf == ActionNotify.MSG_EXECUTE_SUCCESS) {
			if (obj != null) {
				EslObject eslObj = (EslObject) obj;
				textLogInfo.append(getCurrentTime() + eslObj.mMacAddress
						+ ", execute msg succ:" + eslObj.mDownSuccNum + "\r\n");
				eslObj.mDownSuccNum++;
			}
		} else if (downNtf == ActionNotify.MSG_EXECUTE_FAIL) {
			if (obj != null) {
				EslObject eslObj = (EslObject) obj;
				textLogInfo.append(getCurrentTime() + eslObj.mMacAddress
						+ ", execute msg failed, err:" + eslObj.mCommandCause
						+ ", num:" + eslObj.mDownFailNum + "\r\n");
				eslObj.mDownFailNum++;
			}
		} else if (downNtf == ActionNotify.MSG_SHAKE_REQs) {
			if (obj != null) {
				EslShakeReq shakeReq = (EslShakeReq) obj;
				System.out.println(getCurrentTime()
						+ "receive Gw shake msg, adv num:"
						+ shakeReq.mAdvBuffDevNum + ", msg num:"
						+ shakeReq.mBuffDownMsgNum);
			}
		}
	}

	private String getCurrentTime() {
		long nCurrentTime = System.currentTimeMillis();

		Date date = new Date(nCurrentTime);
		return DATE_FORMAT.format(date) + " ";
	}

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
}
	