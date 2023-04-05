# Ejercicio 3 (Semana 4)

Mi solución para comprobar quién va a realizar la acción es añadirlo como condición en los ifs que comprueban qué acción se va a realizar.
Fichero: `HouseEnv.java`
```java
@Override
public boolean executeAction(String ag, Structure action) {
  System.out.println("["+ag+"] doing: "+action);
  boolean result = false;
  if (action.equals(of) && ag.equals("robot")) { // of = open(fridge)
  } else if (action.equals(clf) && ag.equals("robot")) { // clf = close(fridge)
  // ...
  } else if (action.getFunctor().equals("move_towards") && ag.equals("robot")) {
  // ...
  } else if (action.equals(gb) && ag.equals("robot")) {
  // ...
  } else if (action.equals(hb) && ag.equals("robot")) {
  // ...
  } else if (action.equals(sb) && ag.equals("owner")) {
  // ...
  } else if(action.equals(litGetDelivery) && ag.equals("robot")) {
	// ...
  } else if(action.equals(litSaveBeer) && ag.equals("robot")) {
  // ...
	} else if (action.getFunctor().equals("deliver") && ag.equals("supermarket")) {
  // ...
  }
  // ...
}
```
