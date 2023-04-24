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

  public Location getDirection(Location origen, Location destino, SpecializedRobots me) {

    Graph<Location, DefaultEdge> grafo = new SimpleGraph<>(DefaultEdge.class);

    for (int x = 0; x < model.getWidth(); x++) {
      for (int y = 0; y < model.getHeight(); y++) {
        if (model.isWall(x, y))
          continue;
        grafo.addVertex(new Location(x, y));
      }
    }

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

    GraphPath<Location, DefaultEdge> path = DijkstraShortestPath.findPathBetween(grafo, origen,
        destino);

    return path.getVertexList().get(1);
  }

}
