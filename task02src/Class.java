package task02src;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;


public class Class {
    public static void main(String[] args) throws UnknownHostException, IOException {
        String request = "";
        String itemCount = "";
        String budget ="";
        List<Item> itemlist = new ArrayList<>();
        String host = "localhost";
        int port = 3000;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        Socket conn = new Socket(host, port);
        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int bytesRead;
        List<Object> listofobject = new ArrayList<>();

        String completeLine = "";
        // Read bytes from server
        while ((bytesRead = bis.read(buffer)) != -1) {
            String receivedData = new String(buffer, 0, bytesRead);
            completeLine += receivedData;
            if (receivedData.endsWith("prod_end\n")) {
                break;
            }
        }
        String[] lineData = completeLine.split("\n");

        for (String line : lineData) {

            if (line.startsWith("prod_listing") || line.startsWith("prod_start")
                    || line.startsWith("prod_end")) {
                continue;
            }
            String[] lineParts = line.split(":");
            if (line.startsWith("request_id")) {
                request = lineParts[1].trim();
                continue;
            }
            if (line.startsWith("item_count")) {
                itemCount =lineParts[1].trim();
                continue;
            }
            if (line.startsWith("budget")) {
                budget = lineParts[1].trim();
                continue;
            }
            listofobject.add(lineParts[1].trim());
        }

        for (int i = 0; i<listofobject.size(); i += 4){
            Item newItem = new Item();
            newItem.setId(listofobject.get(i).toString());
            newItem.setProduct(listofobject.get(i+1).toString());
            newItem.setPrice(Integer.parseInt(listofobject.get(i+2).toString()));
            newItem.setRating(Integer.parseInt(listofobject.get(i+3).toString()));
            itemlist.add(newItem);
        }
    

        List<Item> result = itemlist.stream()
        .sorted(Comparator.comparing(Item::getRating).reversed()
            .thenComparing(Item::getPrice).reversed())
        .toList();

        String listofid = "";
        int budgetInt = Integer.parseInt(budget);
        int countItem = 0;
        while(budgetInt > 0){
            int price = result.get(countItem).getPrice();
            if(price > budgetInt){
                continue;
            } else {
                listofid += "," + result.get(countItem).getId().trim();
                budgetInt -= price;
            }
            countItem++;
            if(countItem == Integer.parseInt(itemCount)){
                listofid.replaceFirst(",","");
                break;
            }
        }

        Console cons = System.console();

        String name = cons.readLine(">Name: ");
        String email = cons.readLine(">Email: ");
        int remaining = Integer.parseInt(budget) - budgetInt;

        OutputStream os = conn.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        String line1 = "request_id: "+request+"\n";
        String line2 = "name: "+name+"\n";
        String line3 = "email: "+email+"\n";
        String line4 = "items: "+listofid+"\n";
        String line5 = "spent: "+budgetInt+"\n";
        String line6 = "remaining: "+remaining+"\n";
        String line7 = "client_end\n";


        bos.write(line1.getBytes()); 
        bos.write(line2.getBytes()); 
        bos.write(line3.getBytes()); 
        bos.write(line4.getBytes()); 
        bos.write(line5.getBytes()); 
        bos.write(line6.getBytes()); 
        bos.write(line7.getBytes()); 
        bos.flush();

    }
}
