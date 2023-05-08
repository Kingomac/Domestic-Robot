package movement;

import house.HouseModel;
import house.SpecializedRobots;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class NextDirection extends DefaultInternalAction {

  private static HouseModel model;

  private static PathFinder pathFinder;

  public static void initialize(HouseModel model) {
    NextDirection.model = model;
    pathFinder = new PathFinder(model);
  }

  @Override
  public Object execute(TransitionSystem ts, Unifier un, Term[] args) {
    try {
      int origenX = (int) ((NumberTerm) args[0]).solve();
      int origenY = (int) ((NumberTerm) args[1]).solve();
      int destinoX = (int) ((NumberTerm) args[2]).solve();
      int destinoY = (int) ((NumberTerm) args[3]).solve();
      SpecializedRobots robot = SpecializedRobots.from(args[4].toString());

      if (!model.inGrid(destinoX, destinoY))
        throw new IllegalArgumentException("nextDirection destiny must be a free valid position");

      Location nextLocation = pathFinder.getNextPosition(new Location(origenX, origenY),
          new Location(destinoX, destinoY), robot);
      if (nextLocation == null) {
        nextLocation = new Location(origenX, origenY);
        throw new JasonException("Camino no encontrado");
      }
      MovementDirections toret = null;
      if (nextLocation.x < origenX)
        toret = MovementDirections.LEFT;
      else if (nextLocation.x > origenX)
        toret = MovementDirections.RIGHT;
      else if (nextLocation.y < origenY)
        toret = MovementDirections.UP;
      else if (nextLocation.y > origenY)
        toret = MovementDirections.DOWN;
      else {
        toret = MovementDirections.NONE;
        throw new RuntimeException("Next direction is to stay still");
      }
      // System.out.println("NEXTDIRECTION -> " + toret.name());
      return un.unifies(args[5], new Atom(toret.name().toLowerCase()));

    } /*
       * catch (IllegalArgumentException e) {
       * 
       * } catch(NoValueException e) {
       * 
       * }
       */ catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    return false;
  }

}
