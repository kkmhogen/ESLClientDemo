package eslDemo;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import eslDemo.BeaconMqttPushCallback.EslObject;

 
public class EslPannel extends JPanel implements MqttConnNotify{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static String CFG_MQTT_SRV_URL = "MqttSrvUrl";
	private final static String CFG_MQTT_PUBLISH_TOPIC = "MqttPublishTopic";
	private final static String LAST_LOAD_JSON_FILE_PATH = "LastJsonFilePath";
	private final static String CFG_MQTT_USR_NAME = "MqttUserName";
	private final static String CFG_MQTT_USR_PASSWORD = "MqttPassword";

	BeaconMqttClient mMqttClient;  //mqtt connection
    
 
	private JLabel labelMqttSrv, labelGwID, labelMqttUser, labelMqttPwd;            
	
	private JButton buttonConn, buttonOpenFile, buttonPushMsg;        
	private JTextField textMqttSrv, textGwID, textMqttUser, textMqttPwd, textJsonFile;             
	private JTextArea textLogInfo;
    private JPanel pannelMqttSrv, pannelGwID, pannelUser, pannelPwd, pannelLogin, pannelLogInfo, pannelDownload;
	
    public EslPannel(){
    	mMqttClient = new BeaconMqttClient(this); 

    	this.labelMqttSrv = new JLabel("Address");
    	this.labelGwID = new JLabel("Subscribe");
    	this.labelMqttUser = new JLabel("User Name");
    	this.labelMqttPwd = new JLabel("User Pwd");
    	
    	this.textMqttSrv = new JTextField(30);
    	this.textGwID = new JTextField(30);
    	this.textMqttUser = new JTextField(10);
    	this.textMqttPwd = new JTextField(10);
    	this.textJsonFile = new JTextField(30);
    	this.textLogInfo = new JTextArea(6,30);
    	JScrollPane scroll = new JScrollPane(textLogInfo); 
    	scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 

    	this.buttonConn = new JButton("Connected");
    	this.buttonPushMsg = new JButton("Download");
    	buttonPushMsg.setEnabled(false);
    	this.buttonOpenFile = new JButton("OpenFile");
    	
    	this.pannelMqttSrv = new JPanel();
    	this.pannelGwID = new JPanel();
    	this.pannelUser = new JPanel();
    	this.pannelPwd = new JPanel();
    	this.pannelLogin = new JPanel();
    	this.pannelDownload = new JPanel();
    	this.pannelLogInfo = new JPanel();
    	
    	this.setLayout(new GridLayout(7, 1));  //网格式布局
    	
    	this.pannelMqttSrv.add(this.labelMqttSrv);
    	this.pannelMqttSrv.add(this.textMqttSrv);
    	this.pannelMqttSrv.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
    	this.pannelGwID.add(this.labelGwID);
    	this.pannelGwID.add(this.textGwID);
    	this.pannelGwID.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
     	this.pannelUser.add(this.labelMqttUser);
    	this.pannelUser.add(this.textMqttUser);
    	this.pannelUser.setLayout(new FlowLayout(FlowLayout.LEFT));
    
     	this.pannelPwd.add(this.labelMqttPwd);
    	this.pannelPwd.add(this.textMqttPwd);
    	this.pannelPwd.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
    	this.pannelLogin.add(this.buttonConn);
    	this.pannelLogin.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
    	this.pannelLogInfo.add(scroll);
    	this.pannelLogInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
    	this.pannelDownload.add(this.textJsonFile);
    	this.pannelDownload.add(this.buttonOpenFile);
    	this.pannelDownload.add(this.buttonPushMsg);
    	this.pannelDownload.setLayout(new FlowLayout(FlowLayout.LEFT));
    	
    	
    	this.add(this.pannelMqttSrv);
    	this.add(this.pannelGwID);
    	this.add(this.pannelUser);
    	this.add(this.pannelPwd);
    	this.add(this.pannelLogin);
    	this.add(this.pannelLogInfo);	
    	this.add(this.pannelDownload);	
    	
    	String strMqttSrv = EslConfig.getPropertyValue(CFG_MQTT_SRV_URL, mMqttClient.getHostAddr());
    	this.textMqttSrv.setText(strMqttSrv);
    	
    	String strMqttPublishTopic = EslConfig.getPropertyValue(CFG_MQTT_PUBLISH_TOPIC, mMqttClient.getPublishTopic());
    	this.textGwID.setText(strMqttPublishTopic);
    	
    	String strMqttUserName = EslConfig.getPropertyValue(CFG_MQTT_USR_NAME, mMqttClient.getUserName());
    	this.textMqttUser.setText(strMqttUserName);
    	
    	String strMqttUserPassword = EslConfig.getPropertyValue(CFG_MQTT_USR_PASSWORD, mMqttClient.getPassword());
    	this.textMqttPwd.setText(strMqttUserPassword);
    	addClickListener();
    }
    
