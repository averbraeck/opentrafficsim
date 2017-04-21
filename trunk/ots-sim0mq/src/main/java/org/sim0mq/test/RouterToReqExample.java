package org.sim0mq.test;

import java.util.Random;

import org.zeromq.ZMQ;

/**
 * Example from http://stackoverflow.com/questions/20944140/zeromq-route-req-java-example-does-not-work.
 * <p>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 20, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RouterToReqExample
{
    private static Random rand = new Random();

    private static final int NBR_WORKERS = 10;

    private static class Worker extends Thread
    {

        private String workerId;

        Worker(String workerId)
        {
            this.workerId = workerId;
        }

        @Override
        public void run()
        {
            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket worker = context.socket(ZMQ.REQ);
            worker.setIdentity(workerId.getBytes());

            worker.connect("tcp://localhost:5671");

            int total = 0;
            while (true)
            {
                // Tell the broker we're ready for work
                worker.send("Hi Boss");

                // Get workload from broker, until finished
                String workload = worker.recvStr();
                boolean finished = workload.equals("Fired!");
                if (finished)
                {
                    System.out.printf(workerId + " completed: %d tasks\n", total);
                    break;
                }
                total++;

                // Do some random work
                try
                {
                    Thread.sleep(rand.nextInt(500) + 1);
                }
                catch (InterruptedException e)
                {
                }
            }

            worker.close();
            context.term();
        }
    }

    /**
     * While this example runs in a single process, that is just to make it easier to start and stop the example. Each thread
     * has its own context and conceptually acts as a separate process.
     * @param args args, can be empty
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception
    {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket broker = context.socket(ZMQ.ROUTER);
        broker.bind("tcp://*:5671");

        // starting all workers
        for (int workerNbr = 0; workerNbr < NBR_WORKERS; workerNbr++)
        {
            Thread worker = new Worker("worker-" + workerNbr);
            worker.start();
        }

        // Run for five seconds and then tell workers to end
        long endTime = System.currentTimeMillis() + 5000;
        int workersFired = 0;
        while (true)
        {
            // Next message gives us least recently used worker
            String identity = broker.recvStr();
            broker.sendMore(identity);
            broker.recvStr(); // Envelope delimiter
            broker.recvStr(); // Response from worker
            broker.sendMore("");

            // Encourage workers until it's time to fire them
            if (System.currentTimeMillis() < endTime)
                broker.send("Work harder");
            else
            {
                broker.send("Fired!");
                if (++workersFired == NBR_WORKERS)
                    break;
            }
        }

        broker.close();
        context.term();
    }

}
