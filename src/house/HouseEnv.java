package house;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import movement.MovementDirections;
import movement.NextDirection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

enum DishwasherStates {
    OFF,
    ON,
    FINISH
}

public class HouseEnv extends Environment {

    private int dishwasherCycles = 10;

    // common literals

    public static final List<String> SUPERMARKETS = List.of("supermarket_mercadona", "supermarket_lidl");

    public static final Map<String, Literal> LITERALS = List.of(
            "open(fridge)", "close(fridge)", "get(beer)", "sip(beer)", "has(owner,beer)",
            "get(delivery)", "save(beer)", "drop(beer)", "take(trash)", "drop(trash)",
            "make(pinchos)", "get(dish,cupboard)", "nam(pincho)", "empty(bin)", "drop(bin)",
            "put(dish,dishwasher)", "put(dish,cupboard)", "get(dish,dishwasher)", "dishwasher(on)",
            "recycle(owner,beer)", "bin(full)", "available(fridge,beer)")
            .stream().collect(Collectors.toMap(
                    i -> i,
                    i -> Literal.parseLiteral(i)));

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    private HouseModel model; // the model of the grid
    private Map<String, Double> precioProveedor;
    private double[] priceMultipliers = { 0.992, 0.997, 1.002, 1.004, 1.008 };

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
        clearAllPercepts();

        for (MobileAgents robot : MobileAgents.values()) {
            Location loc = model.getAgPos(robot.getValue());
            // Añadir at(Tipo, X, Y)
            MobileAgents.AGENTS.forEach(i -> addPercept(i, Literal.parseLiteral(
                    String.format("at(%s, %d, %d)", robot.name().toLowerCase(), loc.x, loc.y))));
        }

        // Añadir where(Lugar, X, Y) y at(Tipo, Lugar)
        for (Places pl : Places.values()) {
            if (pl.x == -1 && pl.y == -1)
                continue;

            MobileAgents.AGENTS.forEach(i -> addPercept(i,
                    Literal.parseLiteral(String.format("where(%s,%d,%d)", pl.name().toLowerCase(), pl.x, pl.y))));
            for (MobileAgents robot : MobileAgents.values()) {
                if (!pl.equals(robot.base)
                        && model.getAgPos(robot.getValue()).distanceManhattan(pl.location) <= pl.minDist) {
                    addPercept(robot.agentName, Literal.parseLiteral(
                            String.format("at(%s, %s)", robot.name().toLowerCase(), pl.name().toLowerCase())));
                } else if (pl.equals(robot.base)
                        && model.getAgPos(robot.getValue()).distanceManhattan(pl.location) < 1) {
                    addPercept(robot.agentName, Literal.parseLiteral(
                            String.format("at(%s, %s)", robot.name().toLowerCase(), pl.name().toLowerCase())));
                }
            }
        }

        // Recorrer lista de basura
        if (!model.trash.isEmpty()) {
            for (Location lTrash : model.trash) {
                MobileAgents.AGENTS.forEach(i -> addPercept(i,
                        Literal.parseLiteral(String.format("where(trash,%d,%d)", lTrash.x, lTrash.y))));
                for (MobileAgents robot : MobileAgents.values()) {
                    if (model.getAgPos(robot.getValue()).distanceManhattan(lTrash) <= 1) {
                        MobileAgents.AGENTS.forEach(i -> addPercept("robot", Literal.parseLiteral(
                                String.format("at(%s, trash)", robot.name().toLowerCase()))));
                    }
                }
            }
        }

        // Detecta si el cubo de basura está llena
        if (model.binCount >= 5)
            addPercept("robot", LITERALS.get("bin(full)"));

        // El robot cuando está en la nevera puede ver cuantas cervezas quedan
        Arrays.stream(MobileAgents.values()).forEach(i -> {
            if (model.getAgPos(i.getValue()).distanceManhattan(Places.FRIDGE.location) <= 1) {
                addPercept(i.agentName,
                        Literal.parseLiteral(String.format("available(fridge, beer, %d)", model.availableBeers)));
                addPercept(i.agentName,
                        Literal.parseLiteral(String.format("available(fridge, tapa, %d)", model.availableTapas)));
                addPercept(i.agentName,
                        Literal.parseLiteral(String.format("available(fridge, pincho, %d)", model.availablePinchos)));
                if (model.availableBeers > 0)
                    addPercept(i.agentName, LITERALS.get("available(fridge,beer)"));
            }
        });

        // has(owner,beer)
        if (model.sipCount > 0) {
            MobileAgents.AGENTS.forEach(i -> addPercept(i, LITERALS.get("has(owner,beer)")));
        }

