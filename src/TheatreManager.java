import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/*  Addons:
        -Input error handling to the MAX
            -Only accepts Strings containing acceptable characters or matching an array
            -Only accepts integers within a given range or matching an array
            -Most parts are encapsulated in order to create more legible methods
        -No method can continue if there is no valid input
            -Add movie requires that a theatre not have one yet
            -Delete movie requires that a theatre have one
            -Show times cannot be added unless they allow for a maximum of three
            -Ticket purchasing can only happen in theatres with show times that have openings
 */
public class TheatreManager {

    private static final String ACCEPTABLE_CHARACTERS =
            "abcdefghijklmnopqrstuvwxyz" +
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "1234567890-=!@#$%^&*()_+" +
                    "[];',./{}|:<>?\\\"`~ ";
    private static final String[] RATINGS = {"G", "PG", "PG-13", "PG13", "PG 13", "R",
            "NC17", "NC-17", "NC 17", "UR", "NR", "RP"};
    private Theatre[] theatres;
    private Scanner sc;

    public static void main(String[] args) throws Exception {
        new TheatreManager(); //Being lazy
    }

    public static String getPrettyTime(int i) {
        String end;
        int first = i / 60;
        int second = i % 60;
        if (first == 0) first = 12;
        end = first + ":" + String.format("%02d", second);
        return end;
    }

    private TheatreManager() throws Exception { //Real main method here (gets around static constraints)
        setUp();
        generalLoop();
    }

    private void setUp() throws Exception {
        File file = new File("TheatreData.txt");
        if (file.createNewFile()) { //If it cannot create a new file
            firstSetUp();
        }
        try {
            ObjectInputStream infile = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            theatres = new Theatre[10];
            for (int i = 0; i < 10; i++) {
                theatres[i] = (Theatre) infile.readObject();
            }
            infile.close();
            System.out.println("File Read");
        } catch (EOFException e) {
            System.err.println("No save exists, initiating from scratch");
            firstSetUp();
        } catch (Exception e1) {
            firstSetUp();
            System.err.println("Error found, initiating from scratch");
            e1.printStackTrace();
        }
    }

    private void firstSetUp() {
        theatres = new Theatre[10];
        int[] seats = {200, 180, 175, 150, 140, 120, 100, 80, 50, 20};
        for (int i = 0; i < 10; i++) {
            theatres[i] = new Theatre(i + 1, seats[i], null);
        }
    }

    private void generalLoop() throws Exception {
        sc = new Scanner(System.in);
        int input;
        while (true) {
            printOptions();
            input = getInput(1, 8, "");
            if (input == 1) displayTheatreInformation();
            else if (input == 2) addMovie();
            else if (input == 3) deleteMovie();
            else if (input == 4) changeAssignedTheatre();
            else if (input == 5) addShowTime();
            else if (input == 6) deleteShowTime();
            else if (input == 7) purchaseTickets();
            else if (input == 8) break;
            else throw new Exception("Screw Up");
        }
        saveAndExit();
    }

    private void displayTheatreInformation() {
        System.out.println("Theatre Information:");
        for (int i = 0; i < 10; i++) {
            if (theatres[i].getMovie() != null) {
                System.out.println("  Theatre " + (i+1) + " is showing " + theatres[i].getMovie().getPrettyString());
                if (theatres[i].getShows()[0] == null && theatres[i].getShows()[1] == null && theatres[i].getShows()[2] == null)
                    System.out.println("  No Showings");
                else {
                    System.out.println("  Showings:");
                    for (Showing s : theatres[i].getShows()) {
                        if (s != null)
                            System.out.println("    " + s.toPrettyString() + "/" + theatres[i].getNumSeats());
                    }
                }
            } else System.out.println("  Theatre " + (i+1) + " is w/o a movie");
        }
    }

