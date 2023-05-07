import java.util.LinkedList;
import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/*
 * Identificación de los distintos robots
 */
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

/*
 * Identificación de los lugares fijos del grid
 */
enum Places {
    FRIDGE(new Location(0, 0), HouseModel.FRIDGE),
    OWNER(new Location(HouseModel.GSize - 1, HouseModel.GSize - 1), HouseModel.OWNER),
    // OWNER_MUSK(new Location(HouseModel.GSize / 2, 0), HouseModel.OWNER_MUSK),
    BIN(new Location(HouseModel.GSize - 1, 0), HouseModel.BIN),
    DELIVERY(new Location(0, HouseModel.GSize - 1), HouseModel.DELIVERY),
    DISHWASHER(new Location(2, 0), HouseModel.DISHWASHER),
    CUPBOARD(new Location(4, 0), HouseModel.CUPBOARD),
    BASE_ROBOT(new Location(HouseModel.GSize / 2, HouseModel.GSize / 2), -1, 0),
    BASE_CLEANER(new Location(HouseModel.GSize / 2 - 1, HouseModel.GSize - 1), -1, 0),
    BASE_STOREKEEPER(new Location(HouseModel.GSize / 2 + 1, HouseModel.GSize - 1), -1, 0);

    public Location location;
    public Location robotLoc;
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

    /**
     * Cambia la localización actualizando los parámetros x, y y location
     * 
     * @param loc nueva localización
     */
    public void setLocation(Location loc) {
        location = loc;
        x = loc.x;
        y = loc.y;
    }

