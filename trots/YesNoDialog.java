/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.2  99/07/19  10:43:33  10:43:33  dkelly (Dave Kelly)
Modified code to display message with line breaks correctly

Revision 1.1  99/07/13  12:48:09  12:48:09  dkelly (Dave Kelly)
Initial revision

*/

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class YesNoDialog extends Dialog
{
	Button btnYes,btnNo;
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

	public YesNoDialog(Frame f, String title, boolean modal, String message)
	{
		super(f, title, modal);

		StringTokenizer st;
		int i,j;


		Panel pnlTop = new Panel(), pnlBottom = new Panel();
		Dimension s,d;

		this.setLayout(new BorderLayout());
		this.add("North", pnlTop);
		this.add("South", pnlBottom);
		btnYes = new Button("Yes"); btnNo = new Button("No");
		pnlBottom.add(btnYes);
		pnlBottom.add(btnNo);
		st = new StringTokenizer(message, "\n");
		j = st.countTokens();
		pnlTop.setLayout(new GridLayout(j, 1));
		for(i=0;i<j;i++)
		{
			Label l = new Label(st.nextToken());
			l.setAlignment(Label.CENTER);
			pnlTop.add(l);
		}
		this.pack();
		s = Toolkit.getDefaultToolkit().getScreenSize();
		d = this.getSize();
		this.setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
		btnNo.addActionListener(noButtonListener);
	}

	public void addYesListener(ActionListener a)
	{
		btnYes.addActionListener(a);
	}

	public void setName(String name)
	{
		btnYes.setName(name);
	}
}
