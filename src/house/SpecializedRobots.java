package house;

/*
 * Identificación de los distintos robots
 */
public enum SpecializedRobots {
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