    /**
     * Cambia la localización actualizando los parámetros x, y y location
     * 
     * @param x
     * @param y
     */
    public void setLocation(int x, int y) {
        this.x = location.x = x;
        this.y = location.y = y;
    }

}

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    public static final int GSize = 12; // Grid size
    public static final int FRIDGE = 16; // Capa Fridge
    public static final int OWNER = 32; // Capa Owner
    public static final int BIN = 64; // Capa Bin
    public static final int TRASH = 128; // Capa Trash
    public static final int DELIVERY = 256; // Capa Delivery
    public static final int OWNER_MUSK = 512; // Owner Capa Musk
    public static final int DISHWASHER = 1024; // Capa dishwasher
    public static final int CUPBOARD = 2048; // Capa cupboard

    boolean fridgeOpen = false; // si la nevera está abierta
    boolean carryingBeer = false; // si el mayordomo está llevando cerveza
    boolean carryingTrash = false; // si el cleaner está llevando basura
    boolean carryingDelivery = false; // si el storekeeper está llevando una entrega
    DishwasherStates dishwasherState = DishwasherStates.OFF;
    int carryingDish = 0; // si el robot está llevando un plato limpio o sucio
    int sipCount = 0; // how many sip the owner did
    int sipCountMusk = 0;
    int availableBeers = 1; // cervezas en la nevera
    int availablePinchos = 1; // pinchos en la nevera
    int deliveryBeers = 0; // cervezas en la zona delivery
    int binCount = 0; // núm. cervezas en la papelera
    int dishwasherCount = 0;
    int cupboardCount = 0;
    List<Location> trash = new LinkedList<>(); // localización de la basura del mapa
    List<Location> walls = new LinkedList<>();

    public HouseModel() {
        /**
         * agentes:
         * 0 -> robot
         * 1 -> robot especializado en mover cervezas de delivery a cervezas
         * 2 -> robot especializado en recoger latas
         **/
        super(GSize, GSize, SpecializedRobots.values().length); // (tamaño, tamaño, número de agentes móviles)

        // inicializar posiciones de los robots
        setAgPos(SpecializedRobots.ROBOT.getValue(), GSize / 2, GSize / 2);
        setAgPos(SpecializedRobots.CLEANER.getValue(), Places.BASE_CLEANER.location);
        setAgPos(SpecializedRobots.STOREKEEPER.getValue(), Places.BASE_STOREKEEPER.location);

        // inicializar elementos no móviles
        for (Places val : Places.values()) {
            if (val.gridConst != -1 && val.x != -1 && val.y != -1)
                add(val.gridConst, val.location);
        }

        // inicializar muros
        for (int i = 0; i < 15; i++) {
            int posX = 3;
            int posY = 2;

            do {
                posX = (int) Math.round(Math.random() * (GSize - 1));
                posY = (int) Math.round(Math.random() * (GSize - 1));
            } while (isPlace(posX, posY) || !isFree(posX, posY));
            walls.add(new Location(posX, posY));
            addWall(posX, posY, posX, posY);
        }
        // addWall(2, 5, 3, 5);
    }

    public boolean isWall(Location l) {
        return walls.contains(l);
    }

    public boolean isWall(int x, int y) {
        return walls.contains(new Location(x, y));
    }

    public boolean isThereOtherRobot(SpecializedRobots me, Location loc) {

        for (SpecializedRobots rob : SpecializedRobots.values()) {
            Location pos = getAgPos(rob.getValue());
            if (rob.equals(me))
                continue;
            if (pos.equals(loc))
                return true;
        }
        return false;
        // int ag = getAgAtPos(loc);
        // return ag != -1 && ag != me.getValue();
    }

    public boolean isThereOtherRobot(SpecializedRobots me, int x, int y) {
        return isThereOtherRobot(me, new Location(x, y));
    }

    /**
     * Abrir nevera
     * 
     * @return
     */
    boolean openFridge() {
        fridgeOpen = true;
        return true;
    }

    /**
     * Cerrar nevera
     * 
     * @return
     */
    boolean closeFridge() {
        fridgeOpen = false;
        return true;
    }

    /**
     * El robot coge una cerveza de la nevera
     * 
     * @return
     */
    boolean getBeer() {
        if (availableBeers > 0) {
            availableBeers--;
            availablePinchos--;
            carryingBeer = true;
            if (view != null)
                view.update(Places.FRIDGE.x, Places.FRIDGE.y);
        }
        return true;
    }

    /**
     * El supermercado deposita las cervezas en la zona de delivery
     * 
     * @param n número de cervezas a entregar
     * @return
     */
    boolean addBeer(int n) {
        deliveryBeers += n;
        if (view != null)
            view.update(Places.DELIVERY.x, Places.DELIVERY.y);
        return true;
    }

    /**
     * Dar una cerveza al owner
     * 
     * @return
     */
    boolean handInBeer() {
        sipCount = 10;
        carryingBeer = false;
        if (view != null)
            view.update(Places.OWNER.x, Places.OWNER.y);
        return true;
    }

    /**
     * Dar una cerveza al owner_musk
     * 
     * @return
     */
    boolean handInBeerMusk() {
        sipCountMusk = 10;
        carryingBeer = false;
        // if (view != null)
        // view.update(Places.OWNER_MUSK.x, Places.OWNER_MUSK.y);
        return true;
    }

    /**
     * El storekeeper coge las cervezas de la zona delivery
     * 
     * @return
     */
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

    /**
     * El storekeeper guarda las cervezas en la nevera
     * 
     * @return
     */
    boolean saveBeer() {
        availableBeers += 3; // Deja 2 y se queda 1 (en total 3)
        availablePinchos += 3;
        carryingDelivery = false;
        if (view != null)
            view.update(Places.DELIVERY.x, Places.DELIVERY.y);
        return true;
    }

    /**
     * El owner sorbe la cerveza
     * 
     * @return
     */
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

    /**
     * El owner_musk sorbe la cerveza
     * 
     * @return
     */
    boolean sipBeerMusk() {
        if (sipCountMusk > 0) {
            sipCountMusk--;
            // if (view != null)
            // view.update(Places.OWNER_MUSK.x, Places.OWNER_MUSK.y);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Comprueba si una posición está ocupada por un elemento estático
     * 
     * @param x
     * @param y
     * @return true si es un lugar, false si no lo es
     */
    public boolean isPlace(int x, int y) {
        Location loc = new Location(x, y);
        for (Places p : Places.values()) {
            if (loc.equals(p.location))
                return true;
        }
        return false;
    }

    /**
     * El owner tira una cerveza en una posición aleatoria del mapa
     * 
     * @return
     */
    boolean dropBeer() {
        int posX = 3;
        int posY = 2;

        do {
            posX = (int) Math.round(Math.random() * (GSize - 1));
            posY = (int) Math.round(Math.random() * (GSize - 1));
        } while (!isFree(posX, posY) && !isPlace(posX, posY));
        // Genera valores aleatorios mientras no encuentra uno libre

        trash.add(new Location(posX, posY)); // Se añade la basura a las posiciones de basura
        add(TRASH, posX, posY); // Se dibuja en el grid

        System.out.println("dropped beer: " + posX + ", " + posY);

        return true;
    }

    /**
     * El cleaner recoge una cerveza tirada
     * 
     * @return
     */
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
    }

    /**
     * El cleaner deposita la basura en la papelera
     * 
     * @return
     */
    boolean dropTrash() {
        Location lCleaner = getAgPos(SpecializedRobots.CLEANER.getValue());
        if (lCleaner.distanceManhattan(Places.BIN.location) > 1)
            return false;
        carryingTrash = false;
        binCount++;
        return true;
    }

    /**
     * Movimiento de un robot
     * 
     * @param tipo tipo de robot que se va a mover
     * @param dest localización del destino
     * @return
     */
    boolean moveRobot(SpecializedRobots tipo, String dest) {
        Location origen = getAgPos(tipo.getValue());

        if (dest.equals("up")) {
            origen.y--;
        } else if (dest.equals("down")) {
            origen.y++;
        } else if (dest.equals("left")) {
            origen.x--;
        } else {
            origen.x++;
        }

        setAgPos(tipo.getValue(), origen);
        return true;
    }

    /**
     * El cleaner vacía la papelera
     * 
     * @return
     */
    boolean emptyBin() {
        binCount = 0;
        carryingTrash = true;
        return true;
    }

    /**
     * El cleaner deposita la bolsa de basura en la zona delivery
     * 
     * @return
     */
    boolean dropBin() {
        carryingTrash = false;
        return true;
    }

    boolean putDishInDishwasher() {
        dishwasherCount += carryingDish;
        carryingDish = 0;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

    boolean putDishInCupboard() {
        cupboardCount += carryingDish;
        carryingDish = 0;
        if (view != null)
            view.update(Places.CUPBOARD.x, Places.CUPBOARD.y);
        return true;
    }

    boolean getDishInDishwasher() {
        dishwasherCount--;
        carryingDish++;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

    boolean takePlateOwner() {
        carryingDish++;
        return true;
    }

    public boolean dishwasherOn() {
        dishwasherState = DishwasherStates.ON;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

}
