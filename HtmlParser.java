import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
class myMath {
    public static double round2f(double num) {
        num = (int)(num * 1000); 
        int a = (int)(num % 10);
        if (a >= 5) {
            num += 10;
        }
        if(a <= -5){
            num -= 10;
        }
        num -= a;
        num = num/1000;
        return num;
    }
}
class StatisticsUtility {

        public static double calculateStandardDeviationForStock(Map<String, List<Double>> stockPrices, String stockName, int start, int end) {
            List<Double> prices = stockPrices.get(stockName);
            // Extract the sublist for the specified range
            List<Double> priceRange = prices.subList(start-1, end );
            return calculateStandardDeviation(priceRange);
        }
    
        private static double calculateStandardDeviation(List<Double> data) {
            double mean = calculateMean(data);
            double sum = 0;
            for (double num : data) {
                sum += Math.pow(num - mean, 2);
            }
            return Math.sqrt(sum / (data.size()-1));
        }
    
        private static double calculateMean(List<Double> data) {
            double sum = 0;
            for (double num : data) {
                sum += num;
            }
            return sum / data.size();
        }
}
class LinearRegression {

    public static double[] calculateCoefficientsForStock(Map<String, List<Double>> stockPrices, String stockName, int start, int end) {
        List<Double> prices = stockPrices.get(stockName);
        List<Double> priceRange = prices.subList(start - 1, end); // Adjust for zero-based index

        int n = priceRange.size();
        double sumTime = 0, sumPrice = 0, sumTimePrice = 0, sumTimeSquared = 0;

        // Calculate sums of t, Y, t*Y, and t^2 for the given price range
        for (int i = 0; i < n; i++) {
            int t = start + i; // Time index adjusted for start
            double y = priceRange.get(i);
            sumTime += t;
            sumPrice += y;
            sumTimePrice += t * y;
            sumTimeSquared += t * t;
        }

        double meanTime = sumTime / n;
        double meanPrice = sumPrice / n;

        double numeratorB1 = 0;
        double denominatorB1 = 0;

        // Calculate numerator and denominator for slope (b1) using the exact formulas
        for (int i = 0; i < n; i++) {
            int t = start + i;
            double y = priceRange.get(i);
            numeratorB1 += (t - meanTime) * (y - meanPrice);
            denominatorB1 += (t - meanTime) * (t - meanTime);
        }

        double b1 = numeratorB1 / denominatorB1;
        double b0 = meanPrice - b1 * meanTime; // Calculate intercept (b0)

        return new double[]{b0, b1}; // Return b0 and b1 as required
    }
}


class StockMovingAverage {

