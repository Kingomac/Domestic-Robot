package house;

import jason.environment.grid.Location;

/*
 * Identificación de los lugares fijos del grid
 */
enum Places {
  FRIDGE(new Location(0, 0), HouseModel.FRIDGE),
  OWNER(new Location(HouseModel.GSize - 1, HouseModel.GSize - 1)),
  BIN(new Location(HouseModel.GSize - 1, 0), HouseModel.BIN),
  DELIVERY(new Location(0, HouseModel.GSize - 1), HouseModel.DELIVERY),
  DISHWASHER(new Location(2, 0), HouseModel.DISHWASHER),
  CUPBOARD(new Location(4, 0), HouseModel.CUPBOARD),
  BASE_ROBOT(new Location(HouseModel.GSize / 2, HouseModel.GSize / 2), -1, 0, true),
  BASE_CLEANER(new Location(HouseModel.GSize / 2 - 1, HouseModel.GSize - 1), -1, 0, true),
  BASE_STOREKEEPER(new Location(HouseModel.GSize / 2 + 1, HouseModel.GSize - 1), -1, 0, true),
  BASE_BURNER(new Location(HouseModel.GSize / 2, HouseModel.GSize - 1), -1, 0, true);

  public Location location;
  public Location robotLoc;
  public int x;
  public int y;
  public final int gridConst;
  public final int minDist;
  public final boolean canGoThrough;

  private Places(Location loc) {
    location = loc;
    gridConst = -1;
    x = loc.x;
    y = loc.y;
    minDist = 1;
    canGoThrough = false;
  }

  private Places(Location loc, int gridConst) {
    location = loc;
    this.gridConst = gridConst;
    x = loc.x;
    y = loc.y;
    minDist = 1;
    canGoThrough = false;
  }

  private Places(Location loc, int gridConst, int minDist) {
    location = loc;
    this.gridConst = gridConst;
    x = loc.x;
    y = loc.y;
    this.minDist = minDist;
    canGoThrough = false;
  }

  private Places(Location loc, int gridConst, int minDist, boolean canGoThrough) {
    location = loc;
    this.gridConst = gridConst;
    x = loc.x;
    y = loc.y;
    this.minDist = minDist;
    this.canGoThrough = canGoThrough;
  }

  /**
   * Cambia la localización actualizando los parámetros x, y y location
   * 
   * @param loc nueva localización
   */
  public void setLocation(Location loc) {
    location = loc;
    x = loc.x;
    y = loc.y;
  }

  /**
   * Cambia la localización actualizando los parámetros x, y y location
   * 
   * @param x
   * @param y
   */
  public void setLocation(int x, int y) {
    this.x = location.x = x;
    this.y = location.y = y;
  }

}
