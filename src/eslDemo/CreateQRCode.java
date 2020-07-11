package eslDemo;
import com.swetake.util.Qrcode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by BigRoc on 2017/9/8.
 */
public class CreateQRCode 
{
    public static void createQRcode(String qrData, File outputFile) throws IOException {

    	BufferedImage imageBuffer = createQRcode(qrData);
        
        ImageIO.write(imageBuffer, "bmp", outputFile);
    }
    
    public static void createQRcode(String qrData, OutputStream outputStream) throws IOException {

    	BufferedImage imageBuffer = createQRcode(qrData);
        
        ImageIO.write(imageBuffer, "bmp", outputStream);
    }
    
    private static BufferedImage createQRcode(String qrData) throws IOException
    {
    	//�����ά��ͼƬ�ĸ߿��
        // API�ĵ��涨����ͼƬ��ߵķ�ʽ ��v�Ǳ��β��Եİ汾��
        int v =3;
        int width = 66 + 12 * (v - 1);
        int height = 66 + 12 * (v - 1);


        Qrcode x = new Qrcode();
        /**
         * ����ȼ���Ϊ
         * level L : ��� 7% �Ĵ����ܹ���������
         * level M : ��� 15% �Ĵ����ܹ���������
         * level Q : ��� 25% �Ĵ����ܹ���������
         * level H : ��� 30% �Ĵ����ܹ���������
         */
        x.setQrcodeErrorCorrect('L');
        x.setQrcodeEncodeMode('B');//ע��汾��Ϣ N�������� ��A���� a-z,A-Z��B���� ����)
        x.setQrcodeVersion(v);//�汾��  1-40

        byte[] d = qrData.getBytes("utf-8");//����ת��ʽ��Ҫ�׳��쳣

        //������
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);

        //��ͼ
        Graphics2D gs = bufferedImage.createGraphics();

        gs.setBackground(Color.WHITE);
        gs.setColor(Color.BLACK);
        gs.clearRect(0, 0, width, height);

        //ƫ����
        int pixoff = 2;


        /**
         * ���ײȿӵĵط�
         * 1.ע��forѭ�������i��j��˳��
         *   s[j][i]��ά�����j��i��˳��Ҫ����������е� gs.fillRect(j*3+pixoff,i*3+pixoff, 3, 3);
         *   ˳��ƥ�䣬�������ֽ���ͼƬ��һ������
         * 2.ע����ж�if (d.length > 0 && d.length < 120)
         *   �Ƿ�������ַ������ȴ���120�������ɴ��벻ִ�У���ά��հ�
         *   �����Լ����ַ�����С�����ô�����
         */
        if (d.length > 0 && d.length < 120) {
            boolean[][] s = x.calQrcode(d);

            for (int i = 0; i < s.length; i++) {
                for (int j = 0; j < s.length; j++) {
                    if (s[j][i]) {
                        gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
                    }
                }
            }
        }
        gs.dispose();
        bufferedImage.flush();
        
        return bufferedImage;
    }
}