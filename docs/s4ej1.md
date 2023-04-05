# Ejercicio 1 (Semana 4)

Para realizar los cambios del movimiento implementé un nuevo método para calcular la distancia entre dos puntos:

```java
static double distBetween(Location a, Location b) {
	return Math.sqrt(Math.abs(a.x - b.x) + Math.abs(a.y - b.y));
}
```

Al método `moveTowards` lo modifiqué de forma que ahora (editando en el fichero `HouseModel.java`):
1. Calcula la distancia entre el robot y el desitno.
2. Si $distancia > \sqrt2$ se realiza movimiento recto o en diagonal.
```java
if(dist > Math.sqrt(2)) { // Si está lejos se permite el movimiento diagonal
	if (r1.x < dest.x - 1)        r1.x++;
	else if (r1.x > dest.x + 1)   r1.x--;                                                
	if (r1.y < dest.y - 1)        r1.y++;
	else if (r1.y > dest.y + 1)   r1.y--;
}
```
3. Si $1 < distancia \le \sqrt2$ intenta aproximarse por el eje X y si no puede lo intenta por el Y.
```java
else { // Si no se aproxima sin hacer diagonales
	// Primero se intenta acercar por el eje X
	int sumX = 0;
	if(r1.x < dest.x) sumX = 1;
	else if (r1.x > dest.x) sumX = -1;
	if(sumX != 0) {
		r1.x += sumX;
	} else { // Si no lo consigue lo hace por el eje Y
		int sumY = 0;
		if(r1.y < dest.y) sumY = 1;
		else if (r1.y > dest.y) sumY = -1;
		r1.y += sumY;
	}
}
```
4. Después se actualiza una nueva variable que va a definir dónde está el robot:
  1. Se declara como:
  ```java
enum Place {
  FRIDGE,
	OWNER,
	BASE,
	OTHER
}

Place actualPlace = Place.OTHER;
```
  2. Se actualiza según la posición en el método `moveTowards`

Queda finalmente así:
```java
boolean moveTowards(Location dest) {
  Location r1 = getAgPos(0);
	double dist = distBetween(r1, dest);
	
	if(dist > Math.sqrt(2)) { // Si está lejos se permite el movimiento diagonal
		if (r1.x < dest.x - 1)        r1.x++;
		else if (r1.x > dest.x + 1)   r1.x--;                                                
		if (r1.y < dest.y - 1)        r1.y++;
		else if (r1.y > dest.y + 1)   r1.y--;
	}
	else { // Si no se aproxima sin hacer diagonales
		// Primero se intenta acercar por el eje X
		int sumX = 0;
		if(r1.x < dest.x) sumX = 1;
		else if (r1.x > dest.x) sumX = -1;
		if(sumX != 0) {
			r1.x += sumX;
		} else { // Si no lo consigue lo hace por el eje Y
			int sumY = 0;
			if(r1.y < dest.y) sumY = 1;
			else if (r1.y > dest.y) sumY = -1;
			r1.y += sumY;
		}
	}
       
	setAgPos(0, r1); // move the robot in the grid

  // Actualizar dónde está el robot
	if(distBetween(lOwner, r1) == 1) actualPlace = Place.OWNER;
	else if(distBetween(lFridge, r1) == 1) actualPlace = Place.FRIDGE;
	else if(distBetween(lBase, r1) == 0) actualPlace = Place.BASE;
	else actualPlace = Place.OTHER;
		

      // repaint the fridge and owner locations
  if (view != null) {
    view.update(lFridge.x,lFridge.y);
    view.update(lOwner.x,lOwner.y);
  }
        return true;
}
```
  3. Actualizar los percepts para que se detecte la posición del robot correctamente (fichero `HouseEnv.java`):
```java
void updatePercepts() {
  // clear the percepts of the agents
  clearPercepts("robot");
  clearPercepts("owner");

  // get the robot location
  Location lRobot = model.getAgPos(0);

  // add agent location to its percepts
  if (model.actualPlace.equals(HouseModel.Place.FRIDGE)) {
     addPercept("robot", af);
  }
  if (model.actualPlace.equals(HouseModel.Place.OWNER)) {
     addPercept("robot", ao);
  }

  // add beer "status" the percepts
  if (model.fridgeOpen) {
     addPercept("robot", Literal.parseLiteral("stock(beer,"+model.availableBeers+")"));
  }
		
  if (model.sipCount > 0) {
     addPercept("robot", hob);
     addPercept("owner", hob);
  }
}
```
