# Ejercicio 2 (Semana 4)

Modifica el código del modelo y de la vista para incluir una zona de entrega de productos del supermercado (cercana al frigorífico) y modifica el código para que el robot se desplace a dicha posición cuando compruebe que no hay cervezas en el frigorífico. Del mismo modo cuando el robot reciba el pedido, deberá desplazarse hasta la posición desde donde abre la puerta del frigorífico, y una vez allí, se actualizará el número de cervezas disponibles y podrá continuar con la programación que tenía para proporcionar cervezas al propietario.

## Añadir la zona de entrega

1. Crear la constante que va a permitir insertarla en el grid y localización. `HouseModel.java`
```java
public static final int DELIVERY = 64;
Location lDelivery = new Location(0, GSize-1);
```

2. Crear contador de cervezas en la zona de entregas `HouseModel.java`
```java
int deliveryBeers	= 0;
```

3. Representar la zona en la GUI. `HouseView.java`:
```java
public void draw(Graphics g, int x, int y, int object) {
	//  ...
    case HouseModel.DELIVERY:
		  super.drawAgent(g, x, y, Color.pink, -1); // fondo en rosa
		  g.setColor(Color.black); // texto en negro
		  drawString(g, x, y, defaultFont, "Delivery (" + hmodel.deliveryBeers + ")");
		break;
  // ...
}
```

## Cambiar planes del robot para que la use

1. Cambié el plan `bring` para que cuando no haya cerveza haga el pedido y se situe en la zona de entrega. `robot.asl`
```
+!bring(owner,beer)
   :  not available(beer,fridge)
   <- .send(supermarket, achieve, order(beer,3));
      !go_at(robot,delivery).
```

2. Cambié los planes `+delivered(beer, _, _)` para que guarde la cerveza en la nevera y si iba a llevarle una al owner que lo haga. `robot.asl`
```
+delivered(beer,_Qtd,_OrderId)[source(supermarket)]
  : .intend(bring(owner,beer))
  <- get(delivery);
  	.drop_intention(bring(owner,beer));
  	!go_at(robot, fridge);
  	open(fridge);
	  save(beer);
	  close(fridge);
  	+available(beer,fridge);
	  !bring(owner,beer).

+delivered(beer,_Qtd,_OrderId)[source(supermarket)]
  : not .intend(bring(owner,beer))
  <- get(delivery);
  	!go_at(robot, fridge);
  	open(fridge);
	  save(beer);
	  close(fridge);
  	+available(beer,fridge).
```

## Añadir las nuevas funciones al modelo

1. Detectar funciones en el entorno. `HouseEnv.java`:
```java
// ...
public static final Literal litGetDelivery = Literal.parseLiteral("get(delivery)");
public static final Literal litSaveBeer = Literal.parseLiteral("save(beer)");
// ...
public boolean executeAction(String ag, Structure action) {
    // ...
    } else if(action.equals(litGetDelivery)) {
			result = model.getDelivered();
		} else if(action.equals(litSaveBeer)) {
			result = model.saveBeer();
    // ...
```

2. Añadir las nuevas funciones en el modelo. `HouseModel.java`:
```java
boolean getDelivered() {
	if(deliveryBeers > 0 && !carryingBeer) {
  	carryingBeer = true;
		deliveryBeers-=3;
		if(view != null)
			view.update(lDelivery.x, lDelivery.y);
		return true;
	}
	return false;
}
	
boolean saveBeer() {
  if(fridgeOpen && carryingBeer) {
	  availableBeers += 3;
		carryingBeer = false;
		if(view != null)
			view.update(lFridge.x,lFridge.y);
		  return true;
		}
	return false;
}
```

## Otra solución

En este caso el robot una vez el owner le pide una cerveza y no quedan, hace el pedido, va a la zona de entregas, recoge el pedido, lo guarda en la nevera, coge una cerveza y se la lleva al dueño.
Otra solución podría ser que a la hora de dejar las cervezas deje solo 2 y una se la quede para llevarsela al dueño pero, habría que complicar bastante el código de los planes de `delivered`.

