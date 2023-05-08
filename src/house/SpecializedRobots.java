package house;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Identificación de los distintos robots
 */
public enum SpecializedRobots {
  ROBOT(0, Places.BASE_ROBOT),
  CLEANER(1, Places.BASE_CLEANER),
  STOREKEEPER(2, Places.BASE_STOREKEEPER),
  BURNER(3, Places.BASE_BURNER);

  private static final Map<String, SpecializedRobots> STRING_MAP = Arrays.stream(SpecializedRobots.values())
      .collect(Collectors.toMap(SpecializedRobots::name, Function.identity()));

  public static SpecializedRobots from(String x) {
    if (x == null)
      throw new IllegalArgumentException();

    return STRING_MAP.get(x.toUpperCase());
  }

  private final int value;
  public final Places base;

  private SpecializedRobots(int v, Places base) {
    this.value = v;
    this.base = base;
  }

  public int getValue() {
    return value;
  }

}
