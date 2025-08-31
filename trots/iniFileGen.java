/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.4  99/07/05  15:54:33  15:54:33  dkelly (Dave Kelly)
Forced checkin after benchmarking

Revision 1.3  99/07/02  14:01:05  14:01:05  dkelly (Dave Kelly)
Changed for Java 1.2

Changed Properties.save() to Properties.store()

Revision 1.2  99/04/22  16:33:30  16:33:30  dkelly (Dave Kelly)
Added RCS

*/

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

public class iniFileGen extends Observable
{
	Properties props;
	String iniFileName;
	Dialog iniDialog;
        List<TextField> inputValues;
        boolean OK = true;

        private ActionListener buttonListener = new ActionListener()
        {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                        String command = e.getActionCommand();

                        if(command.equalsIgnoreCase("OK"))
                        {
                                OK = checkAllInput();
                                if(!OK) return;

                                for(TextField t : inputValues)
                                {
                                        props.put(t.getName(), t.getText());
                                }
                                try(FileOutputStream out = new FileOutputStream(iniFileName))
                                {
                                        props.store(out, "Updated by iniFileGen");
                                }
                                catch(IOException ex) { /* ignore */ }

/*-----------------------------------------------------*/
/* Tell anyone who's listening that we've been updated */
/*-----------------------------------------------------*/
				tellObservers();
			}
			iniDialog.setVisible(false);
		}
	};

	public void tellObservers()
	{
		setChanged();
		notifyObservers();
	}

/*-------------*/
/* Constructor */
/*-------------*/
	public iniFileGen(String title, String fileName, String propDetails[][])
	{
		int i;
		TextField t;
		//Label l;
		Button btnCancel, btnOK;
		Panel	pnlValues, pnlButtons;
		Frame frame;
		
		pnlValues = new Panel();
		pnlValues.setLayout(new GridLayout(propDetails.length,2));

		pnlButtons = new Panel();
		pnlButtons.setLayout(new FlowLayout());

                inputValues = new ArrayList<>();
		frame = new Frame(title);
		iniDialog = new Dialog(frame,title, true);
		iniDialog.setLayout(new BorderLayout());
		
		iniFileName = fileName;
	
		load(propDetails);

		for(i=0;i<propDetails.length;i++)
		{
			pnlValues.add(new Label(propDetails[i][0]));
			t = new TextField(props.getProperty(propDetails[i][1]));
			t.setName(propDetails[i][1]);
			pnlValues.add(t);
                        inputValues.add(t);
		}

		btnCancel = new Button("Cancel");
		btnCancel.addActionListener(buttonListener);

		btnOK = new Button("OK");
		btnOK.addActionListener(buttonListener);

		pnlButtons.add(btnOK); pnlButtons.add(btnCancel);
		iniDialog.add("North",pnlValues);
		iniDialog.add("South",pnlButtons);
		iniDialog.pack();
		
		OK = checkAllInput();
	}

	public void load(String propDetails[][])
	{
		int i;

                props = new Properties();
                if(new File(iniFileName).exists())
                {
                        try(FileInputStream in = new FileInputStream(iniFileName))
                        {
                                props.load(in);
                        }
                        catch(IOException e) { /* ignore */ }
                }

		for(i = 0; i < propDetails.length; i++)
		{
			if(props.getProperty(propDetails[i][1]) == null)
			{
				props.put(propDetails[i][1],"");
			}
		}
	}

	public void show()
	{	
		iniDialog.setVisible(true);
	}

	public void hide()
	{
		iniDialog.setVisible(false);
	}

	public String getProperty(String name)
	{
		return(props.getProperty(name));
	}

	private boolean checkAllInput()
	{
                boolean status = true;
                boolean focusSet = false;

                for(TextField t : inputValues)
                {
                        if(t.getText() == null || t.getText().isEmpty())
                        {
                                t.setBackground(Color.red);
                                t.setForeground(Color.white);
                                status = false;
                                if(!focusSet)
                                {
                                        t.requestFocus();
                                        focusSet = true;
                                }
                        }
                        else
                        {
                                t.setBackground(Color.white);
                                t.setForeground(Color.black);
                        }
                }

                return status;
        }
}
