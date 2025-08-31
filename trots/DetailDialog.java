/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.1  99/07/13  12:45:35  12:45:35  dkelly (Dave Kelly)
Initial revision

*/

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class DetailDialog extends Dialog
{
	Button btnSpecial,btnNo;
	String dlgName;
	
	ActionListener noButtonListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	};

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
	}

	public DetailDialog(Frame f, String title, String detailNames[], String details, String specialButtonTitle, String noButtonTitle)
	{
		super(f, title, true);

		Panel pnlTop = new Panel(), pnlBottom = new Panel();
		Dimension s,d;

		this.setLayout(new BorderLayout());
		this.add("North", pnlTop);
		this.add("South", pnlBottom);
		btnSpecial = new Button(specialButtonTitle); btnNo = new Button(noButtonTitle);
		pnlBottom.add(btnSpecial);
		pnlBottom.add(btnNo);
		pnlTop.setLayout(new GridLayout(detailNames.length, 2));
		StringTokenizer st = new StringTokenizer(details, "#");
		for(int i=0;i<detailNames.length;i++)
		{
			pnlTop.add(new Label(detailNames[i] + ":"));
			pnlTop.add(new Label(st.nextToken()));
		}
		this.pack();
		s = Toolkit.getDefaultToolkit().getScreenSize();
		d = this.getSize();
		this.setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
		btnNo.addActionListener(noButtonListener);
	}

	public void addSpecialListener(ActionListener a)
	{
		btnSpecial.addActionListener(a);
	}

	public void setName(String name, String buttonActionCommand)
	{
		btnSpecial.setName(name);
		btnSpecial.setActionCommand(buttonActionCommand);
	}
}
