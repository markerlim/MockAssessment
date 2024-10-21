package task02src;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        int port = 3000;  // Default port

        // If a port is provided as an argument, use it
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 3000.");
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Handle client connection in a new thread
                new ClientHandler(socket).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private Random random = new Random();
    private static final String PRODUCT_FILE = "./task02src/listofproducts.txt";
    private Product[] products; // Store products for efficient access

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.products = readProductsFromFile(); // Read products once
        } catch (IOException e) {
            System.out.println("Error reading products: " + e.getMessage());
            closeSocket();
        }
    }

    public void run() {
        try {
            // Get output stream to send data to client
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Generate random request_id
            String requestId = UUID.randomUUID().toString();
            int budget = random.nextInt(500) + 100;  // Random budget between 100 and 500

            // Send the request ID, item count, and budget
            writer.println("request_id: " + requestId);
            writer.println("item_count: " + products.length);
            writer.println("budget: " + budget);
            writer.println("prod_listing");

            // Send product details
            for (Product product : products) {
                writer.println("prod_start");
                writer.println("prod_id: " + product.getId());
                writer.println("title: " + product.getTitle());
                writer.println("price: " + product.getPrice());
                writer.println("rating: " + product.getRating());
                writer.println("prod_end");
            }
            System.out.println("Awaiting Client Response");
            // Wait for response from the client
            String clientResponse;
            List<String> itemList = new ArrayList<>(); // List to hold item IDs
            while ((clientResponse = reader.readLine()) != null) {
                // Process the received client response
                if (clientResponse.equals("client_end")) {
                    break; // Exit the loop when the client indicates it has finished sending data
                }

                // Handle individual lines of the client's response
                if (clientResponse.startsWith("request_id:")) {
                    String clientRequestId = clientResponse.split(":")[1].trim();
                    System.out.println("Received request ID: " + clientRequestId);
                } else if (clientResponse.startsWith("name:")) {
                    String name = clientResponse.split(":")[1].trim();
                    System.out.println("Received name: " + name);
                } else if (clientResponse.startsWith("email:")) {
                    String email = clientResponse.split(":")[1].trim();
                    System.out.println("Received email: " + email);
                } else if (clientResponse.startsWith("items:")) {
                    String[] itemIds = clientResponse.split(":")[1].trim().split(",");
                    for (String id : itemIds) {
                        itemList.add(id.trim());
                    }
                    System.out.println("Received items: " + itemList);
                } else if (clientResponse.startsWith("spent:")) {
                    int spent = Integer.parseInt(clientResponse.split(":")[1].trim());
                    System.out.println("Received spent amount: " + spent);
                    // Validate the budget and rating/price sorting here
                    boolean valid = validateRequest(itemList, spent, budget);
                    // Send success or failure response based on validation
                    if (valid) {
                        writer.println("status: success");
                    } else {
                        writer.println("status: failure");
                    }
                } else if (clientResponse.startsWith("remaining:")) {
                    int remaining = Integer.parseInt(clientResponse.split(":")[1].trim());
                    System.out.println("Received remaining amount: " + remaining);
                }
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeSocket(); // Ensure the socket is closed
        }
    }

    // Validate the request based on the items and budget
    private boolean validateRequest(List<String> itemIds, int spent, int budget) {
        if (spent > budget) {
            return false; // Exceeded budget
        }

        // Retrieve selected products based on itemIds
        List<Product> selectedProducts = new ArrayList<>();
        for (String id : itemIds) {
            for (Product product : products) {
                if (product.getId() == id) {
                    selectedProducts.add(product);
                    break; // Found the product, no need to continue inner loop
                }
            }
        }

        // Sort selected products first by rating (desc) then by price (asc)
        selectedProducts.sort((p1, p2) -> {
            int compare = Integer.compare(p2.getRating(), p1.getRating()); // Descending order for rating
            if (compare == 0) {
                return Integer.compare(p1.getPrice(), p2.getPrice()); // Ascending order for price
            }
            return compare;
        });

        // Verify the order of selected products
        for (int i = 0; i < selectedProducts.size() - 1; i++) {
            if (selectedProducts.get(i).getRating() < selectedProducts.get(i + 1).getRating() ||
                (selectedProducts.get(i).getRating() == selectedProducts.get(i + 1).getRating() &&
                selectedProducts.get(i).getPrice() > selectedProducts.get(i + 1).getPrice())) {
                return false; // Incorrect sorting
            }
        }

        return true; // Validation passed
    }

    // Method to read products from a text file and return them as an array of Product objects
    private Product[] readProductsFromFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(PRODUCT_FILE));
        String line;
        List<Product> productList = new ArrayList<>();
    
        while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                String[] details = line.split(",");  // Assuming CSV format: id,title,price,rating
                if (details.length == 4) {
                    try {
                        String id = details[0].trim(); // Keep id as a String
                        String title = details[1].trim();
                        int price = Integer.parseInt(details[2].trim());
                        int rating = Integer.parseInt(details[3].trim());
                        productList.add(new Product(id, title, price, rating));
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing product details: " + e.getMessage());
                    }
                }
            }
        }
        br.close();
        return productList.toArray(new Product[0]);
    }    

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Client disconnected");
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}

// Product class to hold product details
class Product {
    private String id; // Change id to String
    private String title;
    private int price;
    private int rating;

    public Product(String id, String title, int price, int rating) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.rating = rating;
    }

    public String getId() { // Change return type to String
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public int getRating() {
        return rating;
    }
}
