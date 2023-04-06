import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** class that implements the View of Domestic Robot application */
public class HouseView extends GridWorldView {

    HouseModel hmodel;

    public HouseView(HouseModel model) {
        super(model, "Domestic Robot", 700);
        hmodel = model;
        defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
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
        }
        repaint();
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
