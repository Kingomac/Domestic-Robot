package movement;

import java.util.List;
import java.util.stream.IntStream;

import house.HouseModel;
import house.MobileAgents;
import java.util.LinkedList;
import java.util.Collections;
import jason.environment.grid.Location;

public class PathFinder {

  private class Celda {
    public int f, h, g;
    public int padre_i, padre_j;

    public Celda() {
      this.f = Integer.MAX_VALUE;
      this.h = Integer.MAX_VALUE;
      this.g = 100;
      this.padre_i = -1;
      this.padre_j = -1;
    }

    public Celda(int h) {
      this.f = Integer.MAX_VALUE;
      this.h = h;
      this.g = 0;
      this.padre_i = -1;
      this.padre_j = -1;
    }

    public Celda(int f, int h, int g, int padre_i, int padre_j) {
      this.f = f;
      this.h = h;
      this.g = g;
      this.padre_i = padre_i;
      this.padre_j = padre_j;
    }

    @Override
    public String toString() {
      return "f: " + f + ", h: " + h + ", g: " + g + ", padre_i:" + padre_i + ", padre_j: " + padre_j;
    }
  }

  private HouseModel model;

  public PathFinder(HouseModel model) {
    this.model = model;
  }

  public Location getNextPosition(Location origen, Location destino, MobileAgents me) {

    // Crear matriz con datos de cada celda, se inicializa con
    // la distancia Manhattan de cada celda al destino
    Celda[][] infoMatriz = IntStream.range(0, model.getHeight())
        .mapToObj(i -> IntStream.range(0, model.getWidth())
            .mapToObj(j -> new Celda(new Location(j, i).distanceManhattan(destino))).toArray(PathFinder.Celda[]::new))
        .toArray(PathFinder.Celda[][]::new);

    List<Location> listaAbierta = new LinkedList<>(); // lista de celdas posibles
    List<Location> listaCerrada = new LinkedList<>(); // lista de celdas ya visitadas
    // inicializar listas con el origen del robot
    listaAbierta.add(origen);
    infoMatriz[origen.y][origen.x] = new Celda(0, 0, 0, origen.y, origen.x);

    boolean caminoEncontrado = false;
    while (!listaAbierta.isEmpty() && !caminoEncontrado) {
      // obtener mejor casilla en la lista abierta (que no esté en la lista cerrada)
      Location q = getMinF(listaAbierta, listaCerrada, infoMatriz);
      // se mueve de la lista abierta a la cerrada
      listaAbierta.remove(q);
      listaCerrada.add(q);

      // bucle sobre las posiciones adyacentes disponibles (obstáculos descartados)
      for (Location next : getFreeAdjacentPositions(q, destino, me)) {
        if (next.equals(destino)) { // caso 1: la casilla es el destino -> fin
          caminoEncontrado = true;
          infoMatriz[destino.y][destino.x].padre_i = q.y;
          infoMatriz[destino.y][destino.x].padre_j = q.x;
          return getResultNextLocation(infoMatriz, origen, destino);
        }

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
      }
    }

    if (!caminoEncontrado)
      System.out.println("Error: " + me.name() + " no ha podido encontrar un camino");
    return null;
  }

  private Location getMinF(List<Location> listaAbierta, List<Location> listaCerrada, Celda[][] infoMatriz) {
    int minF = Integer.MAX_VALUE;
    Location res = null;
    for (Location i : listaAbierta) {
      if (!listaCerrada.contains(i) && infoMatriz[i.y][i.x].f < minF) {
        minF = infoMatriz[i.y][i.x].f;
        res = i;
      }
    }
    return res;
  }

  private static Location getResultNextLocation(Celda[][] infoMatriz, Location orig, Location dest) {
    List<Location> resultado = getResultPathReversed(infoMatriz, orig, dest);
    return resultado.get(resultado.size() - 1);
  }

  private static List<Location> getResultPath(Celda[][] infoMatriz, Location orig, Location dest) {
    List<Location> result = getResultPathReversed(infoMatriz, orig, dest);
    Collections.reverse(result);
    return result;
  }

  private static List<Location> getResultPathReversed(Celda[][] infoMatriz, Location orig, Location dest) {
    int i = dest.y;
    int j = dest.x;
    List<Location> resultado = new LinkedList<>();
    while (!(infoMatriz[i][j].padre_i == i && infoMatriz[i][j].padre_j == j)) {
      resultado.add(new Location(j, i));
      int newi = infoMatriz[i][j].padre_i;
      int newj = infoMatriz[i][j].padre_j;
      i = newi;
      j = newj;
    }

    return resultado;
  }

  /**
   * Devuelve las posiciones adyacentes a una casilla si están libres siguiendo un
   * movmiento Manhattan
   * 
   * @param pos
   * @return
   */
  private List<Location> getFreeAdjacentPositions(Location pos, Location destino, MobileAgents me) {
    List<Location> toret = new LinkedList<>();
    if (model.inGrid(pos.x + 1, pos.y)
        && (model.agentCanGo(me, pos.x + 1, pos.y) || destino.equals(new Location(pos.x + 1, pos.y))))
      toret.add(new Location(pos.x + 1, pos.y));
    if (model.inGrid(pos.x - 1, pos.y)
        && (model.agentCanGo(me, pos.x - 1, pos.y) || destino.equals(new Location(pos.x - 1, pos.y))))
      toret.add(new Location(pos.x - 1, pos.y));
    if (model.inGrid(pos.x, pos.y - 1)
        && (model.agentCanGo(me, pos.x, pos.y - 1) || destino.equals(new Location(pos.x, pos.y - 1))))
      toret.add(new Location(pos.x, pos.y - 1));
    if (model.inGrid(pos.x, pos.y + 1)
        && (model.agentCanGo(me, pos.x, pos.y + 1) || destino.equals(new Location(pos.x, pos.y + 1))))
      toret.add(new Location(pos.x, pos.y + 1));

    return toret;
  }

}
