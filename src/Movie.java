import java.io.Serializable;

public class Movie implements Serializable{

    static final long serialVersionUID = 222L;
    private final String name;
    private final String rating;
    private final String genre;
    private final int length;

    public Movie(String n, String r, String g, int l) {
        name = n;
        rating = r;
        genre = g;
        length = l;
    }

    public String getName() {
        return name;
    }

    public int getLengthRounded() {
        if (length % 30 == 0) return length;
        else {
            return 30*(length/30 + 1);
        }
    }

    private String getLengthPretty() {
        return TheatreManager.getPrettyTime(length);
    }

    public String getPrettyString() {
        return name + " (" + genre + ", Rated " + rating + ") - " + getLengthPretty() + " long";
    }
}
