package movement;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Arrays;

public enum MovementDirections {
  UP,
  DOWN,
  LEFT,
  RIGHT,
  NONE;

  private static final Map<String, MovementDirections> STRINGS_MAP = Arrays.stream(MovementDirections.values())
      .collect(Collectors.toMap(MovementDirections::name, Function.identity()));

  public static MovementDirections from(String in)
      throws ClassCastException, IllegalArgumentException, NullPointerException {
    if (in == null)
      throw new IllegalArgumentException();
    return STRINGS_MAP.get(in.toUpperCase());
  }
}
