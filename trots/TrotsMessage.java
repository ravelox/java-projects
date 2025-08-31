/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:11  dkelly


Revision 1.6  99/07/19  12:15:49  12:15:49  dkelly (Dave Kelly)
Further attempts to bring the message to the front of the screen

Revision 1.5  99/07/19  10:20:36  10:20:36  dkelly (Dave Kelly)
Move toFront() so that it is called after the message is displayed

Revision 1.4  99/07/06  12:28:14  12:28:14  dkelly (Dave Kelly)
Removed NoFocusButton and put it in it's own file

Revision 1.3  99/07/02  10:14:29  10:14:29  dkelly (Dave Kelly)
General code cleanup.

Revision 1.2  99/04/22  16:29:27  16:29:27  dkelly (Dave Kelly)
Added NoFocusButton class to stop button being activated when the
message is being displayed

*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/*------------------------------------*/
/* Class to display the TROTS message */
/*------------------------------------*/
public class TrotsMessage
{
	public int instanceID;
	Frame		trotsFrame;
	NoFocusButton	btnAssign, btnDismiss;
	Label		lblUser, lblMessage;
	Random r = new Random();

/*-------------*/
/* Constructor */
/*-------------*/
        public TrotsMessage(ActionListener buttonListener)
	{
		Panel		pnlButtons, pnlMessage;

		trotsFrame = new Frame("New Call Notification");

		trotsFrame.setLayout(new GridLayout(2,1));
		
		trotsFrame.setBackground(Color.red);

		btnAssign = new NoFocusButton("Assign to Me");
		btnDismiss = new NoFocusButton("Dismiss");

		btnAssign.addActionListener(buttonListener);
		btnDismiss.addActionListener(buttonListener);

		pnlButtons = new Panel();
		pnlMessage = new Panel();

		lblUser = new Label();
		lblMessage = new Label();

		lblUser.setAlignment(Label.CENTER);
		lblMessage.setAlignment(Label.CENTER);

		lblUser.setBackground(Color.red);
		lblUser.setForeground(Color.white);

		lblMessage.setBackground(Color.red);
		lblMessage.setForeground(Color.white);

		trotsFrame.add(pnlMessage);
		trotsFrame.add(pnlButtons);

		pnlButtons.setLayout(new FlowLayout());
		pnlButtons.add(btnAssign);
		pnlButtons.add(btnDismiss);

		pnlMessage.setLayout(new GridLayout(2,1));
		pnlMessage.add(lblUser);
		pnlMessage.add(lblMessage);

		trotsFrame.setSize(500,150);
		trotsFrame.setLocation(Math.abs(r.nextInt() % 200), Math.abs(r.nextInt() % 200));
	}

	public void setDetails(int id, String from, String msgText)
	{
		this.instanceID = id;
		btnAssign.setActionCommand("A"+instanceID);
		btnDismiss.setActionCommand("D"+instanceID);
		lblUser.setText("From: " + from);
		lblMessage.setText(msgText);
	}

	public void show()
	{
		trotsFrame.setState(Frame.ICONIFIED);
		trotsFrame.setVisible(true);
		trotsFrame.toFront();
		trotsFrame.requestFocus();
		trotsFrame.setState(Frame.NORMAL);
		trotsFrame.repaint();
		Toolkit.getDefaultToolkit().beep();
	}

	public void hide()
	{
		trotsFrame.setVisible(false);
	}

	public void kill()
	{
		trotsFrame.setVisible(false);
	}

	public void update(String updateMsg)
	{
		lblMessage.setText(updateMsg);
		lblMessage.setBackground(Color.white);
		lblMessage.setForeground(Color.black);
		btnAssign.setEnabled(false);
		trotsFrame.repaint();
	}

	public void reset()
	{
		lblMessage.setBackground(Color.red);
		lblMessage.setForeground(Color.white);
		btnAssign.setEnabled(true);
		btnDismiss.setEnabled(true);
		trotsFrame.repaint();
	}
}
