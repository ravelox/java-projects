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

import java.util.ArrayList;
import java.util.List;

class AllStats
{
        private static List<QueueStats> stats;
        private static int msgTotal;

        private static class QueueStats
        {
                int sentCount;
                int acceptedCount;
                int cancelledCount;
                String queue;

                QueueStats(String name)
                {
                        sentCount = 0;
                        acceptedCount = 0;
                        cancelledCount = 0;
                        queue = name;
                }

                void sent() { sentCount++; }
                void accepted() { acceptedCount++; }
                void cancelled() { cancelledCount++; }
        }

	public AllStats()
	{
                stats = new ArrayList<>();
                msgTotal = 0;
        }

        private QueueStats findStats(String name)
        {
                for(QueueStats s : stats)
                {
                        if(name.equalsIgnoreCase(s.queue)) return s;
                }
                return null;
        }

	public void sent(String name)
	{
                QueueStats s = findStats(name);
                if(s == null) return;
                s.sent();
                msgTotal++;
        }

	public void cancelled(String name)
	{
                QueueStats s = findStats(name);
                if(s == null) return;
                s.cancelled();
        }

	public void accepted(String name)
	{
                QueueStats s = findStats(name);
                if(s == null) return;
                s.accepted();
        }

	public void add(String name)
	{
                QueueStats s = findStats(name);
                if(s != null) return;
                s = new QueueStats(name);
                stats.add(s);
        }

	public void remove(String name)
	{
                QueueStats s = findStats(name);
                if(s == null) return;
                stats.remove(s);
        }

	public int size() { return stats.size(); };

        public String getQueue(int i)
        {
                if(stats.isEmpty() || (i+1) > stats.size()) return "";
                return stats.get(i).queue;
        }

        public int getSent(int i)
        {
                if(stats.isEmpty() || (i+1) > stats.size()) return 0;
                return stats.get(i).sentCount;
        }

        public int getAccepted(int i)
        {
                if(stats.isEmpty() || (i+1) > stats.size()) return 0;
                return stats.get(i).acceptedCount;
        }

        public int getCancelled(int i)
        {
                if(stats.isEmpty() || (i+1) > stats.size()) return 0;
                return stats.get(i).cancelledCount;
        }

	public String getQueueStats(String queueName)
	{
                QueueStats s = findStats(queueName);

                if(s == null) return "";

                return String.format("%s#%d#%d#%d", s.queue, s.sentCount, s.acceptedCount, s.cancelledCount);
        }

        public int getMsgTotal()
        {
                return msgTotal;
        }
}
