package house;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import movement.MovementDirections;
import movement.NextDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

enum DishwasherStates {
    OFF,
    ON,
    FINISH
}

public class HouseEnv extends Environment {

    private int dishwasherCycles = 10;

    // common literals
    public static final Literal of = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
    public static final Literal gb = Literal.parseLiteral("get(beer)");
    public static final Literal hb = Literal.parseLiteral("hand_in(beer)");
    public static final Literal sb = Literal.parseLiteral("sip(beer)");
    public static final Literal hob = Literal.parseLiteral("has(owner,beer)");
    public static final Literal litGetDelivery = Literal.parseLiteral("get(delivery)");
    public static final Literal litSaveBeer = Literal.parseLiteral("save(beer)");

    public static final Literal db = Literal.parseLiteral("drop(beer)");
    public static final Literal takeTrash = Literal.parseLiteral("take(trash)");
    public static final Literal dropTrash = Literal.parseLiteral("drop(trash)");

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    private HouseModel model; // the model of the grid
    private Map<String, Double> precioProveedor;
    private double[] priceMultipliers = { 0.992, 0.997, 1.002, 1.004 };

    @Override
    public void init(String[] args) {
        model = new HouseModel();
        NextDirection.initialize(model);
        precioProveedor = new HashMap<>();
        precioProveedor.put("mahou", 1.0);
        precioProveedor.put("estrella", 1.5);
        precioProveedor.put("skoll", 0.5);
        precioProveedor.put("tortilla", 2.5);
        precioProveedor.put("durum", 5.0);
        precioProveedor.put("empanada", 7.0);

        if (args.length == 1 && args[0].equals("gui")) {
            HouseView view = new HouseView(model);
            model.setView(view);
        }

        updatePercepts();
    }

