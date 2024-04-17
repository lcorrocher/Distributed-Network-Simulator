package utils;

/**
 * Class for colouring text in the console.
 */
public class Colour {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String YELLOW_BOLD = "\u001B[93m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String ORANGE = "\u001B[38;5;208m";

    public static String red(String s) {
        return RED + s + RESET;
    }
    public static String black(String s) {
        return BLACK + s + RESET;
    }
    public static String blue(String s) {
        return BLUE + s + RESET;
    }
    public static String yellow(String s) {
        return YELLOW + s + RESET;
    }
    public static String cyan(String s) {
        return CYAN + s + RESET;
    }
    public static String purple(String s) {
        return PURPLE + s + RESET;
    }
    public static String yellowBold(String s) {
        return YELLOW_BOLD + s + RESET;
    }
    public static String white(String s) {
        return WHITE + s + RESET;
    }
    public static String green(String s) {
        return GREEN + s + RESET;
    }
    public static String orange(String s) {
        return ORANGE + s + RESET;
    }



    public static void main(String[] args) {
        System.out.println(Colour.red("This is red text"));
        System.out.println(Colour.black("This is black text"));
        System.out.println(Colour.blue("This is blue text"));
        System.out.println(Colour.yellow("This is yellow text"));
        System.out.println(Colour.cyan("This is cyan text"));
        System.out.println(Colour.purple("This is purple text"));
        System.out.println(Colour.yellowBold("This is bold yellow text"));
        System.out.println(Colour.white("This is white text"));
        System.out.println(Colour.green("This is green text"));
        System.out.println(Colour.orange("This is orange text"));
    }
}
