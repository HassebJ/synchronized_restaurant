import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mac on 3/29/17.
 */
public class Cook implements Runnable {
    AtomicBoolean isFree = new AtomicBoolean(true);

//    synchronized void serveDiner(){
//
////        if(Restaurant. > 0){
////
////            if(dinersServed.get() < NUM_DINNERS)
////                dinersToTablesService.execute(getNextWaitingDiner());
////
////
////        }
//    }

    public String getId(){
        return Thread.currentThread().getName().substring(Thread.currentThread().getName().length()-1);
    }
    @Override
    public void run(){
//        System.out.println("cooks started");

        while(Restaurant.numDinersServed() < Restaurant.NUM_DINNERS){
            Diner dinerToServe = Restaurant.getNextSeatedDiner();
            if(dinerToServe != null){
                this.makeOrder(dinerToServe.order);
                synchronized (dinerToServe){
                    dinerToServe.notifyAll();
//                    System.out.println("Diner " + dinerToServe.id + " notified of order");
                }

//                dinerToServe.placeOrderAndWait(this);
            }else{
                synchronized (Restaurant.seatedDiners){
                    try{
                        Restaurant.seatedDiners.wait();
                    }catch(InterruptedException e){
                        System.out.println("Cook interrupted by exception "+ e);
                    }

                }

//                System.out.println("getting null dinners");
            }

//            System.out.println(Restaurant.numDinersServed()+ " " +  Restaurant.NUM_DINNERS);
        }


//        System.out.println("cooks finished " + Thread.currentThread().getName());
        Restaurant.shutDown();

    }

    static Cook [] getCooks(int numCooks){

        Cook [] cooks = new Cook[numCooks];
        for(int i = 0; i<cooks.length; i++){
            cooks[i] = new Cook();
        }

        return cooks;
    }

    private void cookItem(MachineType type, int quantity) throws InterruptedException{
        synchronized (type){
            while(!Restaurant.isMachineAvailable(type)){
                type.wait();

            }
            System.out.println(type + " machine used at time: " + Restaurant.getCurrentLogicalTime());

            for (int i = 0; i< quantity; i++){
                Thread.sleep(Restaurant.convertPhysicalToLogicalTime(type.getPrepTime()));
            }
        }
        Restaurant.reclaimMachine(type);
    }


    public void makeOrder(Order o){
//        System.out.println("Making order " + Thread.currentThread().getName());
        //"make" burgers
        try {
            cookItem(MachineType.BURGER, o.numBurgers);

            if (o.numFries > 0) {
                cookItem(MachineType.FRIES, o.numFries);
            }
            if (o.orderedCoke) {
                cookItem(MachineType.SODA, 1);
            }
            if (o.orderedSundae) {
                cookItem(MachineType.ICECREAM, 1);
            }

            o.isPrepared.set(true);
            o.preparedBy = getId();
//            o.notifyAll();
        }catch(InterruptedException e){
            System.out.println("Cook interrupted ! " + e);
        }


    }
}
