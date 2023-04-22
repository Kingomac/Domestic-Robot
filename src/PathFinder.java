import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import jason.environment.grid.Location;

public class PathFinder {

  private HouseModel model;

  public PathFinder(HouseModel model) {
    this.model = model;
  }

  public Location getDirection(Location origen, Location destino) {

    if (origen.distanceManhattan(destino) <= 1)
      return origen;

    Graph<Location, DefaultEdge> grafo = new SimpleGraph<>(DefaultEdge.class);

    for (int x = 0; x < model.getWidth(); x++) {
      for (int y = 0; y < model.getHeight(); y++) {
        grafo.addVertex(new Location(x, y));
      }
    }

    for (int i = 0; i < model.getHeight(); i++) {
      for (int j = 0; j < model.getWidth(); j++) {

        if (!model.isFree(i, j))
          continue;

        Location vertex1 = new Location(i, j);

        // Agrega una arista hacia la celda a la derecha
        if (j < model.getWidth() - 1) {

          Location vertex2 = new Location(i, j + 1);
          grafo.addEdge(vertex1, vertex2);
          grafo.addEdge(vertex2, vertex1);
        }

        // Agrega una arista hacia la celda de abajo
        if (i < model.getHeight() - 1) {
          Location vertex2 = new Location(i + 1, j);
          grafo.addEdge(vertex1, vertex2);
          grafo.addEdge(vertex2, vertex1);
        }
      }
    }

    /*
     * grafo.edgeSet().forEach(i -> {
     * Location origen = grafo.getEdgeSource(i);
     * Location destino = grafo.getEdgeTarget(i);
     * System.out.println(origen.x + "," + origen.y + " " + destino.x + "," +
     * destino.y);
     * });
     */

    /*
     * grafo.vertexSet().forEach(i -> {
     * System.out.println(i);
     * });
     * 
     */

    System.out.println("Origen: " + origen.x + ", " + origen.y);
    System.out.println("Destino: " + destino);
    if (grafo.containsEdge(origen, destino)) {
      System.out.println("VIVA");
    } else {
      System.out.println("NO VIVA");
    }

    if (model.isFree(0, 0)) {
      System.out.println("IS FREE");
    } else {
      System.out.println("NO IS FREE");
    }

    GraphPath<Location, DefaultEdge> path = DijkstraShortestPath.findPathBetween(grafo, origen,
        destino);
    /*
     * System.out.println("PATH");
     * path.getVertexList().forEach(i -> {
     * System.out.println(i);
     * });
     */

    System.out.println("Siguiente: " + path.getVertexList().get(1));
    Location next = path.getVertexList().get(1);
    if (model.isPlace(next.x, next.y) || path.getVertexList().isEmpty()) {
      return origen;
    }
    return path.getVertexList().get(1);
  }

}
