# Memoria Robot Doméstico - Primera iteración del proyecto

> Hecho por:
> 
> Javier Veloso Boubeta
> 
> Cristian Sousa Vidal
> 
> Mario Vila Comesaña

- [Memoria Robot Doméstico - Primera iteración del proyecto](#memoria-robot-doméstico---primera-iteración-del-proyecto)
  - [Elementos participantes del proyecto](#elementos-participantes-del-proyecto)
  - [Robots especializados](#robots-especializados)
  - [Movimiento de robots](#movimiento-de-robots)
    - [Movimiento simple](#movimiento-simple)
    - [Movimiento complejo](#movimiento-complejo)
  - [Supermercados](#supermercados)
  - [Ejemplo de 2 Owners](#ejemplo-de-2-owners)
  - [Calidad de código](#calidad-de-código)


## Elementos participantes del proyecto

- *Papelera*: se encarga principalmente de almacenar la basura que recoge el robot cleaner, y cuando ésta se llena, el cleaner vacía la papelera en la zona de delivery.
- *Zona de Delivery*: en esta zona, el storekeeper decide a qué supermercado comprarle cervezas en función de su precio mínimo, las recibe y las lleva al fridge. Además, el cleaner se encarga de vaciar aquí la papelera.
- *Robot especializados*: el proyecto contará con una serie de robots los cuales estarán diseñados y programados para realizar unas determinadas tareas en función del estado y características del entorno.
- *Owner*: es el dueño del robot, que solicita cervezas para consumirlas y posteriormente tira al escenario las latas de cerveza. Estas latas se tiran al entorno de forma aleatoria. Este agente cuenta con una cantidad de dinero de partida, de la cual la mitad es suministrada al robot para que éste realice la compra de cervezas.

## Robots especializados

- *Robot mayordomo*: se trata de un autómata el cual tiene como principal función suministrar al dueño una unidad de cerveza. Para realizar esta acción, este robot se desplaza desde la nevera al sillón donde se encuentra localizado el dueño. Además, este dispositivo se encarga de controlar a los demás robots, ya que los utiliza como apoyo para realizar tareas de suministro y limpieza.
```prolog
/** Si el robot tiene la intención de llevarle la cerveza a un owner espera a 
terminar para llevarle cerveza a otro **/ 
+!bring(Owner, beer) : .intend(give(A,_)) &
    A \== Owner <- .wait(3000);
           !bring(Owner, beer).
+!bring(Owner, beer) : true <- !give(Owner, beer).
-!bring(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).

/** Acción de coger y llevarle al owner correspondiente la cerveza **/
+!give(Owner,beer)
   :  not too_much(Owner,beer)
   <- !go_to(robot,fridge);
      open(fridge);
      !get_when_available(beer);
      close(fridge);
      !go_to(robot,Owner);
      hand_in(Owner,beer);
      ?has(Owner,beer);
      // remember that another beer has been consumed
      .date(YY,MM,DD);
           .time(HH,NN,SS);
      +consumed(Owner,YY,MM,DD,HH,NN,SS,beer).
	  
+!give(Owner,beer)
   :  too_much(Owner,beer) &
    limit(beer,L)
   <- .concat("The Department of Health does not allow me to give ", Owner, " more than ", L,
              " beers a day! I am very sorry about that!",M);
      .send(Owner,tell,msg(M)).
	  
-!give(_,_)
   :  true
   <- .current_intention(I);
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).
	  
/** Si no hay cerveza en la nevera se queda esperando a que haya **/
+!get_when_available(beer) : available(fridge, beer) <- .wait(100);
           open(fridge);
           get(beer).
+!get_when_available(beer) : not available(fridge, beer) <- .wait({ +available(fridge,beer) });
           !get_when_available(beer). 
```

- *Robot limpiador* (**Cleaner**): Este autómata se encarga de limpiar las latas de cerveza que el dueño tira al suelo o escenario,  para ello, se desplaza hasta una zona adyacente a la lata, la recoge y la lleva a la papelera para desecharla. Si la papelera se encuentra llena, el robot recogerá toda la basura y se desplazará hasta la zona de delivery para deshacerse de toda la basura almacenada. Este robot comienza su movimiento desde una zona de partida, a la cual solo vuelve tras vaciar la papelera.
```prolog
+bin(full) <- .wait(free(cleaner));
           -free(cleaner);
           !take_out_trash;
           +free(cleaner).
+where(trash, A, B) <- .wait(free(cleaner));
           -free(cleaner);
           !clean_trash;
           +free(cleaner).

+!take_out_trash <- !go_to(cleaner, bin);
           empty(bin);
           !go_to(cleaner, delivery);
           drop(bin);
           !go_to(cleaner, base_cleaner). 

+!clean_trash : not where(trash, _, _) <- true.
+!clean_trash : not bin(full) <- !go_to(cleaner, trash);
           take(trash);
           !go_to(cleaner, bin);
           drop(trash).
-!clean_trash <- .wait(3000);
           !clean_trash.
```
- *Robot mozo de almacén* (**Storekeeper**): el Storekeeper es un autómata que tiene como función recoger cervezas en la zona de deliverý y llevarlas al fridge si éste contiene 2 cervezas o menos.  Además, es el encargado de decidir a cuál de los diferentes supermercados comprarle las cervezas, escogiendo el que las tenga más baratas al momento de la compra. Y en caso de que no haga falta recoger ninguna cerveza, éste autómata se quedará en una zona de espera, hasta que haga falta comprar más cervezas.
```prolog
/** Cuando llegue el pedido el storekeeper recoge el pedido y hace el pago **/
+delivered(beer,Qtd,OrderId): min_price(beer, P, S) &
    money(M) &
    M > P
  <- get(delivery);
  	.concat("Pedido recibido con éxito: beer(", OrderId, "), ", Qtd, " unidades e importe ", P, " robux", Mensaje);
  	.send(S, tell, msg(Mensaje));
	.send(S, tell, payment(beer, Qtd, P));
           
	-+money(M - P);
  	!go_to(storekeeper, fridge);
  	open(fridge);
	save(beer);
	close(fridge);
	!go_to(storekeeper, base_storekeeper).

/** Si hay menos de 2 cervezas se hace un pedido **/
+available(fridge, beer, X) : at(storekeeper, base_storekeeper) &
    X <= 2 <-
	!go_to(storekeeper, delivery);
	!update_prices;
           .wait(5000);
           ?min_price(beer, P, S);
	?money(DineroActual);
           if(DineroActual <=  P) { !request_money };
	.send(S, achieve, order(beer,3)).
```


## Movimiento de robots

Para la implementación hemos desarrollado dos variantes distintas, que están mayormente implementadas en ASL con reglas Prolog y “percepts”:

### Movimiento simple

Permite los desplazamientos diagonales y no tiene en cuenta que el robot se sitúe en una posición adyacente al destino. Lo utilizamos para desarrollo porque ocupa menos ciclos y permite una depuración más cómoda.

### Movimiento complejo

Realiza alternancia entre los ejes vertical y horizontal para darle más naturalidad y esquiva obstáculos y otros agentes tratando de elegir siempre el mejor camino aunque por limitaciones del conocimiento que puede tener a veces terminan dando un buen rodeo. Nuestro objetivo no era conseguir el movimiento óptimo, sino llegar al destino.
```prolog
+lastAxis(Tipo,x) <- -lastAxis(Tipo,y).
+lastAxis(Tipo,y) <- -lastAxis(Tipo,x).
```

El movimiento de los distintos robots funciona en base a una serie de predicados construidos en Prolog y la función de movimiento simple implementada en Java. El movimiento se caracteriza por el hecho de que el robot sólo pueda desplazarse  a las posiciones adyacentes, es decir, en cruz, descartando así la posiciones situadas en las diagonales.

Casos:
- *Caso base*: el robot está en su destino, por tanto termina el movimiento.
```prolog
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
```
- *Caso base de error*: el entorno no ha añadido la creencia `where(Sitio, X, Y)` correspondiente, por tanto se espera a que se añada.
```prolog
+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe");
           .wait(where(Sitio, _, _));
           !go_to(Tipo, Sitio).
```
- *Caso 1*: el movimiento se puede realizar de forma óptima cambiando de eje.
```prolog
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
  at(Tipo, X, Y) &
  where(Sitio, DestX,DestY) &
  X > DestX &
  can_go(Tipo, left) &
  lastAxis(Tipo, y) <- move_robot(Tipo, left);
    +lastAxis(Tipo,x);
    !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
  at(Tipo, X, Y) &
  where(Sitio, DestX,DestY) &
  X < DestX &
  can_go(Tipo, right) &
  lastAxis(Tipo, y) <- move_robot(Tipo, right);
    +lastAxis(Tipo,x);
    !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
  at(Tipo, X, Y) &
  where(Sitio, DestX,DestY) &
  Y > DestY &
  can_go(Tipo, up) &
  lastAxis(Tipo, x) <- move_robot(Tipo, up);
    +lastAxis(Tipo, y);
    !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
  at(Tipo, X, Y) &
  where(Sitio, DestX,DestY) &
  Y < DestY &
  can_go(Tipo, down) &
  lastAxis(Tipo, x) <- move_robot(Tipo, down);
    +lastAxis(Tipo, y);
    !go_to(Tipo, Sitio).
```
- *Caso 2*: el movimiento se puede realizar de forma óptima sin cambiar de eje.
```prolog
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    X > DestX &
    can_go(Tipo, left) <- move_robot(Tipo, left);
           +lastAxis(Tipo, x);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    X < DestX &
    can_go(Tipo, right) <- move_robot(Tipo, right);
           +lastAxis(Tipo, x);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    Y > DestY &
    can_go(Tipo, up) <- move_robot(Tipo, up);
           +lastAxis(Tipo, y);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    Y < DestY &
    can_go(Tipo, down) <- move_robot(Tipo, down);
           +lastAxis(Tipo, y);
           !go_to(Tipo, Sitio).
```
- *Caso 3*: se esquiva en una dirección aleatoria teniendo en cuenta la dirección de destino.
```prolog
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    X > DestX &
    not can_go(Tipo, left) <- !gen_dodge_direction(Tipo, left);
           !dodge(Tipo, left, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    X < DestX &
    not can_go(Tipo, right) <- !gen_dodge_direction(Tipo, right);
           !dodge(Tipo, right, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    Y > DestY &
    not can_go(Tipo, up) <- !gen_dodge_direction(Tipo, up);
           !dodge(Tipo, up, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    Y < DestY &
    not can_go(Tipo, down) <- !gen_dodge_direction(Tipo, down);
           !dodge(Tipo, down, 10);
           !go_to(Tipo, Sitio).
```
- *Caso 4*: no puede esquivar mientras va al destino, lo hace en otra dirección.
```prolog
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    not can_go(Tipo, left) <- !gen_dodge_direction(Tipo, left);
           !dodge(Tipo, left, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    not can_go(Tipo, right) <- !gen_dodge_direction(Tipo, right);
           !dodge(Tipo, right, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    not can_go(Tipo, up) <- !gen_dodge_direction(Tipo, up);
           !dodge(Tipo, up, 10);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) &
    not can_go(Tipo, down) <- !gen_dodge_direction(Tipo, down);
           !dodge(Tipo, down, 10);
           !go_to(Tipo, Sitio).
```
- *Caso 5*: no se puede mover, lo intenta en cualquier dirección.
```prolog
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) <- !dodge_any(Tipo, 2);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) <- !dodge_any(Tipo, 2);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) <- !dodge_any(Tipo, 2);
           !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) &
    at(Tipo, X, Y) &
    where(Sitio, DestX,DestY) <- !dodge_any(Tipo, 2);
           !go_to(Tipo, Sitio).
```
- *Caso 6*: está atrapado (probablemente le cortaron todos los caminos muros y otros agentes), espera 3 segundos y lo vuelve a intentar.
```prolog
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio);
           .wait(3000);
           !go_to(Tipo, Sitio).
```

## Supermercados

Contamos con 6 supermercados, que se distinguen por su nombre (Mercadona, Lidl, Froiz, Eroski, Gadis y Dia), cada supermercado tiene un stock y un precio distinto en sus cervezas, el cual se genera de manera aleatoria al iniciar el programa. Y conforme pasa el tiempo, el precio decrece en base a un porcentaje fijo, también aleatorio, lo que hace que todos los supermercados bajen sus precios con el paso del tiempo, pero en el momento en el que el Storekeeper elige un supermercado al que comprarle, éste sube el precio en función de otro porcentaje aleatorio, y obtiene un beneficio en función del número de cervezas vendidas. Todo ésto genera una ‘pseudo-competencia’ entre los supermercados, haciendo que únicamente el más barato obtenga beneficios y venda parte de su stock.

Para generar los valores aleatorios usamos dos estrategias:
- Una para los valores que tenemos que seguir y actualizar como el precio y el stock. Estas se inician en un plan que se ejecuta al inicio en el plan `+!start`. De esta forma evitamos creencias de valores aleatorios generados automáticamente como en el segundo caso solapados con creencias introducidas más tarde.
```prolog
+!start : true <- .random(P);
           +price(beer, (P + 1) * 20);
           .send(robot, tell, price(beer,P));
   	 .random(M);
           +money((M + 100) * 1000);
   	 .random(S);
           +stock(beer, math.round((S + 1) * 5));
   	 +initial_price((P + 1) * 20);
           !make_discount.
```
- Los valores de los multiplicadores se generan siempre de forma aleatoria con una creencia condicional tipo Prolog.
```prolog
price_rise(S + 1.1) :- .random(S).
price_lower(0.999 - S * 0.1) :- .random(S).
```

## Ejemplo de 2 Owners

Hemos hecho una implementación de dos owners bastante simple pero funcional.
```prolog
+!bring(Owner, beer) : .intend(give(A,_)) &
    A \== Owner <- .wait(3000);
           !bring(Owner, beer).
+!bring(Owner, beer) : true <- !give(Owner, beer).
```
Consiste en que el robot va a entregar cervezas siguiendo las peticiones de los owners, y la va realizar si no está haciendo una entrega actualmente, de forma que se evitan los conflictos de intenciones. Llega a funcionar, ya que, entrega las cervezas que piden ambos owners pero no siempre lo hace de forma justa, pueden darse casos en los que entregue tres cervezas seguidas al mismo owner.

Una solución para nuestro problema, es implementar una cola, de forma que las peticiones se metan en la estructura y el agente las vaya realizando una por una. También deberíamos generalizar la creación y gestión de owners, para facilitar su creación y destrucción.


## Calidad de código
A medida que hacíamos el proyecto, el número de variables globales que teníamos en `HouseModel` aumentó de forma considerable. También sucede lo mismo al añadir las percepts necesarias para el correcto funcionamiento. Para solucionar estos problemas, nos planteamos distintas soluciones como crear clases para lugares y robots o usar una tabla hash que almacenará los elementos del modelo teniendo como clave un identificador.

Finalmente la solución que adoptamos fue usar tipos enumerados para lugares y robots. De esta forma tenemos tipos estáticos accesibles desde cualquier parte del código que van a tener siempre los valores actualizados y se instancian en su declaración. Aprovechamos la flexibilidad que le brinda el lenguaje permitiendo almacenar variables y tener constructores y métodos propios. Esto nos permitió usando los métodos `Places.values()` y `SpecializedRobots.values()` añadir “percepts” iterando sobre sus valores dejando un código mucho más limpio.
```java
enum SpecializedRobots {
	ROBOT(0),
	CLEANER(1),
	STOREKEEPER(2);

  private final int value;
  private SpecializedRobots(int v) {
    this.value = v;
  }
  public int getValue() {
    return value;
  }
}

enum Places {
	FRIDGE(new Location(0, 0), HouseModel.FRIDGE),
	OWNER(new Location(HouseModel.GSize - 1, HouseModel.GSize - 1), HouseModel.OWNER),
	OWNER_MUSK(new Location(HouseModel.GSize / 2, 0), HouseModel.OWNER_MUSK),
	BIN(new Location(HouseModel.GSize - 1, 0), HouseModel.BIN),
	DELIVERY(new Location(0, HouseModel.GSize - 1), HouseModel.DELIVERY),
	BASE_ROBOT(new Location(HouseModel.GSize / 2, HouseModel.GSize / 2), -1, 0),
	BASE_CLEANER(new Location(HouseModel.GSize / 2 - 1, HouseModel.GSize - 1), -1, 0),
	BASE_STOREKEEPER(new Location(HouseModel.GSize / 2 + 1, HouseModel.GSize - 1), -1, 0);

	public Location location;
	public int x;
	public int y;
	public final int gridConst;
	public final int minDist;

	private Places(Location loc) {
    	location = loc;
    	gridConst = -1;
    	x = loc.x;
    	y = loc.y;
    	minDist = 1;
	}

	private Places(Location loc, int gridConst) {
    	location = loc;
    	this.gridConst = gridConst;
    	x = loc.x;
    	y = loc.y;
    	minDist = 1;
	}

	private Places(Location loc, int gridConst, int minDist) {
    	location = loc;
    	this.gridConst = gridConst;
    	x = loc.x;
    	y = loc.y;
    	this.minDist = minDist;
	}

	public void setLocation(Location loc) {
    	location = loc;
    	x = loc.x;
    	y = loc.y;
	}

	public void setLocation(int x, int y) {
    	this.x = location.x = x;
    	this.y = location.y = y;
	}

}
```


