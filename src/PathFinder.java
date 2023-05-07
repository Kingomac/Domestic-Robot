import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Arrays;

import jason.environment.grid.Location;

public class PathFinder {

  public final int INT_MAX = Integer.MAX_VALUE;

  public class Celda {
    public int f, h, g;
    public int padre_i, padre_j;

    public Celda() {
      this.f = INT_MAX;
      this.h = INT_MAX;
      this.g = INT_MAX;
      this.padre_i = -1;
      this.padre_j = -1;
    }

    public Celda(int f, int h, int g, int padre_i, int padre_j) {
      this.f = f;
      this.h = g;
      this.g = h;
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

  public Location getDirection(Location origen, Location destino, SpecializedRobots me) {
    System.out.println("----- getDirection");
    int[][] matrizAdyacencia = new int[HouseModel.GSize][HouseModel.GSize];

    for (int i = 0; i < model.getHeight(); i++) {
      for (int j = 0; j < model.getWidth(); j++) {
        matrizAdyacencia[i][j] = model.isFreeOfObstacle(j, i) ? 0 : 1;
      }
    }

    System.out.println("----- Matriz de adyacencia");
    for (int[] i : matrizAdyacencia) {
      System.out.println(Arrays.toString(i));
    }

    Celda[][] infoMatriz = new Celda[HouseModel.GSize][HouseModel.GSize];
    for (int i = 0; i < infoMatriz.length; i++) {
      for (int j = 0; j < infoMatriz.length; j++) {
        infoMatriz[i][j] = new Celda();
        infoMatriz[i][j].h = new Location(j, i).distanceManhattan(destino);
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
    // f = h + g --> f = distanciaManhattan(nodo,destino) + g(si hay camino 0
    // INT_MAX)

    boolean caminoEncontrado = false;
    while (!listaAbierta.isEmpty()) {
      System.out.println("Lista abierta: " + listaAbierta.toString());
      System.out.println("Lista cerrada: " + listaCerrada.toString());
      Location q = getMinF(listaAbierta, infoMatriz);
      listaAbierta.remove(q);
      listaCerrada.add(q);

      System.out.println("Lista abierta: " + listaAbierta.toString());
      System.out.println("Lista cerrada: " + listaCerrada.toString());

      for (Location next : getNextLocations(q)) {
        System.out.println("-- Checking next location: " + next);
        if (next.equals(destino)) {
          System.out.println("Lista abierta: " + listaAbierta.toString());
          System.out.println("Lista cerrada: " + listaCerrada.toString());
          System.out.println("Camino encontrado: " + next);
          caminoEncontrado = true;
          infoMatriz[destino.y][destino.x].padre_i = q.x;
          infoMatriz[destino.y][destino.x].padre_j = q.y;
          List<Location> resultado = getResultado(infoMatriz, destino);
          System.out.println("RESULTADOOOOOOOOOOOOOOOOOO");
          System.out.println(resultado.toString());
          return resultado.get(0);
          ///////////////////////////////////// return getResultado(infoMatriz, destino);
        }

        else if (!listaCerrada.contains(next)) {
          int gNew = infoMatriz[next.y][next.x].h;
          infoMatriz[next.y][next.x].f = gNew + infoMatriz[q.y][q.x].h;
          listaAbierta.add(next);
        }
      }
    }

    if (!caminoEncontrado)
      System.out.println("Error encontrando camino");
    return null;
  }

  Location getMinF(List<Location> listaAbierta, Celda[][] infoMatriz) {
    int minF = INT_MAX;
    Location res = null;
    for (Location i : listaAbierta) {
      if (infoMatriz[i.y][i.x].f < minF) {
        minF = infoMatriz[i.y][i.x].f;
        res = i;
      }
    }
    return res;
  }

  List<Location> getResultado(Celda[][] infoMatriz, Location dest) {
    int i = dest.y;
    int j = dest.x;
    System.out.println("----- infoMatriz");
    for (Celda[] k : infoMatriz) {
      System.out.println(Arrays.toString(k));
    }
    List<Location> resultado = new LinkedList<>();
    while (!(infoMatriz[i][j].padre_i == i && infoMatriz[i][j].padre_j == j)) {
      resultado.add(new Location(i, j));
      int newi = infoMatriz[i][j].padre_i;
      int newj = infoMatriz[i][j].padre_j;
      i = newi;
      j = newj;
    }

    return resultado;
  }

  List<Location> getNextLocations(Location pos) {
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
