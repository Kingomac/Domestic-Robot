import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class getDirection extends DefaultInternalAction {

  @Override
  public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

    return true;
  }

}
