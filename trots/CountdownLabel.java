/*
$Log$
Revision 1.1  2003/10/10 10:16:50  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:09  dkelly


Revision 1.4  99/07/02  14:00:23  14:00:23  dkelly (Dave Kelly)
Fixed for Java 1.2

Changed Thread.stop() to Thread.interrupt()

Revision 1.3  99/04/19  10:31:01  10:31:01  dkelly (Dave Kelly)
Forced checkin

Revision 1.2  99/02/26  12:24:48  12:24:48  dkelly (Dave Kelly)
Added RCS statements

*/
import java.awt.Label;
import java.lang.Thread;

public class CountdownLabel extends Label implements Runnable
{
	private boolean working;
	private int timeLeft;
	//private int initialSeconds;
	private Thread thread;
	private String prefix;

	public void reset(int newTime)
	{
		timeLeft = newTime;
	}

        public CountdownLabel(int seconds)
	{
		working = false;
		timeLeft = seconds;
	}

	public void setPrefix(String s)
	{
		prefix = s;
	}

	public void countdown()
	{
		while(working && timeLeft > 0)
		{
                        this.setText(prefix + timeLeft);
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException exInt) {};
			timeLeft --;
		}
	}

	public void stop()
	{
		working = false;
		if(thread != null)
		{
			thread.interrupt();
		}
		thread = null;
	}

	public void go()
	{
		working = true;
		if(thread == null)
		{
			thread = new Thread(this);
			thread.start();
		}
	}

        @Override
        public void run()
        {
                countdown();
        }
}
