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
  private boolean inicial = true;

  public PathFinder(HouseModel model) {
    this.model = model;
  }

  public Location getDirection(Location origen, Location destino, SpecializedRobots me) {

    /*
     * Esto lo comenté porque da problemas para ir a las bases
     * if (origen.distanceManhattan(destino) <= 1)
     * return origen;
     */

    Graph<Location, DefaultEdge> grafo = new SimpleGraph<>(DefaultEdge.class);

    for (int x = 0; x < model.getWidth(); x++) {
      for (int y = 0; y < model.getHeight(); y++) {
        if (model.isWall(x, y))
          continue;
        grafo.addVertex(new Location(x, y));
      }
    }

    /*
     * for (int i = 0; i < model.getHeight(); i++) {
     * for (int j = 0; j < model.getWidth(); j++) {
     * 
     * if (!model.isFree(i, j))
     * continue;
     * 
     * Location vertex1 = new Location(i, j);
     * 
     * // Agrega una arista hacia la celda a la derecha
     * if (j < model.getWidth() - 1) {
     * 
     * Location vertex2 = new Location(i, j + 1);
     * grafo.addEdge(vertex1, vertex2);
     * grafo.addEdge(vertex2, vertex1);
     * }
     * 
     * // Agrega una arista hacia la celda de abajo
     * if (i < model.getHeight() - 1) {
     * Location vertex2 = new Location(i + 1, j);
     * grafo.addEdge(vertex1, vertex2);
     * grafo.addEdge(vertex2, vertex1);
     * }
     * }
     * }
     */

    for (int i = 0; i < model.getHeight(); i++) {
      for (int j = 0; j < model.getWidth(); j++) {
        if (!model.isWall(i, j)) {
          Location node = new Location(i, j);
          grafo.addVertex(node);

          // add edges to adjacent free nodes
          if (i > 0) {
            Location neighbor = new Location(i - 1, j);
            if (!model.isWall(neighbor))
              grafo.addEdge(node, neighbor);
          }
          if (i < model.getHeight() - 1) {
            Location neighbor = new Location(i + 1, j);
            if (!model.isWall(neighbor))
              grafo.addEdge(node, neighbor);
          }
          if (j > 0) {
            Location neighbor = new Location(i, j - 1);
            if (!model.isWall(neighbor))
              grafo.addEdge(node, neighbor);
          }
          if (j < model.getWidth() - 1) {
            Location neighbor = new Location(i, j + 1);
            if (!model.isWall(neighbor))
              grafo.addEdge(node, neighbor);
          }

        }
      }
    }
    if (inicial) {
      grafo.edgeSet().forEach(i -> {
        Location origen2 = grafo.getEdgeSource(i);
        Location destino2 = grafo.getEdgeTarget(i);
        System.out.println(origen2.x + "," + origen2.y + " " + destino2.x + "," +
            destino2.y);
      });
      inicial = false;
    }

    /*
     * grafo.vertexSet().forEach(i -> {
     * System.out.println(i);
     * });
     * 
     */

    /*
     * System.out.println("Origen: " + origen.x + ", " + origen.y);
     * System.out.println("Destino: " + destino);
     * if (grafo.containsEdge(origen, destino)) {
     * System.out.println("VIVA");
     * } else {
     * System.out.println("NO VIVA");
     * }
     * 
     * if (model.isFree(0, 0)) {
     * System.out.println("IS FREE");
     * } else {
     * System.out.println("NO IS FREE");
     * }
     */

    GraphPath<Location, DefaultEdge> path = DijkstraShortestPath.findPathBetween(grafo, origen,
        destino);

    System.out.println("Siguiente: " + path.getVertexList().get(1));
    Location next = path.getVertexList().get(1);
    System.out.println("PATH (" + path.getVertexList().size() + "):");
    path.getVertexList().forEach(i -> {
      System.out.println(i);
    });

    return path.getVertexList().get(1);
  }

}