    // Assuming this function is within a class that has access to 'stockPrices' Map
    public static void calculateAndPrintMovingAverages(Map<String, List<Double>> stockPrices, String stockName, int start, int end) {
        if (!stockPrices.containsKey(stockName)) {
            System.out.println("Stock name not found: " + stockName);
            return;
        }

        List<Double> prices = stockPrices.get(stockName);
        List<Double> movingAverages = new ArrayList<>();

        // Ensure 'start' and 'end' are within the bounds of the prices list and 'start' is less than 'end'

        // Adjust indices for 0-based indexing in Java lists
        start--; // Since provided 'start' and 'end' are likely 1-based
        // Calculate moving averages for specified range
        for (int i = start; i <= end - 5; i++) {
            double sum = 0;
            for (int j = i; j < i + 5; j++) {
                sum += prices.get(j);
            }
            double average = myMath.round2f(sum / 5);
            movingAverages.add(average);
        }

        // Output results
        try (FileWriter writer = new FileWriter("output.csv",true)) {
            // Write the header information
            File outputFile = new File("output.csv"); // 'outputFile' is the File object's name
            boolean isEmpty = outputFile.length() == 0;
            if (isEmpty) {
                writer.write(stockName + "," + (start + 1) + "," + end + "\n");
            } else {
                // If not empty, start with a newline to separate from previous data
                writer.write("\n" + stockName + "," + (start + 1) + "," + end + "\n");
            }
            // Iterate through each moving average and write it to the file
            for (int i = 0; i < movingAverages.size(); i++) {
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedNumber = df.format(movingAverages.get(i));
                if (i < movingAverages.size() - 1) {
                    writer.write(formattedNumber + ",");
                }else{
                    writer.write(formattedNumber);
                }
            }
            // Optionally print that the writing is complete
            //System.out.println("Data written to output.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Example of how you might call this method:
    public static void main(String[] args) {
        // Assuming stockPrices is already populated
        String filePath = "data.csv";
        Map<String, List<Double>> stockPrices = DataCollector.loadData(filePath);
        String stockName = args[2];
        int start = Integer.parseInt(args[3]);
        int end = Integer.parseInt(args[4]);

        calculateAndPrintMovingAverages(stockPrices, stockName, start, end);
    }
}
class DataCollector {
    public static Map<String, List<Double>> loadData(String filePath) {
        Map<String, List<Double>> stockPrices = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                String[] headers = line.split(",");
                for (String header : headers) {
                    stockPrices.put(header, new ArrayList<>());
                }

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    for (int i = 0; i < values.length; i++) {
                        stockPrices.get(headers[i]).add(Double.parseDouble(values[i]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockPrices;
    }
}
public class HtmlParser {

    public static void main(String[] args) {
        String filePath = "data.csv";
        String outputFilePath = "output.csv";
        try {
            File file = new File("data.csv");
            //System.out.println("enter into first try");
            if ("0".equals(args[0])) {
                Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw").get();
                System.out.println("enter into try data");
                Elements rows = doc.select("tr");

                String output_title = "";
                String output_content = "";

                // Iterate over each row
                for (Element row : rows) {
                    Elements ths = row.select("th");
                    Elements tds = row.select("td");

                    // If the row has header elements (th)

                    if (!ths.isEmpty()) {
                        for (int i = 0; i < ths.size(); i++) {
                            if (i < ths.size() - 1) {
                                // Not the last element, append with a comma
                                output_title += ths.get(i).text() + ",";
                            } else {
                                // Last element, append with a period
                                output_title += ths.get(i).text() + ".";
                            }
                        }
                        output_title += "\n"; // New line after printing all headers
                    }

                    // If the row has data elements (td)
                    if (!tds.isEmpty()) {
                        for (int i = 0; i < tds.size(); i++) {
                            if (i < tds.size() - 1) {
                                // Not the last element, append with a comma
                                output_content += tds.get(i).text() + ",";
                            } else {
                                // Last element, append with a period
                                output_content += tds.get(i).text() + ".";
                            }
                        }
                        output_content += "\n"; // New line after the row
                    }
                }

                boolean exists = file.exists();
                FileWriter writer = new FileWriter("data.csv", true);

                if (!exists || file.length() == 0) {
                    writer.write(output_title);
                }

                writer.write(output_content); // Write the content of output to data.csv
                writer.close(); // Always close the writer to finalize changes
                //System.out.println("Successfully wrote to the file.");

            } else if ("1".equals(args[0])) {

                if ("0".equals(args[1])) {

                    File outputFile = new File("output.csv");
                    try (
                        FileInputStream fis = new FileInputStream(file);
                        FileOutputStream fos = new FileOutputStream(outputFile);
                        FileChannel source = fis.getChannel();
                        FileChannel destination = fos.getChannel();) 
                        {
                            //System.out.println("enter into try");
                            destination.transferFrom(source, 0, source.size());
                            //System.out.("Data has been copied from data.csv to output.csv.");
                        } catch (IOException e) {
                            //System.out.println("An error occurred during the file copying process.");
                            e.printStackTrace();
                        }


                } else if ("1".equals(args[1])) {

                    StockMovingAverage.main(args);

                } else if ("2".equals(args[1])) {

                    String stockName = args[2]; // Assuming stock name is the first argument
                    int start = Integer.parseInt(args[3]); // Assuming start index is the second argument
                    int end = Integer.parseInt(args[4]); // Assuming end index is the third argument
            
                    try {
                        Map<String, List<Double>> stockPrices = DataCollector.loadData(filePath);
                        double stdDev = StatisticsUtility.calculateStandardDeviationForStock(stockPrices, stockName, start, end);
                        double roundNumber = myMath.round2f(stdDev);
                        // Append results to the file
                        try (FileWriter writer = new FileWriter(outputFilePath, true)) {
                            // Check if file is empty to decide whether to write headers
                            File filenew = new File(outputFilePath);
                            DecimalFormat df = new DecimalFormat("#.##");
                            String formattedNumberNew = df.format(roundNumber);
                            // Write data
                            writer.write(stockName + "," + start + "," + end + "\n");
                            writer.write(formattedNumberNew + "\n");                        
                        }
                        //System.out.println("Data successfully written to " + outputFilePath);
                    } catch (IOException e) {
                        System.out.println("Error in file handling: " + e.getMessage());
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Start and end indices must be integers.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }

                } else if ("3".equals(args[1])) {

                    int start = Integer.parseInt(args[3]); // Assuming start index is the second argument
                    int end = Integer.parseInt(args[4]); // Assuming end index is the third argument
                    Map<String, List<Double>> stockPrices = DataCollector.loadData(filePath);
                    Map<String, Double> stockStandardDeviations = new HashMap<>();

                    for (Map.Entry<String, List<Double>> entry : stockPrices.entrySet()) {
                        double stdDev = StatisticsUtility.calculateStandardDeviationForStock(stockPrices, entry.getKey(), start, end);
                        stockStandardDeviations.put(entry.getKey(), myMath.round2f(stdDev));
                    }

                    // Find the top 3 stocks with the highest standard deviations
                    List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(stockStandardDeviations.entrySet());
                    sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                    try (FileWriter writer = new FileWriter(outputFilePath, true)) {
                        // Check if file is empty to decide whether to write headers
                        File filenew = new File(outputFilePath);
                        DecimalFormat df = new DecimalFormat("#.##");

                        //System.out.println("Top 3 stocks with the highest standard deviations:");

                        for (int i = 0; i < Math.min(3, sortedEntries.size()); i++) {
                            Map.Entry<String, Double> entry = sortedEntries.get(i);
                            writer.write(entry.getKey()+ ",");
                        }
                        writer.write(start + "," + end + "\n");

                        for (int i = 0; i < Math.min(3, sortedEntries.size()); i++) {
                            Map.Entry<String, Double> entry = sortedEntries.get(i);
                            writer.write(df.format(entry.getValue()));
                            if(i < 2) {
                                writer.write(",");
                            }
                        }    

                        writer.write("\n");              
                    }

                }else if ("4".equals(args[1])) {
                    System.out.println("enter 4");
                    String stockName = args[2];
                    int start = Integer.parseInt(args[3]); // Assuming start index is the second argument
                    int end = Integer.parseInt(args[4]); // Assuming end index is the third argument
                    Map<String, List<Double>> stockPrices = DataCollector.loadData(filePath);
                    double stdDev []= LinearRegression.calculateCoefficientsForStock(stockPrices, stockName, start, end);
                    for (int i = 0; i < stdDev.length; i++) {
                        System.out.println("before" + stdDev[i]);
                        stdDev[i] = myMath.round2f(stdDev[i]);
                        System.out.println("after"+ stdDev[i]);
                    }
                    try(FileWriter writer = new FileWriter(outputFilePath,true)){
                        File filenew = new File(outputFilePath);
                        DecimalFormat df = new DecimalFormat("#.##");
                        writer.write(stockName + "," + start + "," + end + "\n");
                        writer.write(df.format(stdDev[1]) + ",");
                        writer.write(df.format(stdDev[0]) + "\n");
                        
                    }catch (IOException e) {
                        System.err.println("An error occurred while writing to the file.");
                        e.printStackTrace();
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Error parsing integer from arguments.");
                        e.printStackTrace();
                    }
                    catch (Exception e) {
                        System.err.println("An unexpected error occurred.");
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("error");
                }


            } else {
                System.out.println("error");
            }
            
            } catch (IOException e) {
                e.printStackTrace();
        }

    }

}
