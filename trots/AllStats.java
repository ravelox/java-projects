/*
$Log$
Revision 1.1  2003/10/10 10:16:49  dkelly
Initial revision

Revision 1.1.1.1  2003/06/02 10:13:10  dkelly


Revision 1.4  99/07/09  16:49:58  16:49:58  dkelly (Dave Kelly)
Fixed getQueueStats compilation problem

Revision 1.3  99/07/09  16:39:18  16:39:18  dkelly (Dave Kelly)
Added getQueueStats to output a single line with all queue details

Revision 1.2  99/04/22  16:29:10  16:29:10  dkelly (Dave Kelly)
Added RCS log

*/

import java.util.*;

class AllStats
{
	private static Vector stats;
	public static int msgTotal;

	class queueStats
	{
		public int sentCount;
		public int acceptedCount;
		public int cancelledCount;
		public String queue;

		public queueStats(String name)
		{
			sentCount = 0;
			acceptedCount= 0;
			cancelledCount = 0;
			queue = name;
		}
		public void sent() { sentCount++; }
		public void accepted() { acceptedCount++; }
		public void cancelled() { cancelledCount++; }
	}

	public AllStats()
	{
		stats = new Vector();
		msgTotal = 0;
	}

	private queueStats findStats(String name)
	{
		queueStats s;
		
		for(int i=0;i<stats.size();i++)
		{
			s = (queueStats)stats.elementAt(i);

			if(name.equalsIgnoreCase(s.queue)) return s;
		}

		return null;
	}

	public void sent(String name)
	{
		queueStats s = findStats(name);
		if(s == null) return;
		s.sent();
		msgTotal++;
	}

	public void cancelled(String name)
	{
		queueStats s = findStats(name);
		if(s == null) return;
		s.cancelled();
	}

	public void accepted(String name)
	{
		queueStats s = findStats(name);
		if(s == null) return;
		s.accepted();
	}

	public void add(String name)
	{
		queueStats s = findStats(name);
		if(s != null) return;
		s = new queueStats(name);
		stats.addElement(s);
	}

	public void remove(String name)
	{
		queueStats s = findStats(name);
		if(s == null) return;
		stats.removeElement(s);
	}

	public int size() { return stats.size(); };

	public String getQueue(int i)
	{
		queueStats s;
		if(stats.size() <= 0 || (i+1) > stats.size() ) return new String();
		s = (queueStats)stats.elementAt(i);
		return s.queue;
	}

	public int getSent(int i)
	{
		queueStats s;
		if(stats.size() <= 0 || (i+1) > stats.size() ) return 0;
		s = (queueStats)stats.elementAt(i);
		return s.sentCount;
	}

	public int getAccepted(int i)
	{
		queueStats s;
		if(stats.size() <= 0 || (i+1) > stats.size() ) return 0;
		s = (queueStats)stats.elementAt(i);
		return s.acceptedCount;
	}

	public int getCancelled(int i)
	{
		queueStats s;
		if(stats.size() <= 0 || (i+1) > stats.size() ) return 0;
		s = (queueStats)stats.elementAt(i);
		return s.cancelledCount;
	}

	public String getQueueStats(String queueName)
	{
		queueStats s = findStats(queueName);

		if(s == null) return "";

		return new StringBuffer(s.queue).append("#").append(s.sentCount).append("#").append(s.acceptedCount).append("#").append(s.cancelledCount).toString();
	}
}
