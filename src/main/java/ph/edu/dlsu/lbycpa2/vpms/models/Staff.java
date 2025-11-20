package ph.edu.dlsu.lbycpa2.vpms.models;

public class Staff {
    private String name;
    private String rank;
    private String specialty;

    public Staff(String name, String rank, String specialty) {
        this.name = name;
        this.rank = rank;
        this.specialty = specialty;
    }

    public String getName() { return name; }
    public String getRank() { return rank; }
    public String getSpecialty() { return specialty; }

    @Override
    public String toString() {
        return name + " - " + rank + " - " + specialty;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Staff)) return false;
        Staff s = (Staff) o;
        return name.equalsIgnoreCase(s.name)
                && rank.equalsIgnoreCase(s.rank)
                && specialty.equalsIgnoreCase(s.specialty);
    }

    @Override
    public int hashCode() {
        return (name + rank + specialty).toLowerCase().hashCode();
    }
}


