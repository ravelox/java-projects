import java.applet.*;
import java.awt.*;
import java.io.IOException;
import java.util.StringTokenizer;

public class ScoreClient extends Applet
{
        Label lblMessage;
        TextField txtPlayerName;
        Label[] lblPlayerName;
        Label[] lblPlayerScore;

        @Override
        public void init()
        {
                Panel pnlScores,pnlText;
                lblPlayerName = new Label[10];
                lblPlayerScore = new Label[10];

                pnlText = new Panel(new BorderLayout());
                txtPlayerName = new TextField(getParameter("game"));
                pnlText.add("North", txtPlayerName);
                lblMessage = new Label(getParameter("game"));
                lblMessage.setForeground(Color.white);
                lblMessage.setBackground(Color.black);
                lblMessage.setAlignment(Label.CENTER);
                pnlText.add("South",lblMessage);

                pnlScores = new Panel(new GridLayout(10, 2));
                for(int i = 0; i < 10 ; i++)
                {
                        lblPlayerName[i] = new Label("Player " + i);
                        pnlScores.add(lblPlayerName[i]);
                        lblPlayerScore[i] = new Label("" + i);
                        lblPlayerScore[i].setAlignment(Label.RIGHT);
                        pnlScores.add(lblPlayerScore[i]);
                }
                setLayout(new BorderLayout());
                add("North",pnlText);
                add("Center",pnlScores);

                updateScores();
        }

        public void addScore(String name, int score)
        {
                try (DataConnection d = new DataConnection(getParameter("server"), 9800))
                {
                        d.dataOut.writeBytes("add#" + getParameter("game") + "#");
                        d.dataOut.writeBytes(score + "#");
                        d.dataOut.writeBytes(name + "\n");
                }
                catch(IOException exIO) {}

                updateScores();
        }

        public void updateScores()
        {
                StringTokenizer st1, st2;
                String scoreString;

                try (DataConnection d = new DataConnection(getParameter("server"), 9800))
                {
                        d.dataOut.writeBytes("get#"+getParameter("game")+"\n");

                        scoreString = d.textIn.readLine();

                        st1 = new StringTokenizer(scoreString, ";");

                        for(int i=0; i < 10; i++)
                        {
                                String player = st1.nextToken();
                                st2 = new StringTokenizer(player, "#");

                                String pname = st2.nextToken();
                                String pscore = st2.nextToken();

                                lblPlayerName[i].setText(pname);
                                lblPlayerScore[i].setText(pscore);
                        }
                }
                catch(IOException exIO) {}
        }

        public void newScore(int score)
        {
                try (DataConnection d = new DataConnection(getParameter("server"), 9800))
                {
                        d.dataOut.writeBytes("chk#"+getParameter("game")+"#"+score+"\n");

                        if(d.dataIn.readInt() > 0)
                        {
                                addScore(txtPlayerName.getText(), score);
                                lblMessage.setBackground(Color.red);
                                lblMessage.setForeground(Color.white);
                                lblMessage.setText("High Score");
                        }
                        else
                        {
                                lblMessage.setBackground(Color.black);
                                lblMessage.setForeground(Color.white);
                                lblMessage.setText(getParameter("game"));
                        }
                }
                catch(IOException exIO) { lblMessage.setText("Uh Oh!"); }
        }

        @Override
        public void paint(Graphics g)
        {
                Dimension d = this.getSize();

                g.drawRect(2, 2, d.width - 2, d.height - 2);
        }
}
