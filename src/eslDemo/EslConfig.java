package eslDemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;



public class EslConfig {
	private final static String CFG_FILE_NAME = "config.properties";
	public static String getPropertyValue(String strKey, String strDefaultValue)  {
		Properties pro = new Properties();
		String strRetValue = strDefaultValue;
		
		try
		{
			FileInputStream in = new FileInputStream(CFG_FILE_NAME);
			pro.load(in);
			strRetValue = pro.getProperty(strKey, strDefaultValue);
			
			in.close();
		}
		catch (FileNotFoundException fileNotFound)
		{
			System.err.println("Config file not found");
		}
		catch (IOException ioExcpt)
		{
			System.err.println("Read config io exception");
		}
		
		return strRetValue;
	}
	
	public static int getPropertyIntValue(String strKey, int nDefaultValue)  {
		Properties pro = new Properties();
		int nRetValue = nDefaultValue;
		
		try
		{
			FileInputStream in = new FileInputStream(CFG_FILE_NAME);
			pro.load(in);
			String strValue = pro.getProperty(strKey, null);
			if (strValue != null){
				nRetValue = Integer.valueOf(strValue);
			}
			
			in.close();
		}
		catch (FileNotFoundException fileNotFound)
		{
			System.err.println("Config file not found");
		}
		catch (IOException ioExcpt)
		{
			System.err.println("Read config io exception");
		}
		
		return nRetValue;
	}
	
	public static void savePropertyValue(String strKey, String strValue)  {
		Properties pro = new Properties();
		
		try
		{
			FileInputStream in = new FileInputStream(CFG_FILE_NAME);
			pro.load(in);  
			in.close();
		}
		catch (FileNotFoundException fileNotFound)
		{
			System.err.println("read config file not found");
		}
		catch (IOException ioExcpt)
		{
			System.err.println("Read config io exception");
		}
		
		try
		{
			FileOutputStream oFile = new FileOutputStream(CFG_FILE_NAME);
			pro.setProperty(strKey, strValue);
			pro.store(oFile, "config");
			oFile.close();
		}
		catch (FileNotFoundException fileNotFound)
		{
			System.err.println("Save config file not found");
		}
		catch (IOException ioExcpt)
		{
			System.err.println("Save config io exception");
		}
	}

}
