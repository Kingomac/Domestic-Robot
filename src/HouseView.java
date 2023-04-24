import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.net.URI;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.dot.*;
import org.jgrapht.traverse.*;

/** class that implements the View of Domestic Robot application */
public class HouseView extends GridWorldView {

    HouseModel hmodel;
    private final static int WINDOW_SIZE = 1000;

    public HouseView(HouseModel model) {
        super(model, "Domestic Robot", WINDOW_SIZE);
        hmodel = model;
        defaultFont = new Font("Arial", Font.BOLD, 12000 / WINDOW_SIZE); // change default font
        setResizable(false);
        setVisible(true);
        repaint();
    }

    /** draw application objects */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        super.drawAgent(g, x, y, Color.lightGray, -1);
        switch (object) {
            case HouseModel.FRIDGE:
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Fridge (" + hmodel.availableBeers + ")");
                break;
            case HouseModel.OWNER:
                String o = "Owner";
                if (hmodel.sipCount > 0) {
                    o += " (" + hmodel.sipCount + ")";
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, o);
                break;
            case HouseModel.BIN:
                super.drawAgent(g, x, y, new Color(139, 69, 19), -1);
                g.setColor(Color.white);
                drawString(g, x, y, defaultFont, String.format("Bin (%d)", hmodel.binCount));
                break;

            case HouseModel.TRASH:
                super.drawAgent(g, x, y, Color.pink, -1);
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Dropped beer");
                break;
            case HouseModel.DELIVERY:
                super.drawAgent(g, x, y, Color.green, -1);
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, "Delivery");
                break;
            case HouseModel.OWNER_MUSK:
                String m = "Musk";
                if (hmodel.sipCountMusk > 0) {
                    m += " (" + hmodel.sipCountMusk + ")";
                }
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, m);
                break;
            case HouseModel.DISHWASHER:
                super.drawAgent(g, x, y, new Color(92, 158, 224), -1);
                g.setColor(Color.black);
                String dishwasherM;
                if (hmodel.dishwasherState.equals(DishwasherStates.ON)) {
                    dishwasherM = "**Dishwasher**";
                } else {
                    dishwasherM = String.format("Dishwasher (%d)", hmodel.dishwasherCount);
                }
                drawString(g, x, y, defaultFont, dishwasherM);
                break;
            case HouseModel.CUPBOARD:
                super.drawAgent(g, x, y, new Color(179, 129, 43), -1);
                g.setColor(Color.black);
                drawString(g, x, y, defaultFont, String.format("Cupboard (%d)", hmodel.cupboardCount));
                break;
        }
    }

    @Override
    public void drawEmpty(Graphics g, int x, int y) {
        g.setColor(new Color(238, 238, 238));
        g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
        g.setColor(Color.lightGray);
        g.drawRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Location lRobot = hmodel.getAgPos(SpecializedRobots.ROBOT.getValue());
        Location lCleaner = hmodel.getAgPos(SpecializedRobots.CLEANER.getValue());
        Location lStorekeeper = hmodel.getAgPos(SpecializedRobots.STOREKEEPER.getValue());

        if (x == lRobot.x && y == lRobot.y) { // Dibujar robot mayordomo
            if (!lRobot.equals(Places.OWNER.location) && !lRobot.equals(Places.FRIDGE.location)) {
                c = Color.yellow;
                if (hmodel.carryingBeer)
                    c = Color.orange;
                super.drawAgent(g, x, y, c, -1);
                g.setColor(Color.black);
                super.drawString(g, x, y, defaultFont, "Robot");
            }
        } else if (x == lCleaner.x && y == lCleaner.y) { // Dibujar cleaner
            c = Color.cyan;
            if (hmodel.carryingTrash) {
                c = new Color(25, 125, 175);
            }
            super.drawAgent(g, x, y, c, -1);
            g.setColor(Color.black);
            super.drawString(g, x, y, defaultFont, "Cleaner");
        } else if (x == lStorekeeper.x && y == lStorekeeper.y) { // Dibujar storekeeper
            c = Color.red;
            if (hmodel.carryingDelivery) {
                c = new Color(255, 50, 50);
            }
            super.drawAgent(g, x, y, c, -1);
            g.setColor(Color.black);
            super.drawString(g, x, y, defaultFont, "Storekeeper");
        }

    }
}