        // Dishwasher
        addPercept("robot", Literal.parseLiteral(String.format("plate(dishwasher, %d)", model.dishwasherCount)));
        addPercept("robot",
                Literal.parseLiteral(String.format("dishwasher(%s)", model.dishwasherState.name().toLowerCase())));

        addPercept("robot", Literal.parseLiteral(String.format("cupboard(dish,%d)", model.cupboardCount)));

        // Gestión estados dishwasher
        switch (model.dishwasherState) {
            case ON:
                dishwasherCycles--;
                if (dishwasherCycles <= 0)
                    model.dishwasherState = DishwasherStates.FINISH;
                break;
            case FINISH:
                if (model.dishwasherCount <= 0)
                    model.dishwasherState = DishwasherStates.OFF;
                break;
            case OFF:
                if (dishwasherCycles <= 0)
                    dishwasherCycles = 10;
                break;
        }

        // proveedor(Producto,Precio)
        precioProveedor.forEach((key, val) -> {
            double r = priceMultipliers[(int) Math.floor(Math.random() * priceMultipliers.length)];
            precioProveedor.put(key, val * r);
            SUPERMARKETS.forEach(i -> addPercept(i,
                    Literal.parseLiteral(String.format(Locale.ROOT, "proveedor(%s, %f)", key, val * r))));
        });
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        boolean result = false;
        if (action.equals(LITERALS.get("open(fridge)"))) {
            result = model.openFridge();

        } else if (action.equals(LITERALS.get("close(fridge)"))) { // clf = close(fridge)
            result = model.closeFridge();

        } else if (action.getFunctor().equals("next_direction")) {
        } else if (action.getFunctor().equals("move_agent")) {
            String robot = action.getTerm(0).toString();

            MobileAgents tipo;
            tipo = MobileAgents.from(robot);
            MovementDirections dir = MovementDirections.from(action.getTerm(1).toString());
            result = model.moveAgent(tipo, dir);

        } else if (action.equals(LITERALS.get("get(beer)"))) {
            result = model.getBeer();

        } else if (action.getFunctor().equals("hand_in")) {
            if (action.getTerm(0).toString().equals("owner")) {
                if (action.getTerm(1).toString().equals("beer")) {
                    result = model.handInBeer();
                } else if (action.getTerm(1).toString().equals("pincho")) {
                    result = model.handInPincho();
                }
            }
        } else if (action.equals(LITERALS.get("sip(beer)"))) {
            result = model.sipBeer();
        } else if (action.equals(LITERALS.get("drop(trash)"))) {
            result = model.dropTrash();
        } else if (action.equals(LITERALS.get("drop(beer)"))) {
            result = model.dropBeer();
        } else if (action.equals(LITERALS.get("take(trash)"))) {
            result = model.takeTrash();
        } else if (action.equals(LITERALS.get("get(delivery)")) && ag.equals("robot")) {
            result = model.getDelivered();
        } else if (action.equals(LITERALS.get("save(beer)")) && ag.equals("robot")) {
            result = model.saveBeer();
        } else if (action.equals(LITERALS.get("make(pinchos)"))) {
            result = model.makePinchos();
        } else if (action.equals(LITERALS.get("get(dish,cupboard)"))) {
            result = model.getPlateFromCupboard();
        } else if (action.getFunctor().equals("deliver")) {
            // wait 4 seconds to finish "deliver"
            try {
                Thread.sleep(4000);
                result = model.addBeer((int) ((NumberTerm) action.getTerm(1)).solve());
            } catch (Exception e) {
                logger.info("Failed to execute action deliver!" + e);
            }

        } else if (action.equals(LITERALS.get("nam(pincho)"))) {
            result = model.namPincho();
        } else if (action.equals(LITERALS.get("empty(bin)"))) {
            result = model.emptyBin();
        } else if (action.equals(LITERALS.get("drop(bin)"))) {
            result = model.dropBin();
        } else if (action.equals(LITERALS.get("put(dish,dishwasher)"))) {
            result = model.putDishInDishwasher();
            dishwasherCycles += 5;
        } else if (action.equals(LITERALS.get("put(dish,cupboard)"))) {
            result = model.putDishInCupboard();
        } else if (action.equals(LITERALS.get("get(dish,dishwasher)"))) {
            result = model.getDishInDishwasher();
        } else if (action.equals(LITERALS.get("dishwasher(on)"))) {
            result = model.dishwasherOn();
        } else if (action.getFunctor().equals("take") && action.getTerm(0).toString().equals("plate")) {
            result = model.takePlateOwner();
        } else if (action.equals(LITERALS.get("recycle(owner,beer)"))) {
            result = model.recycleBeer();
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
