package oregontrail;

public abstract class Part {
    /**
     * Default weight of a part
     */
    public static final int DEFAULT_WEIGHT = 5;

    /**
     * Default health of a part
     */
    public static final int DEFAULT_HEALTH = 100;

    /**
     * Health of a part
     */
    protected int health;

    /**
     * Initialize Part with default health
     */
    public Part(){
        this.health = DEFAULT_HEALTH;
    }

    /**
     * Initialize Part
     * @param health Starting health
     */
    public Part(int health) {
        this.health = health;
    }

//    /**
//     * Apply wear of a day
//     * @param game Game to get pace from
//     */
//    @Override
//    public void nextDay(Game game) {
//        health = (health - game.getPace().getRate());
//    }

//    /**
//     * Get how much a type of part weighs
//     * @param p Type of <code>Part</code>
//
//     * @return Weight */
//    public static int getWeight(Part p){
//        if(p instanceof Wheel){
//            return Wheel.getWeight();
//        }else if(p instanceof Tongue){
//            return Tongue.getWeight();
//        }else if(p instanceof Axle){
//            return Axle.getWeight();
//        }
//        return 0;
//    }

    /**
     * Method isReady.
     * @return boolean
     */
    public boolean isReady() {
        return health > 0;
    }

    /**
     * Get health of this part

     * @return health of part */
    public int getHealth(){
        return health;
    }
}
