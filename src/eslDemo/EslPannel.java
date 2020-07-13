package eslDemo;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import eslDemo.MqttEventNotify.ActionNotify;
import eslDemo.MqttEventNotify.ConnectionNotify;

public class EslPannel extends JPanel implements MqttEventNotify {
	private static int mMsgSequence = 101;
	private static final long serialVersionUID = 1L;

	private final static String CFG_MQTT_SRV_URL = "MqttSrvUrl";
	private final static String CFG_MQTT_PUBLISH_TOPIC = "MqttPublishTopic";
	private final static String LAST_LOAD_JSON_FILE_PATH = "LastJsonFilePath";
	private final static String CFG_MQTT_USR_NAME = "MqttUserName";
	private final static String CFG_MQTT_USR_PASSWORD = "MqttPassword";

	BeaconMqttClient mMqttClient; // mqtt connection
	BeaconMqttPushCallback mMqttMsgHandler;  //mqtt message handler
	
	private JButton buttonConn, buttonOpenFile, buttonPushMsg, buttonQRCode;
	private JTextField textMqttSrv, textGwID, textMqttUser, textMqttPwd,
			textJsonFile, textDeviceID;
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
		this.textJsonFile = new JTextField(30);
		this.buttonOpenFile = new JButton("OpenBmpFile");
		JPanel pannelOpenFile = new JPanel();
		pannelOpenFile.add(this.textJsonFile);
		pannelOpenFile.add(this.buttonOpenFile);
		pannelOpenFile.setLayout(new FlowLayout(FlowLayout.LEFT));
		devicePannel.add(pannelOpenFile);
		
		//qr code download
		JPanel pannelDownload = new JPanel();
		this.buttonPushMsg = new JButton("Down Picture");
		this.buttonQRCode = new JButton("Add QR Code");
		pannelDownload.add(this.buttonPushMsg);
		pannelDownload.add(this.buttonQRCode);
		this.buttonQRCode.setEnabled(false);
		this.buttonPushMsg.setEnabled(false);
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

		buttonOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openBmpFile();
			}
		});

		buttonPushMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String strJsonFileName = textJsonFile.getText();
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
			EslConfig.savePropertyValue(LAST_LOAD_JSON_FILE_PATH,
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
			CreateQRCode.createQRcode(textDeviceID.getText(), outputStream);

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

	public boolean openBmpFile() {
		JFileChooser fileChooser = new JFileChooser();

		String strLastDirectory = EslConfig.getPropertyValue(
				LAST_LOAD_JSON_FILE_PATH, ".");
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
			textJsonFile.setText(strJsonFile);
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
			buttonPushMsg.setEnabled(false);
			buttonQRCode.setEnabled(false);
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
			buttonPushMsg.setEnabled(true);
			buttonQRCode.setEnabled(true);
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
						+ ", found new device:" + eslObj.mEslType.getName() + "\r\n");
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