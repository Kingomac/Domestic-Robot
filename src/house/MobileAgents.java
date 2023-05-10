package house;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Identificación de los distintos robots
 */
public enum MobileAgents {
  ROBOT(0, Places.BASE_ROBOT, "robot"),
  CLEANER(1, Places.BASE_CLEANER, "robot"),
  STOREKEEPER(2, Places.BASE_STOREKEEPER, "robot"),
  BURNER(3, Places.BASE_BURNER, "robot"),
  OWNER(4, Places.OWNER, "owner");

  /**
   * "ROBOT" -> MobileAgents.ROBOT
   */
  private static final Map<String, MobileAgents> STRING_MAP = Arrays.stream(MobileAgents.values())
      .collect(Collectors.toMap(MobileAgents::name, Function.identity()));

  public static MobileAgents from(String x) {
    if (x == null)
      throw new IllegalArgumentException();

    return STRING_MAP.get(x.toUpperCase());
  }

  public static List<String> AGENTS = Arrays.stream(MobileAgents.values()).map(i -> i.agentName).distinct()
      .collect(Collectors.toList());

  private final int value;
  public final Places base;
  public final String agentName;

  private MobileAgents(int v, Places base, String agentName) {
    this.value = v;
    this.base = base;
    this.agentName = agentName;
  }

  public int getValue() {
    return value;
  }

}
