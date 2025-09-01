import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

public class YesNoDialog extends Dialog
{
	Button btnYes,btnNo;
	String dlgName;
	
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
	}

	public YesNoDialog(Frame f, String title, boolean modal, String message)
	{
		super(f, title, modal);
		buildDialog(f, title, modal, message, "Yes", "No");
	}

	public YesNoDialog(Frame f, String title, boolean modal, String message, String yes, String no)
	{
		super(f, title, modal);
		buildDialog(f, title, modal, message, yes, no);
	}

	public void buildDialog(Frame f, String title, boolean modal, String message, String yes, String no)
	{
		StringTokenizer st;
		int i,j;


		Panel pnlTop = new Panel(), pnlBottom = new Panel();
		Dimension s,d;

		this.setLayout(new BorderLayout());
		this.add("North", pnlTop);
		this.add("South", pnlBottom);
		btnYes = new Button(yes); btnNo = new Button(no);
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
	}

	public void addListener(ActionListener a)
	{
		btnYes.addActionListener(a);
		btnNo.addActionListener(a);
	}
}