    /** creates the agents percepts based on the HouseModel */
    void updatePercepts() {
        // clear the percepts of the agents
        clearPercepts("robot");
        clearPercepts("owner");
        clearPercepts("owner_musk");
        clearPercepts("supermarket_mercadona");
        clearPercepts("supermarket_lidl");

        /*
         * Código para meterle percepts de a dónde pueden ir
         */
        for (SpecializedRobots robot : SpecializedRobots.values()) {
            Location loc = model.getAgPos(robot.getValue());
            // Añadir at(Tipo, X, Y)
            addPercept("robot", Literal.parseLiteral(
                    String.format("at(%s, %d, %d)", robot.name().toLowerCase(), loc.x, loc.y)));
            // Añadir can_go(Tipo, Direccion)
            if (model.isFree(loc.x + 1, loc.y))
                addPercept("robot",
                        Literal.parseLiteral(String.format("can_go(%s, right)", robot.name().toLowerCase())));

            if (model.isFree(loc.x - 1, loc.y))
                addPercept("robot",
                        Literal.parseLiteral(String.format("can_go(%s, left)", robot.name().toLowerCase())));

            if (model.isFree(loc.x, loc.y + 1))
                addPercept("robot",
                        Literal.parseLiteral(String.format("can_go(%s, down)", robot.name().toLowerCase())));

            if (model.isFree(loc.x, loc.y - 1))
                addPercept("robot", Literal.parseLiteral(String.format("can_go(%s, up)", robot.name().toLowerCase())));

        }

        // Añadir where(Lugar, X, Y) y at(Tipo, Lugar)
        for (Places pl : Places.values()) {
            if (pl.x == -1 && pl.y == -1)
                continue;
            addPercept("robot",
                    Literal.parseLiteral(String.format("where(%s,%d,%d)", pl.name().toLowerCase(), pl.x, pl.y)));

            for (SpecializedRobots robot : SpecializedRobots.values()) {
                if (model.getAgPos(robot.getValue()).distanceManhattan(pl.location) <= pl.minDist) {
                    addPercept("robot", Literal.parseLiteral(
                            String.format("at(%s, %s)", robot.name().toLowerCase(), pl.name().toLowerCase())));
                }
            }
        }

        // Recorrer lista de basura
        if (!model.trash.isEmpty()) {
            for (Location lTrash : model.trash) {
                addPercept("robot", Literal.parseLiteral(String.format("where(trash,%d,%d)", lTrash.x, lTrash.y)));
                for (SpecializedRobots robot : SpecializedRobots.values()) {
                    if (model.getAgPos(robot.getValue()).distanceManhattan(lTrash) <= 1) {
                        addPercept("robot", Literal.parseLiteral(
                                String.format("at(%s, trash)", robot.name().toLowerCase())));
                    }
                }
            }
        }

        // Detecta si el cubo de basura está llena
        if (model.binCount >= 5)
            addPercept("robot", Literal.parseLiteral("bin(full)"));

        // El robot cuando está en la nevera puede ver cuantas cervezas quedan
        Location lRobot = model.getAgPos(SpecializedRobots.ROBOT.getValue());
        if (Places.FRIDGE.location.distanceManhattan(lRobot) <= 1) { // Si está en la nevera puede ver cuantas quedan
            addPercept("robot",
                    Literal.parseLiteral(String.format("available(fridge, beer, %d)", model.availableBeers)));
            if (model.availableBeers > 0)
                addPercept("robot", Literal.parseLiteral("available(fridge, beer)"));
        }

        // has(owner,beer)
        if (model.sipCount > 0) {
            addPercept("robot", hob);
            addPercept("owner", hob);
        }
        /*
         * if (model.sipCountMusk > 0) {
         * addPercept("robot", Literal.parseLiteral("has(owner_musk,beer)"));
         * addPercept("owner_musk", Literal.parseLiteral("has(owner_musk,beer)"));
         * }
         */

        // Dishwasher
        addPercept("robot", Literal.parseLiteral(String.format("plate(dishwasher, %d)", model.dishwasherCount)));
        addPercept("robot",
                Literal.parseLiteral(String.format("dishwasher(%s)", model.dishwasherState.name().toLowerCase())));

        addPercept("robot", Literal.parseLiteral(String.format("cupboard(dish,%d)", model.cupboardCount)));

        if (model.dishwasherState.equals(DishwasherStates.ON)) {
            dishwasherCycles--;
        }
        if (dishwasherCycles <= 0) {
            dishwasherCycles = 10;
            model.dishwasherState = DishwasherStates.FINISH;
        }

        if (model.dishwasherState.equals(DishwasherStates.FINISH) && model.dishwasherCount == 0) {
            model.dishwasherState = DishwasherStates.OFF;
        }

        precioProveedor.forEach((key, val) -> {
            double r = priceMultipliers[(int) Math.floor(Math.random() * priceMultipliers.length)];
            precioProveedor.put(key, val * r);
            addPercept("supermarket_mercadona", Literal.parseLiteral(String.format("proveedor(%s, %f)", key, val * r)));
            addPercept("supermarket_lidl", Literal.parseLiteral(String.format("proveedor(%s, %f)", key, val * r)));
        });

    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        boolean result = false;
        if (action.equals(of)) { // of = open(fridge)
            result = model.openFridge();

        } else if (action.equals(clf)) { // clf = close(fridge)
            result = model.closeFridge();

        } else if (action.getFunctor().equals("next_direction")) {
        } else if (action.getFunctor().equals("move_robot")) {
            String robot = action.getTerm(0).toString();

            SpecializedRobots tipo;
            tipo = SpecializedRobots.from(robot);
            MovementDirections dir = MovementDirections.from(action.getTerm(1).toString());
            result = model.moveRobot(tipo, dir);

        } else if (action.equals(gb)) {
            result = model.getBeer();

        } else if (action.getFunctor().equals("hand_in")) {
            if (action.getTerm(0).toString().equals("owner")) {
                result = model.handInBeer();
            } else {
                result = model.handInBeerMusk();
            }

        } else if (action.equals(sb)) {
            result = model.sipBeer();
        } else if (action.equals(Literal.parseLiteral("sip_musk(beer)"))) {
            result = model.sipBeerMusk();
        } else if (action.equals(dropTrash)) {
            result = model.dropTrash();
        } else if (action.equals(db)) {
            result = model.dropBeer();
        } else if (action.equals(takeTrash)) {
            result = model.takeTrash();
        } else if (action.equals(litGetDelivery) && ag.equals("robot")) {
            result = model.getDelivered();
        } else if (action.equals(litSaveBeer) && ag.equals("robot")) {
            result = model.saveBeer();
        } else if (action.getFunctor().equals("deliver")) {
            // wait 4 seconds to finish "deliver"
            try {
                Thread.sleep(4000);
                result = model.addBeer((int) ((NumberTerm) action.getTerm(1)).solve());
            } catch (Exception e) {
                logger.info("Failed to execute action deliver!" + e);
            }

        } else if (action.equals(Literal.parseLiteral("empty(bin)"))) {
            result = model.emptyBin();
        } else if (action.equals(Literal.parseLiteral("drop(bin)"))) {
            result = model.dropBin();
        } else if (action.equals(Literal.parseLiteral("put(dish,dishwasher)"))) {
            result = model.putDishInDishwasher();
            dishwasherCycles += 5;
        } else if (action.equals(Literal.parseLiteral("put(dish,cupboard)"))) {
            result = model.putDishInCupboard();
        } else if (action.equals(Literal.parseLiteral("get(dish,dishwasher)"))) {
            result = model.getDishInDishwasher();
        } else if (action.equals(Literal.parseLiteral("dishwasher(on)"))) {
            result = model.dishwasherOn();
        } else if (action.getFunctor().equals("take") && action.getTerm(0).toString().equals("plate")) {
            result = model.takePlateOwner();
        } else {
            logger.info("Failed to execute action " + action);
        }

        if (result) {
            updatePercepts();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        return result;
    }
}
