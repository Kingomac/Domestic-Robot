package house;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Identificación de los distintos robots
 */
public enum SpecializedRobots {
  ROBOT(0),
  CLEANER(1),
  STOREKEEPER(2);

  private static final Map<String, SpecializedRobots> STRING_MAP = Arrays.stream(SpecializedRobots.values())
      .collect(Collectors.toMap(SpecializedRobots::name, Function.identity()));

  public static SpecializedRobots from(String x) {
    if (x == null)
      throw new IllegalArgumentException();

    return STRING_MAP.get(x.toUpperCase());
  }

  private final int value;

  private SpecializedRobots(int v) {
    this.value = v;
  }

  public int getValue() {
    return value;
  }

}
