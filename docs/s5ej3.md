# Ejercicio 3 (Semana 5)

Modifica la acción move_towards(Location), de forma que utilice cuatro nuevas acciones para el desplazamiento del robot: arriba, abajo, izquierda y derecha. Es decir, cada paso que dé el robot no puede ser en diagonal, sino que debe utilizar las acciones anteriores en su movimiento. 

Ten en cuenta que para resolver este ejercicio, la percepción de ubicación del robot (en el entorno) debe mejorarse (por ejemplo, at(robot, columna, línea)) para que pueda decidir la acción a tomar en cada paso que dé. 

Una vez modificado el entorno para reflejar este cambio, agrega obstáculos en el entorno y modifica el código para que el robot los esquive en sus desplazamientos. 

Fíjate que la elección de las acciones hacia arriba, hacia abajo, hacia la izquierda y hacia la derecha debe estar limitada por los obstáculos y los límites de la habitación.

1. Hicimos un `enum` para representar los movimientos. `MoveDirections.java`
```java
public enum MoveDirections {
  UP,
  DOWN,
  LEFT,
  RIGHT
}
```

2. Añadimos las creencias de donde están el owner, las latas tiradas y la nevera. `HouseEnv.java`
```java
void updatePercepts() {
//..
addPercept("robot", Literal.parseLiteral(String.format("at(robot,%d,%d)", lRobot.x, lRobot.y)));
addPercept("robot", Literal.parseLiteral(String.format("where(owner,%d,%d)", model.lOwner.x, model.lOwner.y)));
addPercept("robot", Literal.parseLiteral(String.format("where(bin,%d,%d)", model.lBin.x, model.lBin.y)));
addPercept("robot",Literal.parseLiteral(String.format("where(fridge,%d,%d)", model.lFridge.x, model.lFridge.y)));
addPercept("robot", Literal.parseLiteral(String.format("where(trash,%d,%d)", model.lBeer.x, model.lBeer.y)));
//..
```

3. Modificamos los planes para que se mueva a las posiciones correspondientes cambiando de eje a cada movimiento para tener un trazo más normal.
```prolog
+lastAxis(x) <- -lastAxis(y).
+lastAxis(y) <- -lastAxis(x).

+!go_to(robot, Sitio) : where(Sitio,X,Y) <- .print("[robot]: going to ", Sitio); !go_at(robot,X,Y).
+!go_to(robot, Sitio) <- .print("No se como se va a ", Sitio).

+!go_at(robot, X, Y) : at(robot, X, Y) <- true.
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & not lastAxis(x) & at(robot, X, Y) & X < DestX <- move_towards(right); +lastAxis(x); -lastAxis(y); !go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & not lastAxis(x) & at(robot, X, Y) & X > DestX <- move_towards(left); +lastAxis(x); -lastAxis(y);!go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & not lastAxis(y) & at(robot, X, Y) & Y < DestY <- move_towards(down); +lastAxis(y);-lastAxis(x);!go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & not lastAxis(y) & at(robot, X, Y) & Y > DestY <- move_towards(up); +lastAxis(y);-lastAxis(x);!go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & at(robot, X, Y) & X < DestX <- move_towards(right); +lastAxis(x); !go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & at(robot, X, Y) & X > DestX <- move_towards(left); +lastAxis(x);!go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & at(robot, X, Y) & Y < DestY <- move_towards(down); +lastAxis(y);!go_at(robot,DestX,DestY).
+!go_at(robot, DestX, DestY) : not at(robot, DestX, DestY) & at(robot, X, Y) & Y > DestY <- move_towards(up); +lastAxis(y);!go_at(robot,DestX,DestY).
```

4. Añadimos los muros. `HouseModel.java`
```java
public HouseModel() {
  //...
  addWall(2, 3, 2, 3);
  //...
}
```
5. Cambiamos el `move_towards()`. `HouseModel.java`
```java
boolean moveTowards(MoveDirections dir) {
  System.out.println("MOVE TOWARDS " + dir.name());
  Location locInitial = getAgPos(0);
  Location r1 = getAgPos(0);
  MoveDirections dirDodge = moveInDirection(r1, dir);

  int i;
  List<MoveDirections> directions = new ArrayList<>();
  directions.add(dirDodge);
  for (MoveDirections j : MoveDirections.values()) {
    if (j != dirDodge)
      directions.add(j);
  }
  for (i = 0; !isFree(r1) && i < directions.size(); i++) {
    r1.x = locInitial.x;
    r1.y = locInitial.y;
    moveInDirection(r1, directions.get(i));
  }
  if (i >= directions.size() - 1) {
    System.out.println("AAAAAAAAAA NO ME PUEDO MOVER");
    return false;
  }
  setAgPos(0, r1); // move the robot in the grid

  // repaint the fridge and owner locations
  if (view != null) {
    view.update(lFridge.x, lFridge.y);
    view.update(lOwner.x, lOwner.y);
  }
  return true;
}
```

