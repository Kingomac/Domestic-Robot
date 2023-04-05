import java.util.LinkedList;
import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import jason.stdlib.map.get;
import jason.stdlib.map.remove;

enum SpecializedRobots {
    ROBOT(0),
    CLEANER(1),
    STOREKEEPER(2);

    private final int value;

    private SpecializedRobots(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }
}

enum Places {
    FRIDGE(new Location(0, 0), HouseModel.FRIDGE),
    OWNER(new Location(HouseModel.GSize - 1, HouseModel.GSize - 1), HouseModel.OWNER),
    OWNER_MUSK(new Location(HouseModel.GSize / 2, 0), HouseModel.OWNER_MUSK),
    BIN(new Location(HouseModel.GSize - 1, 0), HouseModel.BIN),
    // TRASH(new Location(-1, -1), HouseModel.TRASH),
    DELIVERY(new Location(0, HouseModel.GSize - 1), HouseModel.DELIVERY),
    BASE_ROBOT(new Location(HouseModel.GSize / 2, HouseModel.GSize / 2), -1, 0),
    BASE_CLEANER(new Location(HouseModel.GSize / 2 - 1, HouseModel.GSize - 1), -1, 0),
    BASE_STOREKEEPER(new Location(HouseModel.GSize / 2 + 1, HouseModel.GSize - 1), -1, 0);

    public Location location;
    public int x;
    public int y;
    public final int gridConst;
    public final int minDist;

    private Places(Location loc) {
        location = loc;
        gridConst = -1;
        x = loc.x;
        y = loc.y;
        minDist = 1;
    }

    private Places(Location loc, int gridConst) {
        location = loc;
        this.gridConst = gridConst;
        x = loc.x;
        y = loc.y;
        minDist = 1;
    }

    private Places(Location loc, int gridConst, int minDist) {
        location = loc;
        this.gridConst = gridConst;
        x = loc.x;
        y = loc.y;
        this.minDist = minDist;
    }

    public void setLocation(Location loc) {
        location = loc;
        x = loc.x;
        y = loc.y;
    }

