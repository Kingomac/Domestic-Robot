import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import jason.*;
import jason.environment.grid.Location;

public class HouseModelTest {
  @Test
  void addNumbers() {
    HouseModel h = new HouseModel();
    int prev = h.availableBeers;
    h.saveBeer();
    assertEquals(h.availableBeers, prev + 3);
  }

  @Test
  public void testFridgeOpen() {
    HouseModel h = new HouseModel();
    h.openFridge();
    h.openFridge();
    h.openFridge();
    assertTrue(h.fridgeOpen);
    h.closeFridge();
    h.closeFridge();
    assertFalse(h.fridgeOpen);
  }

  @Test
  public void testMoveRobot() {
    HouseModel h = new HouseModel();
    Location dest1 = new Location(Places.BASE_CLEANER.x, Places.BASE_CLEANER.y - 1);
    Location dest2 = new Location(Places.BASE_CLEANER.x + 2, Places.BASE_CLEANER.y - 1);
    h.moveRobot(SpecializedRobots.CLEANER, dest1);
    assertEquals(h.getAgPos(SpecializedRobots.CLEANER.getValue()), dest1);
    h.moveRobot(SpecializedRobots.CLEANER, dest2);
    assertNotEquals(h.getAgPos(SpecializedRobots.CLEANER.getValue()), dest2);
  }
}