    private void addMovie() {
        boolean good = false;
        for (Theatre t : theatres) if (t.getMovie() == null) good = true;
        if (!good) {
            System.out.println("All Theatres Have a Movie");
            return;
        }
        String name, genre, rating;
        int length, theatreNum;

        Name_Loop:
        while (true) {
            name = getAcceptableString("Enter Movie Name: ", "Invalid Input");
            for (Theatre t : theatres)
                if (t.getMovie() != null) {
                    if (t.getMovie().getName().equals(name)) {
                        System.out.println("That Movie Already Exists");
                        continue Name_Loop;
                    }
                }
            break;
        }
        genre = getAcceptableString("Enter Movie Genre: ", "Invalid Input");
        rating = getAcceptableString("Enter Movie Rating: ", "Invalid Input", RATINGS);
        length = getInput(90, 150, "Enter Movie Length (90-150 min): ");

        ArrayList<Integer> ar = new ArrayList<>();
        for (int i = 0; i < 10; i++) if (theatres[i].getMovie() == null) ar.add(i);
        int[] arr = new int[ar.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = ar.get(i) + 1;
        theatreNum = getInput("Enter Theatre Number: ", arr) - 1;

        theatres[theatreNum].setMovie(new Movie(name, rating, genre, length));
        System.out.println(theatres[theatreNum].getMovie().getName() + " has been added to Theatre " + (theatreNum+1));
    }

    private void deleteMovie() {
        boolean good = false;
        for (Theatre t : theatres) if (t.getMovie() != null) good = true;
        if (!good) {
            System.out.println("No Theatres Have A Movie");
            return;
        }
        int num;
        while (true) {
            num = getTheatreByNameOrNumber("Enter Theatre Number or its Movie Name to Remove: ");
            if (theatres[num].getMovie() == null)
                System.out.println("That Theatre Has No Movie");
            else break;
        }
        System.out.println(theatres[num].getMovie().getName() + " has been removed from Theatre " + (num+1));
        theatres[num].setMovie(null);
        theatres[num].setShows(new Showing[3]);
    }

    private void changeAssignedTheatre() {
        ArrayList<Integer> ar = new ArrayList<>();
        for (int i = 0; i < 10; i++) if (theatres[i].getMovie() != null) ar.add(i);
        if (ar.size() < 2) {
            System.out.println("No Theatres To Swap");
            return;
        }

        int[] arr = new int[ar.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = ar.get(i) + 1;
        int t1 = getInput("Enter First Theatre Number: ", arr) - 1;

        ar.remove(t1);
        arr = new int[ar.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = ar.get(i) + 1;
        int t2 = getInput("Enter Second Theatre Number: ", arr) - 1;

        Movie tempM = theatres[t1].getMovie();
        Showing[] tempS = theatres[t1].getShows();

        theatres[t1].setMovie(theatres[t2].getMovie());
        theatres[t1].setShows(theatres[t2].getShows());
        theatres[t2].setMovie(tempM);
        theatres[t2].setShows(tempS);
        System.out.println("Theatres " + (t1 + 1) + " and " + (t2 + 1) + " have been swapped");
    }

    private void addShowTime() {
        int sum = 0, num, num2;
        for (Theatre t : theatres) {
            if (t.getMovie() == null) sum += 11;
            if (t.getShows()[2] != null) sum++;
        }
        if (sum % 11 == 10) { //Stored it in base 11 because I'm lazy
            System.out.println("All Theatres Have Three Showings Already");
            return;
        } else if (sum / 11 == 10) {
            System.out.println("No Theatre Has a Movie");
            return;
        }

        while (true) {
            num = getTheatreByNameOrNumber("Enter Theatre Number or its Movie Name: ");
            if (theatres[num].getShows()[2] != null) {
                System.out.println("That Theatre already has three showings");
            } else if (theatres[num].getMovie() == null) {
                System.out.println("That Theatre has no assigned show");
            } else break;
        }

        String[] times = {"12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00",
                "12:30", "1:30", "2:30", "3:30", "4:30", "5:30", "6:30", "7:30", "8:30", "9:30", "10:30",
                "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
                "01:30", "02:30", "03:30", "04:30", "05:30", "06:30", "07:30", "08:30", "09:30"};
        Showing s;
        String input;
        while (true) {
            input = getAcceptableString("Enter ShowTime in hh:mm ", "Invalid Time", times);
            if (input.length() == 4) input = "0" + input;
            num2 = (Integer.parseInt(input.substring(0, 2)) % 12) * 60 + Integer.parseInt(input.substring(3)); //Convert time to minutes after 12
            s = new Showing(num2, 0);
            if (willThisShowingLockUpTheTheatre(s, theatres[num].getMovie()) || !doesThisShowingFitTheTheatre(s, theatres[num])) {
                System.out.println("Showing Doesn't Fit");
            } else break;
        }

        theatres[num].addShow(s);
        System.out.println("Theatre " + (num + 1) + " now has a showing at " + input);
    }

    private void deleteShowTime() {
        int sum = 0;
        for (Theatre t : theatres) if (t.getShows()[0] != null) sum++;
        if (sum == 0) {
            System.out.println("There Are No Existing Showings");
            return;
        }
        int num;
        while (true) {
            num = getTheatreByNameOrNumber("Enter Theatre Number or its Movie Name: ");
            if (theatres[num].getShows()[0] == null) {
                System.out.println("That Theatre Has No Showings");
            } else {
                break;
            }
        }
        System.out.println("Show times are:");
        sum = 3;
        for (Showing s : theatres[num].getShows()) {
            if (s != null) System.out.println(s.toPrettyString());
            else sum--;
        }
        int num2 = getInput(1, sum, "Select Showing to Remove (1-" + sum + "): ");
        System.out.println("Removed Showing At " + theatres[num].getShows()[num2 - 1].getShowTimePretty() + " From Theatre " + (num + 1));
        theatres[num].removeShow(num2 - 1);
    }

    private void purchaseTickets() {
        int sum = 0;
        for (Theatre t : theatres) {
            for (Showing s : t.getShows())
                if (s != null)
                    if (s.getSeatsSold() < t.getNumSeats()) sum++;
            if (t.getMovie() != null) sum += 11;
        }
        if (sum / 11 == 0) {
            System.out.println("No Theatre has a movie");
            return;
        } else if (sum % 11 == 0) {
            System.out.println("All Showings for all Movies are filled");
            return;
        }
        int num;
        while (true) {
            num = getTheatreByNameOrNumber("Enter Theatre Number or its Movie Name: ");
            sum = 0;
            if (theatres[num].getShows()[0] == null) {
                System.out.println("That Theatre has no Showings");
                continue;
            }
            for (Showing s : theatres[num].getShows())
                if (s != null)
                    if (s.getSeatsSold() < theatres[num].getNumSeats()) sum++;
            if (sum == 0) System.out.println("All Showings For That Theatre Are Filled");
            else break;
        }

        System.out.println("Show Times are:");
        sum = 3;
        for (Showing s : theatres[num].getShows()) {
            if (s != null) System.out.println(s.toPrettyString() + "/" + theatres[num].getNumSeats());
            else sum--;
        }
        int num2 = getInput(1, sum, "Select Showing to Purchase Tickets For (1-" + sum + "): ");
        int num3 = getInput(1, theatres[num].getNumSeats() - theatres[num].getShows()[num2 - 1].getSeatsSold(),
                "Select Number of Tickets to Purchase");
        System.out.println(num3 + " Tickets Purchased for the " + theatres[num].getShows()[num2 - 1].getShowTimePretty() +
                " Showing of " + theatres[num].getMovie().getName());
        theatres[num].getShows()[num2 - 1].setSeatsSold(theatres[num].getShows()[num2 - 1].getSeatsSold() + num3);
    }

    private void saveAndExit() throws Exception {
        File file = new File("TheatreData.txt");
        ObjectOutputStream outfile = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        for (int i = 0; i < 10; i++) {
            outfile.writeObject(theatres[i]);
        }
        outfile.close();
        System.exit(0);
    }

    private int getInput(int min, int max, String message) {
        String input = "";
        int num = -1;
        while (input.equals("")) {
            System.out.print(message);
            input = sc.nextLine();
            if (input.length() == 0) continue;
            try {
                num = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                input = "";
                continue;
            }
            if (num < min || num > max) {
                System.out.println("Input out of range");
                input = "";
            }
        }
        return num;
    }

    private int getInput(String message, int[] acceptableInputs) {
        String input = "";
        int num = -1;
        boolean good;
        while (input.equals("")) {
            System.out.print(message);
            input = sc.nextLine();
            if (input.length() == 0) continue;
            try {
                num = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                input = "";
                continue;
            }
            good = false;
            for (int i : acceptableInputs) {
                if (num == i) good = true;
            }
            if (!good) {
                System.out.println("Input out of range");
                input = "";
            }
        }
        return num;
    }

    private void printOptions() {
        System.out.println(
                "1. Display Theatre Information" + "\n" +
                        "2. Add Movie" + "\n" +
                        "3. Delete Movie" + "\n" +
                        "4. Change Assigned Theatre" + "\n" +
                        "5. Add Show Time" + "\n" +
                        "6. Delete Show Time" + "\n" +
                        "7. Purchase Tickets" + "\n" +
                        "8. Save and Exit"
        );
    }

    private boolean acceptableString(String s) {
        for (char c : s.toCharArray())
            if (!ACCEPTABLE_CHARACTERS.contains(c + ""))
                return false;
        return true;
    }

    private String getAcceptableString(String prompt, String errorMessage) {
        String input = "";
        while (input.equals("")) {
            System.out.print(prompt);
            input = sc.nextLine();
            if (input.length() == 0) continue;
            if (!acceptableString(input)) {
                input = "";
                if (errorMessage.length() > 0) System.out.println(errorMessage);
            }
        }
        return input;
    }

    private String getAcceptableString(String prompt, String errorMessage, String[] acceptableInputs) {
        String input = "";
        boolean good;
        while (input.equals("")) {
            System.out.print(prompt);
            input = sc.nextLine();
            if (input.length() == 0) continue;

            good = false;
            for (String s : acceptableInputs) if (s.equals(input)) good = true;

            if (!good) {
                input = "";
                if (errorMessage.length() > 0) System.out.println(errorMessage);
            }
        }
        return input;
    }

    private int getTheatreByNameOrNumber(String message) {
        System.out.print(message);
        String input = "";
        int num = -1;
        boolean fail;
        while (input.equals("")) {
            input = sc.nextLine();
            if (input.length() == 0) continue;
            try {
                num = Integer.parseInt(input);
                if (num < 1 || num > 10) {
                    System.out.println("Invalid Theatre Number");
                    input = "";
                } else num--;
                continue;
            } catch (NumberFormatException ignored) {
            }

            fail = true;
            for (int i = 0; i < 10; i++) {
                if (theatres[i] == null) continue;
                if (theatres[i].getMovie() == null) continue;
                if (theatres[i].getMovie().getName().equals(input)) {
                    num = i;
                    fail = false;
                }
            }
            if (fail) {
                System.out.println("That Movie Doesn't Exist");
                input = "";
            }
        }
        return num;
    }

    private boolean willThisShowingLockUpTheTheatre(Showing s, Movie m) { //Turns out this isn't actually useful
        int movieLength = m.getLengthRounded();
        int showingStart = s.getShowTime();

        int spotsAllowed = 0;
        for (int i = 1; i < 4; i++) {
            if (showingStart - i * (30 + movieLength) < 0)
                spotsAllowed++;
            if (showingStart + i * (30 + movieLength) > 660)
                spotsAllowed++;
        }

        return (spotsAllowed < 2); //Yes if one/zero spots allowed
    }

    private boolean doesThisShowingFitTheTheatre(Showing s, Theatre t) {
        int space = t.getMovie().getLengthRounded() + 30;
        for (Showing show : t.getShows())
            if (show != null)
                if (Math.abs(show.getShowTime() - s.getShowTime()) < space) return false;
        return true;
    }
}
