import java.io.Serializable;

public class Theatre implements Serializable {

    static final long serialVersionUID = 333L;
    private final int theatreNumber; //Was entirely useless
    private final int numSeats;
    private Movie movie;
    private Showing[] shows = new Showing[3];

    public Theatre(int tN, int nS, Movie m) {
        theatreNumber = tN;
        numSeats = nS;
        movie = m;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public Movie getMovie() {
        return movie;
    }

    public Showing[] getShows() {
        return shows;
    }


    public void setMovie(Movie m) {
        movie = m;
    }

    public void setShows(Showing[] s) {
        shows = s;
    }

    public void addShow(Showing s) {
        for (int i = 0; i < shows.length; i++) {
            if (shows[i] == null) {
                shows[i] = s;
                return;
            }
        }
    }

    /*
    public void removeShow(Showing s) {
        int spot = -1;
        for (int i = 0; i < 2; i++) if (shows[i] == s) spot = i;
        removeShow(spot);
    }*/

    public void removeShow(int spot) {
        if (spot == 0) shows[0] = shows[1];
        if (spot <= 1) shows[1] = shows[2];
        shows[2] = null;
    }
}
