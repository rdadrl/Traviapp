package TravianBindings;

public class Building {
    private String name;
    private int level;

    private boolean building;
    private int dueInSec;

    public Building (String name, int level) {
        this.name = name;
        this.level = level;
    }

    public void setName(String newName) { name = newName; }
    public String getName() { return name; }

    public void setLevel(int newLevel) { level = newLevel; }
    public int getLevel() { return level; }

    public void setBuilding(boolean val) { building = val; }
    public boolean isBuilding() { return building; }

    public void setDueInSec(int newDue) { dueInSec = newDue; }
    public int getDueInSec() { return dueInSec; }
    public int getDueInMin() { return (dueInSec / 60); }

}
