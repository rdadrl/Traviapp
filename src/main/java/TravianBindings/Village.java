package TravianBindings;

public class Village {
    public String name;
    public int x;
    public int y;

    private int lumber;
    private int clay;
    private int iron;
    private int crop;

    public Village(String vName, int xCoord, int yCoord) {
        name = vName;
        x = xCoord;
        y = yCoord;
    }

    public void setName(String newName) { name = newName; }
    public String getName() { return name; }

    public void setX(int newX) { x = newX; }
    public int getX() { return x; }

    public void setY(int newY) { y = newY; }
    public int getY() { return y; }

    public void setLumber(int newLumber) { lumber = newLumber; }
    public int getLumber() { return lumber; }

    public void setClay(int newClay) { clay = newClay; }
    public int getClay() { return clay; }

    public void setIron(int newIron) { iron = newIron; }
    public int getIron() { return iron; }

    public void setCrop(int newCrop) { crop = newCrop; }
    public int getCrop() { return crop; }
}
