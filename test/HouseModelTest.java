import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import jason.*;

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
    assertTrue(true);
  }
}