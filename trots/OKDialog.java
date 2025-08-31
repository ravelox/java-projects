/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.2  99/07/19  10:19:35  10:19:35  dkelly (Dave Kelly)
Modified code to display lines with CR breaks correctly

Revision 1.1  99/07/13  12:46:50  12:46:50  dkelly (Dave Kelly)
Initial revision

*/

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class OKDialog extends Dialog
{
	ActionListener OKButtonListener = new ActionListener()
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

	public OKDialog(Frame f, String title, boolean modal, String message)
	{
		super(f, title, modal);

		Panel pnlTop = new Panel(), pnlBottom = new Panel();
		Dimension s,d;
		Button btnOK;
		StringTokenizer st;
		int i,j;

		this.setLayout(new BorderLayout());
		this.add("North", pnlTop);
		this.add("South", pnlBottom);
		btnOK = new Button("OK");
		pnlBottom.add(btnOK);
		st = new StringTokenizer(message , "\n");
		j = st.countTokens();
		pnlTop.setLayout(new GridLayout(j,1));
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
		btnOK.addActionListener(OKButtonListener);
	}
}
