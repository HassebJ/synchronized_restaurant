import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mac on 3/29/17.
 */
public class Diner  {
    Order order;
    AtomicBoolean isSeated;
    int arrivalTime;
    int id;
    static int count = 0;
    static int timeToEat = 30;


    public Diner(int time, int burgers, int fries, boolean coke, boolean sundae){
        this.arrivalTime = time;
        this.order = new Order( burgers, fries, coke, sundae);
        this.isSeated = new AtomicBoolean(false);
        id = count;
        count ++;
    }
    public static Diner [] getDiners(int numDiners){
        int timeElapsed = 0;
        Diner [] diners = new Diner[numDiners];
//        int timeAvailable = 120;

        for(int i = 0; i<diners.length; i++){

//            diners[i] = getDinerWithRandomOrder(timeElapsed);
            diners[i] = new Diner(timeElapsed, 2, 1, true, true  );

            if(timeElapsed < 120){
                timeElapsed+=150;
            }

        }
        return diners;
    }


    public static Diner getDinerWithRandomOrder(int time){

        return new Diner(time, getRandomInt(1, 10), getRandomInt(1,5), getRandomBool(), getRandomBool()  );
    }

    static int getRandomInt(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    private static boolean getRandomBool(){
        return ThreadLocalRandom.current().nextBoolean();
    }

    public void getSeatedAndWaitForOrder(){
        synchronized (Restaurant.NUM_TABLES) {
            if(this.id == 0){
                Restaurant.initializeStartTime(this.arrivalTime);
            }
            try{
                long arrivalDelay =  arrivalTime - Restaurant.getCurrentLogicalTime() ;
                if(arrivalDelay > 0){
                    Thread.sleep(arrivalDelay);
                }
            }catch (InterruptedException e){
                System.out.println("Diner: " + id + " interrupted before being seated because " + e);
            }

            while (Restaurant.getFreeTables() < 1 ) {
                try {
//                    System.out.println("Diner {" + id + "," + arrivalTime + "} waiting to be seated");
                    Restaurant.NUM_TABLES.wait();
                } catch (InterruptedException e) {
//                    System.out.println("Diner: "+ id + "," + arrivalTime + "} interrupted.");
                }

            }
            Restaurant.allocateTable(this);
        }
        this.isSeated.set(true);






        synchronized (this){
            try{
                while (!this.order.isPrepared.get()){
                    this.wait();
                }
                System.out.println("Order for Diner: " + id + " prepared by Chef: "+ this.order.preparedBy + " and brought to the table at time: "+ Restaurant.getCurrentLogicalTime() );
            }catch(InterruptedException e){
//                System.out.println("Diner {" + id +","+arrivalTime+ "} interrupted before eating because " + e.getCause() );
            }
        }




        eat();
    }


    public  void eat(){
        try{
//            System.out.println("Diner {" + id +","+arrivalTime+ "} started eating");
            Thread.sleep(Restaurant.convertPhysicalToLogicalTime(timeToEat));
        }catch (InterruptedException e){
            System.out.println("Diner with arrival time: {" + id +","+arrivalTime+ "} interrupted.");
        }
        Restaurant.reclaimTable();
//        System.out.println("Diner {" + id + "," + arrivalTime + "} finished  at: " + Restaurant.getCurrentLogicalTime() );
//        return;
        synchronized (Restaurant.NUM_TABLES){
            Restaurant.NUM_TABLES.notifyAll();
        }



    }


//    public  void placeOrderAndWait(Cook ourCook) {
//        try{
////            Cook ourCook = Restaurant.getAvailableCook();
//            ourCook.makeOrder(this.order);
//            while (!this.order.isPrepared){
//                this.order.wait();
//            }
//        }catch(Exception e){
//            System.out.println("Diner {" + id +","+arrivalTime+ "} interrupted before eating");
//        }
//        System.out.println("Diner {" + id +","+arrivalTime+ "} order made");
//
//        eat();
//
//    }




//    public void run () {
//
////
////
////
////
////        if(!isSeated){
////            //do something to get a seat
////        }
////        if(!notEatenYet){
////            //place order
////        }
////        if(!orderHasArrived){
////            //start eating
////        }
////        if(finishedEating){
////            //pay bill and leave
////        }
//
//        try {
//            Thread.sleep(Restaurant.convertPhysicalToLogicalTime(timeToEat));
//        }catch (InterruptedException e){
//            System.out.println("Diner with arrival time: "+arrivalTime+ " exited prematurely because of" + e.getMessage());
//        }
//    }
}
