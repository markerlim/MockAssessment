package src;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Functions {
    private Map<String, Map<String, Float>> listofdata;
    private List<String> listofcategory;

    public void OpenCSV(String filename) throws IOException {
        Reader r = new FileReader(filename);
        BufferedReader br = new BufferedReader(r);
        this.listofdata = new HashMap<>() {
        };
        this.listofcategory = new ArrayList<>();
        System.out.println("File Read");
        String line;
        int counter = 0;
        while ((line = br.readLine()) != null) {
            counter++;
            if (counter == 1) {
                continue;
            }
            Map<String, Float> appNameRating = new HashMap<>();
            String[] lineData = line.split(",(?=([^\\\"]|\\\"[^\\\"]*\\\")*$)");
            String appName = lineData[0];
            String category = lineData[1].toUpperCase().trim();
            float rating = Float.parseFloat(lineData[2]);
            if (listofcategory.contains(category)) {
                // get current category data with app and rating
                appNameRating = listofdata.get(category);
                appNameRating.put(appName, rating);
            } else {
                // add to empty appNameRating
                listofcategory.add(category);
                appNameRating.put(appName, rating);
            }
            listofdata.put(category, appNameRating);
        }
        printDetails();
        System.out.printf("total lines read: %s", counter - 1);
        br.close();
    }

    private void printDetails() {
        for (String category : listofdata.keySet()) {
            System.out.printf("Category: %s\n", category);
            Map<String, Float> appNameRating = listofdata.get(category);
            float highest = 0;
            String highestRated = "";
            float lowest = 9999;
            String lowestRated = "";
            float sum = 0;
            float avg = 0;
            int count = 0;
            int discarded = 0;
            for (String name : appNameRating.keySet()) {
                count++;
                Float rating = appNameRating.get(name);
                if (rating > highest) {
                    highest = rating;
                    highestRated = name;
                } 
                if(rating< lowest){{
                    lowest = rating;
                    lowestRated = name;
                }}
                if(rating.isNaN()){
                    discarded++;
                    continue;
                }
                sum += rating;
            }
            avg = sum/(count - discarded);
            System.out.printf("\tHighest: %s, (%f)\n",highestRated,highest);
            System.out.printf("\tLowest: %s, (%f)\n",lowestRated,lowest);
            System.out.printf("\tAverage: %f\n",avg);
            System.out.printf("\tCount: %d\n", count);
            System.out.printf("\tDiscarded: %d\n", discarded);
        }
    }
}
