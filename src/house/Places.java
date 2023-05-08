package house;

import jason.environment.grid.Location;

/*
 * Identificación de los lugares fijos del grid
 */
enum Places {
  FRIDGE(new Location(0, 0), HouseModel.FRIDGE),
  OWNER(new Location(HouseModel.GSize - 1, HouseModel.GSize - 1), HouseModel.OWNER),
  // OWNER_MUSK(new Location(HouseModel.GSize / 2, 0), HouseModel.OWNER_MUSK),
  BIN(new Location(HouseModel.GSize - 1, 0), HouseModel.BIN),
  DELIVERY(new Location(0, HouseModel.GSize - 1), HouseModel.DELIVERY),
  DISHWASHER(new Location(2, 0), HouseModel.DISHWASHER),
  CUPBOARD(new Location(4, 0), HouseModel.CUPBOARD),
  BASE_ROBOT(new Location(HouseModel.GSize / 2, HouseModel.GSize / 2), -1, 0),
  BASE_CLEANER(new Location(HouseModel.GSize / 2 - 1, HouseModel.GSize - 1), -1, 0),
  BASE_STOREKEEPER(new Location(HouseModel.GSize / 2 + 1, HouseModel.GSize - 1), -1, 0);

  public Location location;
  public Location robotLoc;
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
