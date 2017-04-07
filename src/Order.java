import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mac on 3/29/17.
 */
public class Order {


    int numBurgers;
    int numFries;
    boolean orderedCoke;
    boolean orderedSundae;
    AtomicBoolean isPrepared;
    String preparedBy;

    public Order( int burgers, int fries, boolean coke, boolean sundae){

        this.numBurgers = burgers;
        this.numFries = fries;
        this.orderedCoke = coke;
        this.orderedSundae = sundae;
        this.isPrepared = new AtomicBoolean(false);
        this.preparedBy = "";
    }
}
