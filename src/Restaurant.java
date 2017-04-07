import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mac on 3/29/17.
 */
public final class Restaurant {

//    enum Message {SEATED, }

    static int NUM_DINNERS = 3;
    public static AtomicInteger NUM_TABLES = new AtomicInteger(2);
    static int NUM_COOKS = 4;
    static Machine burgerMaker;
    static Machine friesMaker;
    static Machine sodaMaker;
    static Machine sundaeMaker;
    static AtomicInteger dinersServed = new AtomicInteger(0);
    static ExecutorService dinersToTablesService;
    static ExecutorService cooksThreadPool;
    static AtomicInteger logicalTime;
    static long startTime;


    static Diner [] diners ;
    static Cook [] cooks;
    static boolean ishutDownInitiate;

    static AtomicInteger availableCookIndex = new AtomicInteger(0);
    static public Queue <Diner> seatedDiners;


    static {
        burgerMaker = new Machine(MachineType.BURGER);
        friesMaker = new Machine(MachineType.FRIES);
        sodaMaker = new Machine(MachineType.SODA);
        sundaeMaker = new Machine(MachineType.ICECREAM);
        diners = Diner.getDiners(NUM_DINNERS);
        cooks = Cook.getCooks(NUM_COOKS);

        seatedDiners = new ConcurrentLinkedDeque<>();

        dinersToTablesService = Executors.newCachedThreadPool();
        cooksThreadPool = Executors.newFixedThreadPool(cooks.length);
        logicalTime = new AtomicInteger(0);
        ishutDownInitiate = false;
    }
    public static int getFreeTables(){
        return NUM_TABLES.get();
    }
    public static synchronized void allocateTable(Diner diner){

        System.out.println("Diner: " + diner.id + " SEATED at table: " + NUM_TABLES.getAndDecrement() + " at time: " + Restaurant.getCurrentLogicalTime());
        if(Restaurant.seatedDiners.size() == 0){
            Restaurant.initializeStartTime(diner.arrivalTime);
        }
        Restaurant.seatedDiners.add(diner);
        synchronized (Restaurant.seatedDiners){
            Restaurant.seatedDiners.notifyAll();
        }
//        return NUM_TABLES.getAndDecrement();
    }
    public static int reclaimTable(){
        dinersServed.incrementAndGet();
        synchronized (Restaurant.seatedDiners){
            Restaurant.seatedDiners.notifyAll();
        }
        return NUM_TABLES.incrementAndGet();
    }

    public static synchronized boolean  checkCookAvailable(){
        for(int i = 0; i<cooks.length; i++){
            if(cooks[i].isFree.get() == true){
                cooks[i].isFree.set(false);
                availableCookIndex.set(i);
                return true;
            }
        }
        return false;
    }
    public static Cook getAvailableCook(){
        return cooks[availableCookIndex.get()];
    }

    public static int numDinersServed(){
        return dinersServed.get();

    }

    public static Diner getNextSeatedDiner() {

        return seatedDiners.poll();
    }

    public static int convertPhysicalToLogicalTime(int minutes){
        logicalTime.addAndGet(minutes);
        return minutes;
    }

    public static long getCurrentLogicalTime(){

        return System.currentTimeMillis() - startTime;
    }
    static private Machine getMachine(MachineType type) throws InterruptedException{
        switch (type){
            case BURGER:
                return burgerMaker;
            case FRIES:
                return friesMaker;
            case SODA:
                return sodaMaker;
            case ICECREAM:
                return sundaeMaker;
            default:
                throw new InterruptedException();

        }
    }

    public synchronized static boolean isMachineAvailable(MachineType type) throws InterruptedException{
        Machine requestedMachine = getMachine(type);
        if(requestedMachine.isFree){
            requestedMachine.isFree = false;
            return true;
        }
        return requestedMachine.isFree;
    }
    public static void reclaimMachine(MachineType type) throws InterruptedException{
        Machine returnedMachine = getMachine(type);
        returnedMachine.isFree = true;
        synchronized (type){
            type.notifyAll();
        }


    }
    public synchronized static void  shutDown(){
        if(!ishutDownInitiate){
            ishutDownInitiate = true;
            System.out.println("LAST diner left the restaurant at time: " + Restaurant.getCurrentLogicalTime());
            dinersToTablesService.shutdownNow();
            cooksThreadPool.shutdown();
        }

    }


    public static void startSeatingDiners(){

        for(Diner diner: diners){
            dinersToTablesService.execute(() -> {diner.getSeatedAndWaitForOrder();} );
        }
    }

    public static void main (String []args){

        startSeatingDiners();

        for(int i = 0; i < cooks.length; i++){
            cooksThreadPool.execute(cooks[i]);
        }
    }

    public static void initializeStartTime(int arrivalTime){
        startTime = System.currentTimeMillis() - arrivalTime;
    }





}
