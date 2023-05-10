package house;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import movement.MovementDirections;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    public static final int GSize = 11; // Grid size
    public static final int FRIDGE = 16; // Capa Fridge
    public static final int OWNER = 32; // Capa Owner
    public static final int BIN = 64; // Capa Bin
    public static final int TRASH = 128; // Capa Trash
    public static final int DELIVERY = 256; // Capa Delivery
    public static final int DISHWASHER = 512; // Capa dishwasher
    public static final int CUPBOARD = 1024; // Capa cupboard

    boolean fridgeOpen = false; // si la nevera está abierta
    boolean carryingBeer = false; // si el mayordomo está llevando cerveza
    boolean carryingTrash = false; // si el cleaner está llevando basura
    boolean carryingDelivery = false; // si el storekeeper está llevando una entrega
    boolean burningTrash = false;
    boolean namOrSip = false;
    boolean canDropBeer = true;
    DishwasherStates dishwasherState = DishwasherStates.OFF;
    int carryingDishDirty = 0; // si el robot está llevando un plato limpio o sucio
    int carryingDishClean = 0;
    int carryingDishPincho = 0;
    int sipCount = 0; // how many sip the owner did
    int namCount = 0;
    int sipCountMusk = 0;
    int availableBeers = 3; // cervezas en la nevera
    int availablePinchos = 0; // pinchos en la nevera
    int availableTapas = 1;
    int deliveryBeers = 0; // cervezas en la zona delivery
    int binCount = 0; // núm. cervezas en la papelera
    int dishwasherCount = 0;
    int cupboardCount = 8;
    List<Location> trash = new LinkedList<>(); // localización de la basura del mapa

    public HouseModel() {
        /**
         * agentes:
         * 0 -> robot
         * 1 -> robot especializado en mover cervezas de delivery a cervezas
         * 2 -> robot especializado en recoger latas
         **/
        super(GSize, GSize, MobileAgents.values().length + 1); // (tamaño, tamaño, n_robots + 1_owner)

        // inicializar posiciones de los robots
        for (MobileAgents rob : MobileAgents.values())
            setAgPos(rob.getValue(), rob.base.x, rob.base.y);

        // inicializar elementos no móviles
        for (Places val : Places.values()) {
            if (val.gridConst != -1 && val.x != -1 && val.y != -1)
                add(val.gridConst, val.location);
        }

        // inicializar muros
        Random rm = new Random();
        int numMuros = 7 + rm.nextInt(3);

        for (int i = 0; i < numMuros; i++) {
            int posX = 3;
            int posY = 2;

            do {
                posX = 2 + (int) Math.round(Math.random() * (GSize - 5));
                posY = 2 + (int) Math.round(Math.random() * (GSize - 5));
            } while (isPlace(posX, posY) || !isFree(posX, posY));
            addWall(posX, posY, posX, posY);
        }
    }

    public boolean isThereOtherRobot(MobileAgents me, Location loc) {

        for (MobileAgents rob : MobileAgents.values()) {
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

    public boolean isThereOtherRobot(MobileAgents me, int x, int y) {
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
            carryingDishClean--;
            carryingDishPincho++;
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

    boolean handInPincho() {
        carryingDishPincho--;
        namCount = 10;
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
        availableTapas += 1;
        carryingDelivery = false;
        if (view != null)
            view.update(Places.FRIDGE.x, Places.FRIDGE.y);
        return true;
    }

    /**
     * El owner sorbe la cerveza
     * 
     * @return
     */
    boolean sipBeer() {
        namOrSip = !namOrSip;
        if (sipCount > 0) {
            sipCount--;
            if (sipCount == 0)
                canDropBeer = true;
            if (view != null)
                view.update(Places.OWNER.x, Places.OWNER.y);
            return true;
        } else {
            return false;
        }
    }

    boolean namPincho() {
        namOrSip = !namOrSip;
        if (namCount > 0) {
            namCount--;
            if (view != null) {
                view.update(Places.OWNER.x, Places.OWNER.y);
            }
        }
        return true;

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
        if (!canDropBeer)
            return true;
        canDropBeer = false;
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

    boolean recycleBeer() {
        binCount++;
        if (view != null)
            view.update(Places.BIN.x, Places.BIN.y);
        return true;
    }

    /**
     * El cleaner recoge una cerveza tirada
     * 
     * @return
     */
    boolean takeTrash() {
        Location lCleaner = getAgPos(MobileAgents.CLEANER.getValue());
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
        Location lCleaner = getAgPos(MobileAgents.CLEANER.getValue());
        if (lCleaner.distanceManhattan(Places.BIN.location) > 1)
            return false;
        carryingTrash = false;
        binCount++;
        if (view != null)
            view.update(Places.BIN.x, Places.BIN.y);
        return true;
    }

    /**
     * Movimiento de un robot
     * 
     * @param tipo tipo de robot que se va a mover
     * @param dest localización del destino
     * @return
     */
    boolean moveAgent(MobileAgents tipo, String dest) {
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

    boolean moveAgent(MobileAgents tipo, MovementDirections dir) {
        Location origen = getAgPos(tipo.getValue());

        if (dir.equals(MovementDirections.UP))
            origen.y--;
        else if (dir.equals(MovementDirections.DOWN))
            origen.y++;
        else if (dir.equals(MovementDirections.LEFT))
            origen.x--;
        else if (dir.equals(MovementDirections.RIGHT))
            origen.x++;

        setAgPos(tipo.getValue(), origen);
        return true;
    }

    boolean moveAgent(MobileAgents tipo, Location dest) {
        setAgPos(tipo.getValue(), dest);
        return true;
    }

    /**
     * El cleaner vacía la papelera
     * 
     * @return
     */
    boolean emptyBin() {
        binCount = 0;
        burningTrash = true;
        if (view != null) {
            Location rob = getAgPos(MobileAgents.BURNER.getValue());
            view.update(rob.x, rob.y);
            view.update(Places.BIN.x, Places.BIN.y);
        }
        return true;
    }

    /**
     * El cleaner deposita la bolsa de basura en la zona delivery
     * 
     * @return
     */
    boolean dropBin() {
        carryingTrash = false;
        burningTrash = false;
        if (view != null) {
            Location rob = getAgPos(MobileAgents.BURNER.getValue());
            view.update(rob.x, rob.y);
        }
        return true;
    }

    boolean putDishInDishwasher() {
        dishwasherCount += carryingDishDirty;
        carryingDishDirty = 0;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

    boolean putDishInCupboard() {
        cupboardCount += carryingDishClean;
        carryingDishClean = 0;
        if (view != null)
            view.update(Places.CUPBOARD.x, Places.CUPBOARD.y);
        return true;
    }

    boolean getDishInDishwasher() {
        dishwasherCount--;
        carryingDishClean++;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

    boolean takePlateOwner() {
        carryingDishDirty++;
        return true;
    }

    boolean getPlateFromCupboard() {
        cupboardCount--;
        carryingDishClean++;
        if (view != null)
            view.update(Places.CUPBOARD.x, Places.CUPBOARD.y);
        return true;
    }

    public boolean dishwasherOn() {
        dishwasherState = DishwasherStates.ON;
        if (view != null)
            view.update(Places.DISHWASHER.x, Places.DISHWASHER.y);
        return true;
    }

    public boolean robotCanGo(MobileAgents me, Location pos) {
        if (me.isOwner && pos.equals(me.base.location))
            return true;
        for (Location t : trash) {
            if (t.equals(pos))
                return false;
        }
        for (Places p : Places.values()) {
            if (p.location.equals(pos) && !p.canGoThrough)
                return false;
        }
        return (isFreeOfObstacle(pos) && !isThereOtherRobot(me, pos));
    }

    public boolean robotCanGo(MobileAgents me, int posX, int posY) {
        Location loc = new Location(posX, posY);
        return robotCanGo(me, loc);
    }

    public boolean makePinchos() {
        availableTapas--;
        availablePinchos += 3;
        if (view != null)
            view.update(Places.FRIDGE.x, Places.FRIDGE.y);
        return true;
    }

}
