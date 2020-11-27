package oregontrail.store;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import oregontrail.Axle;
import oregontrail.Tongue;
import oregontrail.Wagon;
import oregontrail.Wheel;
import utils.CorneliusUtils;

public class BridgerGeneralStore extends GeneralStore {
    private MessageReceivedEvent event;

    public BridgerGeneralStore(MessageReceivedEvent event) {
        this.event = event;
        setOxenPrice(35.00);
        setClothingPrice(17.50);
        setAmmoPrice(3.50);
        setAxlePrice(17.50);
        setWheelPrice(17.50);
        setTonguePrice(17.50);
        setFoodPrice(0.35);

        setNumOxenAvailable(10);
        setNumSetsClothingAvailable(25);
        setNumAmmoAvailable(45);
        setNumWagonAxlesAvailable(2);
        setNumWagonTonguesAvailable(2);
        setNumWagonWheelsAvailable(2);
        setNumFoodAvailable(1500);

        setImageURL("https://lh3.googleusercontent.com/pw/ACtC-3fpp-nKovWkd3k11-5fYke0KpTLT53HZE6FuWu7TrBsdSJXC3bnM3-34_WC3klDM6VhSCg8ZWS9Unwn2Vc9JS17Gk1oJNvXPbm3vpjUZgWOjM5kprEom9Z0FwkjTgxYU4xPDpg1m-vuZz4748wBOEga=w525-h347-no?authuser=1");
    }

    @Override
    public boolean canBuy(String input, double cash) {
        if (input != null && !input.isEmpty()) {
            String[] itemAndQuantity = input.split(" ");
            if (itemAndQuantity.length != 2) return false;
            String item = itemAndQuantity[0].trim();
            String quantity = itemAndQuantity[1].trim();
            if (CorneliusUtils.isNumeric(item) && CorneliusUtils.isNumeric(quantity)) {
                int q = Integer.parseInt(quantity);
                switch (item) {
                    case "1":
                        if (getNumOxenAvailable() < q) {
                            event.getChannel().sendMessage("There are only " + getNumOxenAvailable() + " oxen left in stock").queue();
                            return false;
                        }
                        if ((q * getOxenPrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " oxen").queue();
                            return false;
                        }
                        return true;
                    case "2":
                        if (getNumSetsClothingAvailable() < q) {
                            event.getChannel().sendMessage("There is only " + getNumSetsClothingAvailable() + " sets of clothing left in stock").queue();
                            return false;
                        }
                        if ((q * getClothingPrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " sets of clothing").queue();
                            return false;
                        }
                        return true;
                    case "3":
                        if (getNumAmmoAvailable() < q) {
                            event.getChannel().sendMessage("There is only " + getNumAmmoAvailable() + " boxes of ammo left in stock").queue();
                            return false;
                        }
                        if ((q * getAmmoPrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " boxes of ammo").queue();
                            return false;
                        }
                        return true;
                    case "4":
                        if (getNumWagonWheelsAvailable() < q) {
                            event.getChannel().sendMessage("There are only " + getNumWagonWheelsAvailable() + " spare wheels left in stock").queue();
                            return false;
                        }
                        if ((q * getWheelPrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " wheels").queue();
                            return false;
                        }
                        return true;
                    case "5":
                        if (getNumWagonAxlesAvailable() < q) {
                            event.getChannel().sendMessage("There are only " + getNumWagonAxlesAvailable() + " spare axles left in stock").queue();
                            return false;
                        }
                        if ((q * getAxlePrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " axles").queue();
                            return false;
                        }
                        return true;
                    case "6":
                        if (getNumWagonTonguesAvailable() < q) {
                            event.getChannel().sendMessage("There are only " + getNumWagonTonguesAvailable() + " spare tongues left in stock").queue();
                            return false;
                        }
                        if ((q * getTonguePrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " tongues").queue();
                            return false;
                        }
                        return true;
                    case "7":
                        if (getNumFoodAvailable() < q) {
                            event.getChannel().sendMessage("There is only " + getNumFoodAvailable() + " pounds of food left in stock").queue();
                            return false;
                        }
                        if ((q * getFoodPrice()) > cash) {
                            event.getChannel().sendMessage("You don't have enough cash for " + q + " pounds of food").queue();
                            return false;
                        }
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the total cost
     * @param input
     * @return
     */
    public double buy(String input, Wagon wagon) {
        String[] itemAndQuantity = input.split(" ");
        String item = itemAndQuantity[0].trim();
        int quantity = Integer.parseInt(itemAndQuantity[1]);
        double price = Double.MIN_VALUE; //Ensure there is no item with this value
        switch (item) {
            case "1":
                setNumOxenAvailable(getNumOxenAvailable() - quantity);
                price = getOxenPrice();
                wagon.setOxen(wagon.getOxen() + quantity);
                break;
            case "2":
                setNumSetsClothingAvailable(getNumSetsClothingAvailable() - quantity);
                price = getClothingPrice();
                wagon.setClothes(wagon.getClothes() + quantity);
                break;
            case "3":
                setNumAmmoAvailable(getNumAmmoAvailable() - quantity);
                price = getAmmoPrice();
                wagon.setAmmo(wagon.getAmmo() + (quantity * 20));
                break;
            case "4":
                setNumWagonWheelsAvailable(getNumWagonWheelsAvailable() - quantity);
                price = getWheelPrice();
                for (int i = 0; i < quantity; i++) wagon.getSpareParts().add(new Wheel());
                break;
            case "5":
                setNumWagonAxlesAvailable(getNumWagonAxlesAvailable() - quantity);
                price = getAxlePrice();
                for (int i = 0; i < quantity; i++) wagon.getSpareParts().add(new Axle());
                break;
            case "6":
                setNumWagonTonguesAvailable(getNumWagonTonguesAvailable() - quantity);
                price = getTonguePrice();
                for (int i = 0; i < quantity; i++) wagon.getSpareParts().add(new Tongue());
                break;
            case "7":
                setNumFoodAvailable(getNumFoodAvailable() - quantity);
                price = getFoodPrice();
                wagon.setFood(wagon.getFood() + quantity);
                break;
        }

        if (price != Double.MIN_VALUE) {
            wagon.setCash(wagon.getCash() - (quantity * price));
            return quantity * price;
        }

        return 0;
    }
}
