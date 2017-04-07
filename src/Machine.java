/**
 * Created by mac on 3/29/17.
 */
public class Machine {
    int prepTime;
    MachineType type;
    boolean isFree;

    public Machine( MachineType type){
        this.type = type;
        prepTime = this.type.getPrepTime();
        this.isFree = true;
    }
}

enum  MachineType{
    BURGER(5), FRIES(3), SODA(2), ICECREAM(1);

    private int prepTime;
    MachineType(int value){
        this.prepTime = value;
    }
    public int getPrepTime(){
        return this.prepTime;
    }
}
