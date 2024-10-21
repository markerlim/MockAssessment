package src;

import java.io.IOException;

public class Main {
    public static void main (String[] args) throws IOException{
        String filename = args[0];
        System.out.println(filename);
        Functions function = new Functions();
        function.OpenCSV(filename);
    }
}
