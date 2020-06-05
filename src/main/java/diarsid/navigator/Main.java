package diarsid.navigator;

public class Main {

    public static void main(String[] args) {
        Navigator navigator1 = new Navigator();
        navigator1.openInCurrentTab("D:/DEV/0__Engines");

        Navigator navigator2 = new Navigator();
//        navigator.openInCurrentTab("D:/DEV/0__Engines/Java");
        navigator2.openInNewTab("D:/DEV/1__Projects");
    }

}
