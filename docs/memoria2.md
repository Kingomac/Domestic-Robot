# Memoria Robot Doméstico - Entrega final

> Hecho por:
> 
> Javier Veloso Boubeta
> 
> Cristian Sousa Vidal
> 
> Mario Vila Comesaña

- [Memoria Robot Doméstico - Entrega final](#memoria-robot-doméstico---entrega-final)
  - [Elementos participantes del proyecto](#elementos-participantes-del-proyecto)
    - [Zonas](#zonas)
    - [Agentes](#agentes)
      - [RobotMayordomo](#robotmayordomo)
      - [Cleaner](#cleaner)
      - [Burner](#burner)
      - [Storekeeper](#storekeeper)
  - [Movimiento de Agentes](#movimiento-de-agentes)
    - [Funciones de coste y heurística](#funciones-de-coste-y-heurística)
    - [¿Por qué A\*?](#por-qué-a)
    - [Uso de las directivas *up*,*down*,*left*,*right*](#uso-de-las-directivas-updownleftright)
    - [Creencias usadas](#creencias-usadas)
    - [Código](#código)
  - [Cas](#cas)


## Elementos participantes del proyecto
  ### Zonas
- *Papelera*: Almacena toda la basura generada por un Owner. Esta cuenta con una capacidad de 5 latas, y una vez que se llene, se vaciará gracias al robot Burner.
- *Delivery*: En esta zona, el StoreKeeper decide a qué supermercado comprarle los productos. Para decidir en que supermercado se hace la compra, el robot StoreKeeper tiene en cuenta cual es la marca favorita, cual es el precio más barato y si hay suficiente stock de ese producto. Si no hubiese suficiente cantidad, comprará las unidades restantes en otro Supermecado. Una vez recibidos los productos, el robot los recoge y los deposita en la Fridge.
- *Nevera*: En ésta zona, es donde se guardan tanto las cervezas, como los pinchos y las tapas de las que salen los pinchos, aquí, es donde el MyRobot viene a por las cervezas y pinchos para lleva al Owner y a hacer a partir de las tapas, los pinchos correspondientes, ademá, es aquí donde el storekeeper guarda las cervezas que compra en la zona de deliver
- *Lavavajillas*: Cada vez que el Owner come un pincho, ensucia un plato, así que MyRobot va a por ese plato y lo guarda en el lavavajillas, que, una vez se llene, en nuestro caso con 6 platos, el lavavajillas comenzará a funcionar, y una vez termine, se supone que el lavavajillas habrá terminado.
- *Alacena*: Agente encargado de almacenar una cantidad de platos limpios, necesarios para la fabricación de los pinchos, éstos platos son los que MyRobot saca del lavavajillas una vez éste termine. 
### Agentes
- *Robot especializados*: el proyecto cuenta con una serie de robots los cuales están  diseñados y programados para realizar unas determinadas tareas en función del estado y características del entorno. Dentro de este grupo encontramos:
#### RobotMayordomo
Robot encargado de recibir la solicitud de cerveza del owner, ir a buscarla a la nevera junto con un pincho y llevársela. También es el encargado de recoger los platos sucios del owner para meterlos en el lavavajillas, y, una vez estén listos, guardarlos en la alacena, y es responsable también de cocinar los pinchos a partir de una tapa y un plato limpio.
Este robot es el más complejo en su codificación, pues cuenta con una gran multitud de planes para realizar todas las tareas:
1. En este plan, el robot mayordomo solicita al dueño una cantidad de dinero. Posteriormente se actualiza el saldo de la cartera del robot.
```prolog
!request_money.
+!request_money <- .send(owner, achieve, ask_money(robot)).
+!save_money(Nuevo) <- ?money(Actual); .print("Dinero actual: ", Actual, " que sumado da ", Actual + Nuevo); -+money(Actual + Nuevo).
```

```prolog
!myrobot.
+!myrobot: plate(dirty)[source(Owner)] <- 
	!go_to(robot,Owner);
	-plate(dirty)[source(Owner)];
	//.wait({ -plate(dirty) });
	take(plate,Owner);
	!go_to(robot, dishwasher);
	put(dish,dishwasher);
	.wait(plate(dishwasher,_));
	?plate(dishwasher, X);
	if(X >= 6) { dishwasher(on) };
	.wait(500);
	!myrobot.
+!myrobot: bring(beer)[source(Owner)] <- !get_dish_for_pincho; !give(Owner,beer); !myrobot.
+!myrobot: dishwasher(finish) <- !save_plates; !myrobot.
+!myrobot: dishwasher(on) <- !go_to(robot, base_robot); .wait(500); !myrobot.
+!myrobot: true <- !go_to(robot, base_robot); .wait(500); !myrobot.
```

#### Cleaner 
Encargado de recoger los botellines vacíos desperdigados por el entorno que tira el Owner tras beber una cerveza. Éste robot lleva éstos botellines a la papelera.  
Para llevar a cabo está acción se dispone del plan !clean_trash:
```prolog
!clean_trash.
+!clean_trash: where(trash, _, _) <- !go_to(cleaner, trash); take(trash); !go_to(cleaner, bin); drop(trash); !clean_trash. 
+!clean_trash: true <- !go_to(cleaner, base_cleaner); .wait(where(trash,_,_)); !clean_trash.
```
Para este plan se tienen dos posibilidades:
1. Si el robot tiene la creencia de que hay basura, independientemente de donde se ubique, este, irá hasta esa localización y recogerá los deshechos para posteriormente depositarlos en la papelera.
2. Una vez realizado lo anterior, el robot vuelve a su posición de descanso, donde esperará hasta que se vuelva a generar basura.

#### Burner
Una vez la papelera llega a su máxima capacidad, este agente se encarga de recoger toda la basura e incinerarla, vaciando la papelera en el proceso, una vez termine, volverá a su posición de descanso, hasta que la papelera se vuelva a llenar.
Para que puede realizar estas tareas se utiliza el plan !take_out_trash:
```prolog
!take_out_trash.
+!take_out_trash: bin(full) <- !go_to(burner, bin); empty(bin); .wait(5000); drop(bin); !take_out_trash.
+!take_out_trash: true <- !go_to(burner, base_burner); .wait({ +bin(full) }); !take_out_trash.
```
Como se puede observar en el código superior, este plan cuenta con dos casos:
1. Si tiene la creencia de que la papelera está llena, se dirigirá hacia esta localización para vaciarla y quemar toda la basura.
2. Una terminado el proceso de vaciado, el robot Burner volverá a su posición de descanso donde esperará hasta que la papelera se vuelva a llenar.

#### Storekeeper
Robot encargado de comprar cervezas en el supermercado, y guardarlas en la nevera. Para comprar las cervezas, dictamina el supermercado más barato en el que haya cervezas a momento de la compra, y compra las cervezas, además compra cervezas una vez la cantidad de cervezas almacenadas en la nevera es inferior a 3, haciendo imposible que la nevera se quede vacía.

- *Owner*: solicita cervezas para consumirlas y posteriormente tira al escenario las latas de cerveza. Estas latas se tiran al entorno de forma aleatoria. Este agente cuenta con una cantidad de dinero de partida, de la cual la mitad es suministrada al robot para que éste realice la compra de cervezas.
- *Supermercado*: Una serie de agentes, que contienen una serie de artículos necesarios en nuestro sistema como las cervezas o las tapas, necesarias para cocinar los pinchos. Cada supermercado contiene un precio distinto para cada artículo, y se distinguen por su nombre. Los supermercados competirán por tener el precio mas bajo para vender artículos al storekeeper. Ésta forma de competición está implementada con una serie de porcentajes, que decrecen el precio de los productos con el tiempo, o lo aumentan cuando se realiza una venta al storekeeper, cabe destacar que el porcentaje de aumento y decrecimiento es distinto para cada supermercado.

## Movimiento de Agentes
Inicializar listas y añadir localización inicial a lista abier
El movimiento se realiza usando el algoritmo A*. En vez de grafos, usamos una matriz que contiene la información necesaria para el algoritmo.
```java
private class Celda {
  public int f, h, g;
  public int padre_i, padre_j;
  // ...
}
```

<span>1. Generar la matriz de información del grid (se calcula el parámetro $h$).</span>
```java
Celda[][] infoMatriz = IntStream.range(0, model.getHeight())
    .mapToObj(i -> IntStream.range(0, model.getWidth())
        .mapToObj(j -> new Celda(new Location(j, i).distanceManhattan(destino))).toArray(PathFinder.Celda[]::new))
    .toArray(PathFinder.Celda[][]::new);
```
<span>2. Inicializar listas y añadir localización inicial a lista abierta.</span>
```java
List<Location> listaAbierta = new LinkedList<>(); // lista de celdas  posibles
List<Location> listaCerrada = new LinkedList<>(); // lista de celdas ya visitadas
// inicializar listas con el origen del robot
listaAbierta.add(origen);
infoMatriz[origen.y][origen.x] = new Celda(0, 0, 0, origen.y, origen.x);
boolean caminoEncontrado = false;
```
<span>3. Bucle principal, se recorre la lista abierta mientras no esté vacía</span>
```java
while (!listaAbierta.isEmpty() && !caminoEncontrado) {
```
<span>3.1. Se obtiene la celda con el menor $f$ en la lista abierta</span>
<span>3.2. Se mueve de la lista abierta a la cerrada</span>
```java
// obtener mejor casilla en la lista abierta (que no esté en la lista cerrada)
Location q = getMinF(listaAbierta, listaCerrada, infoMatriz);
// se mueve de la lista abierta a la cerrada
listaAbierta.remove(q);
listaCerrada.add(q);
```
<span>3.2.1. Se itera sobre las posiciones adyacentes libres</span>
```java
// bucle sobre las posiciones adyacentes disponibles (obstáculos descartados)
for (Location next : getFreeAdjacentPositions(q, destino, me)) {
```
<span>3.2.2 Si esa celda es el destino fin del algoritmo</span>
```java
if (next.equals(destino)) { // caso 1: la casilla es el destino -> fin
  caminoEncontrado = true;
  infoMatriz[destino.y][destino.x].padre_i = q.y;
  infoMatriz[destino.y][destino.x].padre_j = q.x;
  return getResultNextLocation(infoMatriz, origen, destino);
}
```
<span>3.2.3. Si esa celda no está en la lista cerrada se actualizan los parámetros y si no está en la lista abierta (no es considerado como una posibilidad) o $f_nuevo < f_anterior$ se añade como posibilidad</span>
```java
else if (!listaCerrada.contains(next)) { // caso 2: no está en la lista cerrada, se sigue
  // calcular nuevos parámetros para la celda
  int gNew = infoMatriz[next.y][next.x].g + 1;
  int fNew = infoMatriz[next.y][next.x].h + gNew;
  // si no está en la lista abierta o su nuevo f (coste) es menor que el anterior
  if (!listaAbierta.contains(next) || infoMatriz[next.y][next.x].f > fNew) {
    // se añade como posibilidad en la lista abierta y se actualizan sus parámetros
    listaAbierta.add(next);
    infoMatriz[next.y][next.x].g = gNew;
    infoMatriz[next.y][next.x].f = fNew;
    infoMatriz[next.y][next.x].padre_i = q.y;
    infoMatriz[next.y][next.x].padre_j = q.x;
  }
}
```


### Funciones de coste y heurística

$$
f = h + g
$$
$$
h(loc_1, loc_2) = distanciaManhattan(loc_1, loc_2)
$$

El coste $g$ empieza con valor $g=0$ y este se incrementa cada vez que se añade esa celda a la lista cerrada.

### ¿Por qué A*?

Probamos diferentes implementaciones del movimiento, empezando por reglas simples de movimiento inmediato en Jason, Dijkstra usando un grafo que represente el grid y la versión final el algoritmo A*. Finalmente elegimos este algoritmo por su menor complejidad algoritmica, para ilustrarlo vamos a comparar sus complejidades en el peor y en el mejor de los casos para ambos algoritmos, además de la complejidad de construcción del grafo en el caso de Dijkstra y de la matriz en caso de nuestra implementación de A*.

- $V$: número de vértices.
- $E$: número de aristas.

|                             | Dijkstra                                                                    | A*                                  |
| --------------------------- | --------------------------------------------------------------------------- | ----------------------------------- |
| Memoria                     | Grafo con JGraphT $V=11*11=121$, $E=10*10*4+11*4=144$, en total 265 objetos | Matriz de $11*11=121$ objetos Celda |
| Complejidad en el peor caso | $O(E+V+Vlog(V))$                                                            | $O(E)$                              |



### Uso de las directivas *up*,*down*,*left*,*right*

Nuestros agentes móviles usan estas directivas, que usan para llamar a la función `move_robot(Agente,Dirección)` que moverá al agente en la dirección indicada. Esta dirección se obtiene de una función interna de Jason que hemos implementado y se llama con `movement.NextDirection(OrigenX,OrigenY,DestX,DestY,Tipo,Resultado)`, que aplica llama al `Pathfinder` y aplica el algoritmo para unificar dando como resultado la dirección en la que se tiene que mover el agente.

Para hacer una acción interna de Jason Agentspeak tuvimos que reestructurar la parte Java del proyecto para que esté en paquetes distintos del default, de forma que podamos invocarla desde el código Jason. Entonces, dividimos el código Java en dos paquetes, que son las carpetas `house` y `movement`

### Creencias usadas

Para que sea posible usamos los siguientes *percepts*:
- `where(Sitio,X,Y)`: define la posición de los lugares (Places) en el grid.
- `at(Sitio,Agente)`: determina si un agente está en un lugar (Places).
- `at(Agente,X,Y)`: define la posición de un agente en el grid.

### Código

Este es el plan usado `!go_to(Agente,Sitio)`:

```prolog
+!go_to(Tipo, Sitio) : not where(Sitio, X, Y) <- .print("El sitio ", Sitio, " no existe"); .wait(where(Sitio, _, _)); !go_to(Tipo, Sitio).
+!go_to(Tipo, Sitio) : at(Tipo, Sitio) <- true.
+!go_to(Tipo, Sitio) : not at(Tipo, Sitio) & where(Sitio, DestX,DestY) & at(Tipo, OrigenX, OrigenY) <- movement.NextDirection(OrigenX,OrigenY,DestX,DestY,Tipo,AA); move_agent(Tipo, AA); !go_to(Tipo, Sitio).
-!go_to(Tipo, Sitio) <- .print(Tipo, " can't !go_to ", Sitio); .wait(3000); !go_to(Tipo, Sitio).
```

Casos:
1. No se puede determinar la posición de la casilla a la que se quiere ir, entonces, se espera a que tenga la creencia y se llama de nuevo.
2. Se encuentra en el sitio, entonces, termina devolviendo `true`.
3. No está en el destino, entonces, se calcula la dirección del siguiente movimiento, se mueve el agente una celda en la dirección al destino y se llama de nuevo.
4. Es el caso de error, se produce cuando no se encuentra un camino, lo que probablemente sea que hay agentes bloqueando el camino, por tanto, espera 3 segundos y lo vuelve a intentar.



## Cas