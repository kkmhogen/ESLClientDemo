package eslDemo;


import javax.swing.*;

	
public class EslMain extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EslMain(EslPannel pannel)
	{
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600, 800);
		this.setTitle("Mqtt Demo");
		this.add(pannel);
		this.setResizable(false);
		this.setVisible(true);
	}

	public static void main(String[] args)
	{
		EslPannel panel = new EslPannel();
		new EslMain(panel);
	}
}