    public void setLocation(int x, int y) {
        this.x = location.x = x;
        this.y = location.y = y;
    }

}

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    /*
     * private class HouseElement {
     * public final int gridConst;
     * public final Location location;
     * 
     * public HouseElement(int gridConst, Location location) {
     * this.gridConst = gridConst;
     * this.location = location;
     * }
     * }
     * 
     * private final Map<Places, HouseElement> elements;
     */

    // the grid size
    public static final int GSize = 10;
    public static final int FRIDGE = 16;
    public static final int OWNER = 32;
    public static final int BIN = 64;
    public static final int TRASH = 128;
    public static final int DELIVERY = 256;
    public static final int OWNER_MUSK = 512;

    boolean fridgeOpen = false; // whether the fridge is open
    boolean carryingBeer = false; // whether the robot is carrying beer
    boolean carryingTrash = false;
    boolean carryingDelivery = false;
    int sipCount = 0; // how many sip the owner did
    int sipCountMusk = 0;
    int availableBeers = 1; // how many beers are available
    int deliveryBeers = 0;
    int binCount = 0;
    List<Location> trash = new LinkedList<>();

    public HouseModel() {
        // create a 7x7 grid with one mobile agent
        /**
         * agentes:
         * 0 -> robot
         * 1 -> robot especializado en mover cervezas de delivery a cervezas
         * 2 -> robot especializado en recoger latas
         **/
        super(GSize, GSize, SpecializedRobots.values().length);

        // initial location of robot (column 3, line 3)
        // ag code 0 means the robot
        setAgPos(SpecializedRobots.ROBOT.getValue(), GSize / 2, GSize / 2);
        setAgPos(SpecializedRobots.CLEANER.getValue(), Places.BASE_CLEANER.location);
        setAgPos(SpecializedRobots.STOREKEEPER.getValue(), Places.BASE_STOREKEEPER.location);

        // initial location of fridge and owner
        for (Places val : Places.values()) {
            if (val.gridConst != -1 && val.x != -1 && val.y != -1)
                add(val.gridConst, val.location);
        }
        // addWall(2, 5, 3, 5);
    }

    boolean openFridge() {
        /*
         * if (!fridgeOpen) {
         * fridgeOpen = true;
         * return true;
         * } else {
         * return false;
         * }
         */
        fridgeOpen = true;
        return true;
    }

    boolean closeFridge() {
        /*
         * if (fridgeOpen) {
         * fridgeOpen = false;
         * return true;
         * } else {
         * return false;
         * }
         */

        fridgeOpen = false;
        return true;
    }

    boolean getBeer() {
        // if (fridgeOpen && availableBeers > 0 && !carryingBeer) {
        if (availableBeers > 0) {
            availableBeers--;
            carryingBeer = true;
            if (view != null)
                view.update(Places.FRIDGE.x, Places.FRIDGE.y);
        }
        return true;
        /*
         * } else {
         * return false;
         * }
         */
    }

    boolean addBeer(int n) {
        deliveryBeers += n;
        if (view != null)
            view.update(Places.FRIDGE.x, Places.FRIDGE.y);
        return true;
    }

    boolean handInBeer() {
        // if (carryingBeer) {
        sipCount = 10;
        carryingBeer = false;
        if (view != null)
            view.update(Places.OWNER.x, Places.OWNER.y);
        return true;
        /*
         * } else {
         * return false;
         * }
         */
    }

    boolean handInBeerMusk() {
        // if (carryingBeer) {
        sipCountMusk = 10;
        carryingBeer = false;
        if (view != null)
            view.update(Places.OWNER_MUSK.x, Places.OWNER_MUSK.y);
        return true;
        /*
         * } else {
         * return false;
         * }
         */
    }

    boolean getDelivered() {
        if (deliveryBeers >= 3 && !carryingDelivery) {
            carryingDelivery = true;
            deliveryBeers -= 3;
            if (view != null)
                view.update(Places.DELIVERY.x, Places.DELIVERY.y);
            return true;
        }
        return false;
    }

    boolean saveBeer() {
        // if (carryingDelivery) {
        availableBeers += 3; // Deja 2 y se queda 1 (en total 3)
        carryingDelivery = false;
        if (view != null)
            view.update(Places.DELIVERY.x, Places.DELIVERY.y);
        return true;
        // }
        // return false;
    }

    boolean sipBeer() {
        if (sipCount > 0) {
            sipCount--;
            if (view != null)
                view.update(Places.OWNER.x, Places.OWNER.y);
            return true;
        } else {
            return false;
        }
    }

    boolean sipBeerMusk() {
        if (sipCountMusk > 0) {
            sipCountMusk--;
            if (view != null)
                view.update(Places.OWNER_MUSK.x, Places.OWNER_MUSK.y);
            return true;
        } else {
            return false;
        }
    }

    boolean isPlace(int x, int y) {
        Location loc = new Location(x, y);
        for (Places p : Places.values()) {
            if (loc.equals(p.location))
                return true;
        }
        return false;
    }

    boolean dropBeer() {
        int posX = 3;
        int posY = 2;

        do {
            posX = (int) Math.round(Math.random() * (GSize - 1));
            posY = (int) Math.round(Math.random() * (GSize - 1));
        } while (!isFree(posX, posY) && !isPlace(posX, posY));

        trash.add(new Location(posX, posY));

        // Places.TRASH.setLocation(posX, posY);
        add(TRASH, posX, posY);

        System.out.println("dropped beer: " + posX + ", " + posY);

        return true;
    }

    boolean takeTrash() {
        Location lCleaner = getAgPos(SpecializedRobots.CLEANER.getValue());
        Location near = trash.stream().filter(x -> lCleaner.distanceManhattan(x) <= 2).findFirst().orElse(null);
        if (near == null) {
            System.out.println("NEAR IS NULL");
            return false;
        }
        carryingTrash = true;
        remove(TRASH, near);
        trash.remove(near);
        return true;
        /*
         * if (lCleaner.distanceManhattan(Places.TRASH.location) > 1)
         * return false;
         * carryingTrash = true;
         * remove(Places.TRASH.gridConst, Places.TRASH.location);
         * Places.TRASH.setLocation(-1, -1);
         * return true;
         */
    }

    boolean dropTrash() {
        Location lCleaner = getAgPos(SpecializedRobots.CLEANER.getValue());
        if (lCleaner.distanceManhattan(Places.BIN.location) > 1)
            return false;
        carryingTrash = false;
        binCount++;
        return true;
    }

    boolean moveRobot(SpecializedRobots tipo, Location dest) {
        Location origen = getAgPos(tipo.getValue());

        /*
         * if (!isFree(dest))
         * return false;
         */

        if (origen.x < dest.x)
            origen.x++;
        else if (origen.x > dest.x)
            origen.x--;
        if (origen.y < dest.y)
            origen.y++;
        else if (origen.y > dest.y)
            origen.y--;

        setAgPos(tipo.getValue(), origen);

        return true;
    }

    boolean emptyBin() {
        binCount = 0;
        carryingTrash = true;
        return true;
    }

    boolean dropBin() {
        carryingTrash = false;
        return true;
    }

}
