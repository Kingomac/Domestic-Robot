# Ejercicio 2 (Semana 5)

Hasta el momento suponemos que el propietario (agente owner) se encarga, de alguna manera, de reciclar las latas vacías de cerveza, o que no le preocupa que se acumulen sin fin en la posición del owner (aunque esto no sea visible)

Modifica el entorno de manera que exista un cubo de reciclaje (bin) donde el robot deposite las latas vacías, antes de servir al owner una nueva cerveza (para inspirarte en como localizar y tirar la lata, puedes utilizar el código de la aspiradora -cleaning-robots-). 

En este nuevo escenario, en el que hay que mantener limpia la casa, considera las siguientes dos situaciones posibles:

- El agente robot se desplaza hasta el lugar donde se encuentra el agente owner, para recoger la cerveza y tirarla en el cubo de reciclaje, antes de ir al frigorífico a por una nueva cerveza (si aun no se ha superado el límite diario).
- El agente owner implementa un propietario no muy considerado (tipo Homer Simpson) que tira la lata al suelo (en una posición aleatoria del entorno), antes de pedir una nueva cerveza; en este caso, el agente robot debe localizar la cerveza, recogerla y tirarla antes de servir una nueva cerveza.

1. Primero añadimos el objeto en el modelo. `HouseModel.java`
```java
public static final int BIN = 64;
//...
Location lBin = new Location(GSize-1,0);
//...
public HouseModel() {
	//...
	add(BIN, lBin);
	//...
}

```
2. Después añadimos que se dibuje en el grid. `HouseView.java`.
```java
public void draw(Graphics g, int x, int y, int object) {
	//...
	case HouseModel.BIN:
		super.drawAgent(g, x, y, new Color(139,69,19), -1);
		g.setColor(Color.white);
		drawString(g,x,y,defaultFont, "Bin");
		break;
   //...
}
```
3. Creamos cervezas que van a aparecer de forma aleatoria por el grid cuando el owner las tire. `HouseModel.java`
```java
public static final int BEER = 128;
//...
boolean carryingTrash = false;
//...
Location lBeer = new Location(-1,-1);

boolean dropBeer(){
	int posX = (int) Math.round(Math.random() * (GSize-1));
	int posY = (int) Math.round(Math.random() * (GSize-1));
	
	lBeer.x = posX;
	lBeer.y = posY;
	add(BEER, posX, posY);
		
	System.out.println("dropped beer: " + posX + ", "  + posY);
	
	return true;
}
```
4. Hicimos métodos para que el robot las recoja y las tire en la basura. `HouseModel.java`
```java
boolean takeTrash() {
	Location lRobot = getAgPos(0);
	if(!lRobot.equals(lBeer)) return false;
	carryingTrash = true;
	remove(BEER, lBeer);
	lBeer.x = -1;
	lBeer.y = -1;
	return true;
}
	
boolean dropTrash() {
	Location lRobot = getAgPos(0);
	if(!lRobot.equals(lBin)) return false;
	carryingTrash = false;
	return true;
}
```
5. Añadimos literales en el entorno para gestionar las nuevas acciones del robot y para que detecte cuándo está en la basura o encima de una cerveza tirada. `HouseEnv.java`.
```java
public static final Literal db  = Literal.parseLiteral("drop(beer)");
public static final Literal takeTrash = Literal.parseLiteral("take(trash)");
public static final Literal dropTrash = Literal.parseLiteral("drop(trash)");
public static final Literal atTrash = Literal.parseLiteral("at(robot,trash)");
public static final Literal atBin = Literal.parseLiteral("at(robot,bin)");
//...
void updatePercepts() {
  //..
	if(lRobot.equals(model.lBeer)) {
		addPercept("robot", atTrash);	
	}
	
	if(lRobot.equals(model.lBin)) {
		addPercept("robot", atBin);
	}
		
	if(model.lBeer.x != -1 && model.lBeer.y != -1) {
		addPercept("robot", Literal.parseLiteral("trash(hay)"));
	}
		
}

```
`lBeer` sería la posición de la cerveza tirada, que el entorno en caso de que sea distinta de (-1,-1) añadirá como percepción al robot para que la recoja.

`HouseEnv.java`
```java
public boolean executeAction(String ag, Structure action) {
  //...
  else if (action.getFunctor().equals("move_towards")) {
    String l = action.getTerm(0).toString();
    Location dest = null;
    if (l.equals("fridge")) {
      dest = model.lFridge;
    } else if (l.equals("owner")) {
      dest = model.lOwner;
    } else if (l.equals("trash")) {
      dest = model.lBeer;
    } else if(l.equals("bin")) {
      dest = model.lBin;
    }
		//...
  } else if(action.equals(db)) {
    result = model.dropBeer();
  } else if(action.equals(takeTrash)) {
    result = model.takeTrash();
  } else if(action.equals(dropTrash)) {
    result = model.dropTrash();
  }
  //...
}

```

6. Por último actualizamos los planes del robot, para que antes de llevarle una cerveza al owner compruebe si hay basura en el entorno.
```
+!bring(owner,beer) : trash(hay) <- !go_at(robot, trash); take(trash); !go_at(robot,bin); drop(trash); -trash(hay); !bring(owner,beer).
```

