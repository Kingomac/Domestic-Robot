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
  ROBOT(0, Places.BASE_ROBOT, "robot", false),
  CLEANER(1, Places.BASE_CLEANER, "robot", false),
  STOREKEEPER(2, Places.BASE_STOREKEEPER, "robot", false),
  BURNER(3, Places.BASE_BURNER, "robot", false),
  OWNER(4, Places.OWNER, "owner", true);

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
  public final boolean isOwner;

  private MobileAgents(int v, Places base, String agentName, boolean isOwner) {
    this.value = v;
    this.base = base;
    this.agentName = agentName;
    this.isOwner = isOwner;
  }

  public int getValue() {
    return value;
  }

}
