package battleship;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

public class YesNoDialog extends Dialog {
        private static final long serialVersionUID = 1L;

        Button btnYes,btnNo;
	
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
	}

        @SuppressWarnings("this-escape")
        public YesNoDialog(Frame f, String title, boolean modal, String message) {
                super(f, title, modal);
                buildDialog(f, title, modal, message, "Yes", "No");
        }

        @SuppressWarnings("this-escape")
        public YesNoDialog(Frame f, String title, boolean modal, String message, String yes, String no) {
                super(f, title, modal);
                buildDialog(f, title, modal, message, yes, no);
        }

        private void buildDialog(Frame f, String title, boolean modal, String message, String yes, String no) {
                String[] lines = message.split("\n");
                int j = lines.length;

                Panel pnlTop = new Panel(), pnlBottom = new Panel();
                Dimension s,d;

                this.setLayout(new BorderLayout());
                this.add("North", pnlTop);
                this.add("South", pnlBottom);
                btnYes = new Button(yes); btnNo = new Button(no);
                pnlBottom.add(btnYes);
                pnlBottom.add(btnNo);
                pnlTop.setLayout(new GridLayout(j, 1));
                for(int i=0;i<j;i++)
                {
                        Label l = new Label(lines[i]);
                        l.setAlignment(Label.CENTER);
                        pnlTop.add(l);
                }
                this.pack();
                s = Toolkit.getDefaultToolkit().getScreenSize();
                d = this.getSize();
                this.setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
        }

        public void addListener(ActionListener a) {
                btnYes.addActionListener(a);
                btnNo.addActionListener(a);
        }
}