    private void addClickListener()
    {
    	buttonConn.addActionListener(new ActionListener(){
    		 public void actionPerformed(ActionEvent e) {
    			 String strMqttSrvAddr = textMqttSrv.getText();
    			 String strMqttPublishTopic = textGwID.getText();
    			 String strMqttUser = textMqttUser.getText();
    			 String strMqttPwd = textMqttPwd.getText();
    			 
    			 if (!mMqttClient.isConnected())
    			 {
    				 EslConfig.savePropertyValue(CFG_MQTT_USR_PASSWORD, strMqttPwd);
    				 EslConfig.savePropertyValue(CFG_MQTT_USR_NAME, strMqttUser);
    				 EslConfig.savePropertyValue(CFG_MQTT_PUBLISH_TOPIC, strMqttPublishTopic);
    				 EslConfig.savePropertyValue(CFG_MQTT_SRV_URL, strMqttSrvAddr);
    				 
    				 mMqttClient.setConnectinInfo(strMqttSrvAddr, strMqttPublishTopic, strMqttUser, strMqttPwd);
    				 mMqttClient.connect();
    			 }
             }
        });
    	
    	
    	buttonOpenFile.addActionListener(new ActionListener(){
      		 public void actionPerformed(ActionEvent e)
      		 {
      			openJsonFile();
      		 }
          });
    	
    	buttonPushMsg.addActionListener(new ActionListener(){
     		 public void actionPerformed(ActionEvent e)
     		 {
     			 String strJsonFileName = textJsonFile.getText();
     			 if (strJsonFileName == null)
     			 {
     				textLogInfo.append("File not exist");
     				return;
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
	     		        String lineTxt = bufferedReader.readLine();
	     		        read.close();
	     		        if (lineTxt.length() > 0)
	     		        {
	     		        	System.out.println("read file, length:" + lineTxt.length());
	     		        	mMqttClient.pubCommand2Gateway(lineTxt);
	     		        }
	     			 } 
	     			 else 
	     			 {
	     		        System.out.println("File not exist");
	     			 }
	     		} catch (Exception excpt) {
	     		   excpt.printStackTrace();
	     		} 
 			 }
         });
    }
    
    public boolean openJsonFile()
    {
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
    

	@Override
	public void connectionNotify(MqttConnNotify.ConnectionNotify connNtf) {
		// TODO Auto-generated method stub
		if (connNtf == ConnectionNotify.CONN_NTF_CONNECED)
		{
			buttonConn.setEnabled(false);
			textLogInfo.append("Mqtt Server connected\r\n");
		}
		else if (connNtf == ConnectionNotify.CONN_NTF_DISCONNECTED)
		{
			buttonConn.setEnabled(true);
			buttonPushMsg.setEnabled(false);
			textLogInfo.append("Mqtt Server disconnected\r\n");
		}
		else if (connNtf == ConnectionNotify.CONN_SHAKE_SUCCESS)
		{
			buttonPushMsg.setEnabled(true);
			textLogInfo.append("Gateway shake success\r\n");
		}
	}

	@Override
	public void actionNotify(MqttConnNotify.ActionNotify downNtf, Object obj) {
		// TODO Auto-generated method stub
		if (downNtf == ActionNotify.MSG_DOWNLOAD_SUCCESS)
		{
			if (obj != null)
			{
				EslObject eslObj = (EslObject)obj;
				textLogInfo.append(eslObj.mMacAddress + ", download msg succ\r\n");
			}
		}
		else if (downNtf == ActionNotify.FOUND_DEVICE)
		{
			if (obj != null)
			{
				EslObject eslObj = (EslObject)obj;
				textLogInfo.append(eslObj.mMacAddress + ", found new device succ\r\n");
			}
		}
		else if (downNtf == ActionNotify.MSG_EXECUTE_SUCCESS)
		{
			if (obj != null)
			{
				EslObject eslObj = (EslObject)obj;
				textLogInfo.append(eslObj.mMacAddress + ", execute msg succ\r\n");
			}
		}
		else if (downNtf == ActionNotify.MSG_EXECUTE_FAIL)
		{
			if (obj != null)
			{
				EslObject eslObj = (EslObject)obj;
				textLogInfo.append(eslObj.mMacAddress + ", execute msg failed, err:" + eslObj.mCommandCause + "\r\n");
			}
		}
	}
}