package concurrence;

import idgenerator.idservice.IDService;
import idgenerator.idservice.IDServiceParallel;
import concurrence.performanceclients.PerformanceClientFutures;
import concurrence.performanceclients.PerformanceClientThreads;
import concurrence.performanceclients.PerformanceClientThreadsCopilot;

public class PerformanceClientMain {
    public static void main(String[] args) {
        IDService idService = new IDService(1000000000L, 9999999999L);
        IDServiceParallel idServiceParallel = new IDServiceParallel(1000000000L, 99999999999L);

        PerformanceClientThreadsCopilot performanceClientThreadsCopilot = new PerformanceClientThreadsCopilot(idService);
        PerformanceClientThreads performanceClientThreads = new PerformanceClientThreads(idService);
        PerformanceClientFutures performanceClientFutures = new PerformanceClientFutures(idService);


        performanceClientThreads.run();
        //performanceClient.testRaceCondition();
    }
}
