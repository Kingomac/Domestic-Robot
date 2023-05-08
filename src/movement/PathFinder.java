package movement;

import java.util.List;
import house.HouseModel;
import house.SpecializedRobots;
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

  public Location getNextPosition(Location origen, Location destino, SpecializedRobots me) {

    Celda[][] infoMatriz = new Celda[model.getHeight()][model.getWidth()];
    for (int i = 0; i < infoMatriz.length; i++) {
      for (int j = 0; j < infoMatriz[i].length; j++) {
        infoMatriz[i][j] = new Celda();
        infoMatriz[i][j].h = new Location(j, i).distanceManhattan(destino);
        if (!model.isFreeOfObstacle(i, j)) {
          infoMatriz[i][j].g = Integer.MAX_VALUE;
        }
      }
    }

    // System.out.println("----- infoMatriz");
    // for (Celda[] i : infoMatriz) {
    // System.out.println(Arrays.toString(i));
    // }

    List<Location> listaAbierta = new LinkedList<>();
    List<Location> listaCerrada = new LinkedList<>();
    listaAbierta.add(origen);
    infoMatriz[origen.y][origen.x] = new Celda(0, 0, 0, origen.y, origen.x);

    boolean caminoEncontrado = false;
    while (!listaAbierta.isEmpty() && !caminoEncontrado) {
      // System.out.println("Lista abierta: " + listaAbierta.toString());
      // System.out.println("Lista cerrada: " + listaCerrada.toString());
      Location q = getMinF(listaAbierta, listaCerrada, infoMatriz);
      listaAbierta.remove(q);
      listaCerrada.add(q);

      // System.out.println("Lista abierta: " + listaAbierta.toString());
      // System.out.println("Lista cerrada: " + listaCerrada.toString());

      for (Location next : getFreeAdjacentPositions(q)) {
        // System.out.println("-- Checking next location: " + next);
        if (next.equals(destino)) {
          // System.out.println("Lista abierta: " + listaAbierta.toString());
          // System.out.println("Lista cerrada: " + listaCerrada.toString());
          // System.out.println("Camino encontrado: " + next);
          caminoEncontrado = true;
          infoMatriz[destino.y][destino.x].padre_i = q.y;
          infoMatriz[destino.y][destino.x].padre_j = q.x;
          List<Location> resultado = getResult(infoMatriz, origen, destino);
          // System.out.println("RESULTADO");
          // System.out.println(resultado.toString());
          return resultado.get(0);
          ///////////////////////////////////// return getResultado(infoMatriz, destino);
          // return resultado.get(resultado.size() - 1);
        }

        else if (!listaCerrada.contains(next)) {
          int gNew = infoMatriz[next.y][next.x].g + 1;
          int fNew = infoMatriz[next.y][next.x].h + gNew;
          if (!listaAbierta.contains(next) || infoMatriz[next.y][next.x].f > fNew) {
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
      System.out.println("Error encontrando camino");
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

  List<Location> getResult(Celda[][] infoMatriz, Location orig, Location dest) {
    int i = dest.y;
    int j = dest.x;
    // System.out.println("aaaaaaaaaaaaa");
    List<Location> resultado = new LinkedList<>();
    while (!(infoMatriz[i][j].padre_i == i && infoMatriz[i][j].padre_j == j)) {
      // System.out.println("infomatriz[" + i + "," + j + "]: " + infoMatriz[i][j]);
      resultado.add(new Location(j, i));
      int newi = infoMatriz[i][j].padre_i;
      int newj = infoMatriz[i][j].padre_j;
      // System.out.format("[%d,%d] -> [%d,%d], ", i, j, newi, newj);
      i = newi;
      j = newj;
    }

    Collections.reverse(resultado);
    return resultado;
  }

  /**
   * Devuelve las posiciones adyacentes a una casilla si están libres siguiendo un
   * movmiento Manhattan
   * 
   * @param pos
   * @return
   */
  private List<Location> getFreeAdjacentPositions(Location pos) {
    List<Location> toret = new LinkedList<>();
    if (model.inGrid(pos.x + 1, pos.y) && model.isFreeOfObstacle(pos.x + 1, pos.y))
      toret.add(new Location(pos.x + 1, pos.y));
    if (model.inGrid(pos.x - 1, pos.y) && model.isFreeOfObstacle(pos.x - 1, pos.y))
      toret.add(new Location(pos.x - 1, pos.y));
    if (model.inGrid(pos.x, pos.y - 1) && model.isFreeOfObstacle(pos.x, pos.y - 1))
      toret.add(new Location(pos.x, pos.y - 1));
    if (model.inGrid(pos.x, pos.y + 1) && model.isFreeOfObstacle(pos.x, pos.y + 1))
      toret.add(new Location(pos.x, pos.y + 1));

    return toret;
  }

}
