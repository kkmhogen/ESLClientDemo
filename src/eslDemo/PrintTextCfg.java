package eslDemo;

import pic2mqttdata.MTagType;
import pic2mqttdata.bmp.ByteTool;

public class PrintTextCfg {
	
    private static int FIXED_PRINT_HEAD_LEN = 10;
	public static int MSG_TYPE_PRINT_MAC = 6;
	public static int ENCODE_TYPE_V2 = 2;
	public static int FONT_TEXT_WIDTH = 8;
	public static int FONT_TEXT_HEIGH = 8;

    
	public MTagType lcdType;
	public int nPictrueID;            //picture id that will included in advertisement packet
	public int nPictureNode;          //picture node index that save on ESL
	public String strPrintText;         //the text that print
	public int nStartRow;
	public int nStartColumn;
	public int nBkgColor;  //ESL background color. 0:black,1:white,2:red,3:transparent
	public int nTextBkgColor;  //background color on text area
	public int nTextColor;     //text color
	
	public String objectToMessage() throws Exception
	{
		StringBuilder sb = new StringBuilder();
        
        //message type and encode type
        int nByte0 =  (MSG_TYPE_PRINT_MAC << 2);
        sb.append( ByteTool.int2HexString(nByte0) );
        
        //message length
		int nByte1 = FIXED_PRINT_HEAD_LEN + strPrintText.length();
		sb.append( ByteTool.int2HexString(nByte1) );
	    
        //background color and node index
        int nByte2 = (nBkgColor << 4) + nPictureNode;
        sb.append( ByteTool.int2HexString(nByte2) );

        //picture id
        int nPictureIDNetorder[] = ByteTool.tools_H2NL(nPictrueID);
        sb.append( ByteTool.ints2HexStringOrder(nPictureIDNetorder) );

        //font id and text num
        int nTextLen = (strPrintText.length() << 3);
        sb.append( ByteTool.int2HexString(nTextLen) );

		//x0 pos; 15~14 bit text bkg color; 13~12 bit font color; 11~0 x0 pos
		int nFontAndPos = 0;
		nFontAndPos = nTextBkgColor;
		nFontAndPos <<= 14;
		int textFontColor = (nTextColor << 12);
		nFontAndPos += textFontColor;
		int nStartXPos = FONT_TEXT_WIDTH * nStartColumn;
		int nEndXPos = nStartXPos + FONT_TEXT_WIDTH * strPrintText.length();
		if (nEndXPos >= lcdType.getWidth())
    	{
    		 String err="text end X position larger then lcd size";
             throw new Exception(err);
    	}
		nFontAndPos += nStartXPos;
        int nFontX0Array[] = ByteTool.tools_H2NS(nFontAndPos);
        sb.append( ByteTool.ints2HexStringOrder(nFontX0Array));
        
        //y0 pos; 15~12bit reserved; 11~0 y0 pos
		int sY0Pos = nStartRow * FONT_TEXT_HEIGH;
		if (sY0Pos + FONT_TEXT_HEIGH >= lcdType.getHeight())
    	{
    		 String err="text end Y position larger then lcd size";
             throw new Exception(err);
    	}
		int nY0PosArray[] = ByteTool.tools_H2NS(sY0Pos);
        sb.append( ByteTool.ints2HexStringOrder(nY0PosArray));


        //mac address
        byte[] utfString = strPrintText.getBytes("UTF-8");
        String strUtfText = ByteTool.bytes2HexStringOrder(utfString);
        sb.append(strUtfText);
        
        return sb.toString();
	}
}
