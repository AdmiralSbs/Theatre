import java.io.Serializable;

public class Showing implements Serializable{

    static final long serialVersionUID = 111L;
    private final int showTime;
    private int seatsSold;

    public Showing(int sT, int sS) {
        showTime = sT;
        seatsSold = sS;
    }

    public int getShowTime() {
        return showTime;
    }

    public int getSeatsSold() {
        return seatsSold;
    }

    public void setSeatsSold(int sS) {
        seatsSold = sS;
    }

    public String toPrettyString() { return "Show Time: " + getShowTimePretty() + ", Seats Sold: " + seatsSold; }

    public String getShowTimePretty() {
        return TheatreManager.getPrettyTime(showTime);
    }

}
