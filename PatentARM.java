import com.opencsv.CSVReader;
import java.io.*;
import java.util.*;
public class PatentARM {
    public static void main(String[] args) {
        try {
            String path = "C:\\Users\\YUVARAAJ\\OneDrive\\Documents\\sem 5 mini project\\ipc_pairs.csv";
            List<String[]> records = loadCSV(path);
            Map<String, Set<String>> transactions = new HashMap<>();
            for (String[] row : records) {
                String patentId = row[0];
                transactions.putIfAbsent(patentId, new HashSet<>());
                transactions.get(patentId).add(row[1]);
                transactions.get(patentId).add(row[2]);
            }
            Map<String, Integer> itemCounts = new HashMap<>();
            Map<String, Integer> pairCounts = new HashMap<>();
            for (Set<String> ipcSet : transactions.values()) {
                List<String> list = new ArrayList<>(ipcSet);
                for (String ipc : list) itemCounts.put(ipc, itemCounts.getOrDefault(ipc, 0) + 1);
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i + 1; j < list.size(); j++) {
                        String key = list.get(i) + "->" + list.get(j);
                        pairCounts.put(key, pairCounts.getOrDefault(key, 0) + 1);
                    }
                }
            }
            int totalTransactions = transactions.size();
            Map<String, Double> convergenceIndicator = new HashMap<>();
            System.out.println("Rule\tSupport\tConfidence\tCI");
            for (String pair : pairCounts.keySet()) {
                String[] parts = pair.split("->");
                String A = parts[0];
                String B = parts[1];
                int countAB = pairCounts.get(pair);
                int countA = itemCounts.get(A);
                int countB = itemCounts.get(B);
                double support = (double) countAB / totalTransactions;
                double confidence = (double) countAB / countA;
                double ci = (double) countAB / (countA + countB - countAB); // CI = P(A ∩ B) / P(A ∪ B)
                convergenceIndicator.put(pair, ci);
                System.out.printf("%s -> %s\t%.3f\t%.3f\t%.3f%n", A, B, support, confidence, ci);
            }
            String networkPath = "C:\\Users\\KARTHIK RAHUL . K\\OneDrive\\Documents\\sem 5 mini project\\TC_Network.txt";
            PrintWriter writer = new PrintWriter(new FileWriter(networkPath));
            writer.println("NodeA\tNodeB\tCI");
            for (Map.Entry<String, Double> entry : convergenceIndicator.entrySet()) {
                String[] nodes = entry.getKey().split("->");
                writer.printf("%s\t%s\t%.3f%n", nodes[0], nodes[1], entry.getValue());
            }
            writer.close();
            System.out.println("Technology convergence network saved at: " + networkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static List<String[]> loadCSV(String path) throws Exception {
        List<String[]> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            reader.readNext(); 
            String[] line;
            while ((line = reader.readNext()) != null) data.add(line);
        }
        return data;
    }
}
