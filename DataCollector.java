import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataCollector {
    public static void main(String[] args) {
        String filePath = "data.csv"; // Replace with your actual file path
        Map<String, List<String>> dataMap = new LinkedHashMap<>(); // LinkedHashMap to preserve order of insertion

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Read the first line to get headers

            if (line != null) {
                String[] headers = line.split(","); // Assumes that the CSV uses commas to separate data
                for (String header : headers) {
                    dataMap.put(header, new ArrayList<>()); // Initialize a list for each header
                }

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    for (int i = 0; i < values.length; i++) {
                        dataMap.get(headers[i]).add(values[i]); // Add each value to the corresponding list
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output to verify the contents
        printDataMap(dataMap);
    }

    private static void printDataMap(Map<String, List<String>> dataMap) {
        for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}


public class StockMovingAverage {

    // Assuming this function is within a class that has access to 'stockPrices' Map
    public static void calculateAndPrintMovingAverages(Map<String, List<Double>> stockPrices, String stockName, int start, int end) {
        if (!stockPrices.containsKey(stockName)) {
            System.out.println("Stock name not found: " + stockName);
            return;
        }

        List<Double> prices = stockPrices.get(stockName);
        List<Double> movingAverages = new ArrayList<>();

        // Ensure 'start' and 'end' are within the bounds of the prices list and 'start' is less than 'end'
        if (start < 1 || end > prices.size() || start > end) {
            System.out.println("Invalid start or end parameters.");
            return;
        }

        // Adjust indices for 0-based indexing in Java lists
        start--; // Since provided 'start' and 'end' are likely 1-based

        // Calculate moving averages for specified range
        for (int i = start; i <= end - 5; i++) {
            double sum = 0;
            for (int j = i; j < i + 5; j++) {
                sum += prices.get(j);
            }
            double average = sum / 5;
            movingAverages.add(average);
        }

        // Output results
        System.out.println("Moving averages for " + stockName + " from day " + (start + 1) + " to day " + end + ":");
        for (Double avg : movingAverages) {
            System.out.printf("%.2f ", avg);
        }
        System.out.println(); // New line after the averages
    }

    // Example of how you might call this method:
    public static void main(String[] args) {
        // Assuming stockPrices is already populated
        Map<String, List<Double>> stockPrices = /* the map you've filled with stock data */;

        if (args.length < 5) {
            System.out.println("Usage: java StockDataProcessor <stockName> <startDay> <endDay>");
            return;
        }

        String stockName = args[2];
        int start = Integer.parseInt(args[3]);
        int end = Integer.parseInt(args[4]);

        calculateAndPrintMovingAverages(stockPrices, stockName, start, end);
    }
}
