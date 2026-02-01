import ai.onnxruntime.*;
import com.opencsv.CSVReader;
import org.apache.commons.math3.ml.clustering.*;
import java.io.*;
import java.util.*;

class CosineDistance {
    public double compute(double[] v1, double[] v2) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        if (normA == 0 || normB == 0) return 1.0;
        return 1.0 - (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}

public class PatentTopicModel {

    static int NUM_TOPICS = 10;
    static int MAX_LEN = 128;

    public static void main(String[] args) {
        try {
            List<String> abstracts = loadAbstracts(
                    "C:\\Users\\YUVARAAJ\\OneDrive\\Documents\\sem 5 mini project\\abstractf.csv"
            );
            System.out.println("Total abstracts: " + abstracts.size());
            NUM_TOPICS = Math.max(2, (int) Math.sqrt(abstracts.size()));
            System.out.println("Auto-selected topics: " + NUM_TOPICS);
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            OrtSession session = env.createSession(
                    "C:\\Users\\YUVARAAJ\\IdeaProjects\\bertkkr\\src\\main\\resources\\patentsberta-onnx\\model.onnx",
                    opts
            );
            System.out.println("ONNX model loaded!");
            Map<String, Integer> vocab = loadVocab(
                    "C:\\Users\\YUVARAAJ\\IdeaProjects\\bertkkr\\src\\main\\resources\\patentsberta-onnx\\vocab.txt"
            );
            List<double[]> embeddings = new ArrayList<>();
            for (String abs : abstracts) {
                embeddings.add(generateEmbedding(session, env, abs, vocab, MAX_LEN));
            }
            List<Clusterable> points = new ArrayList<>();
            for (double[] emb : embeddings) points.add(new DoublePoint(emb));
            KMeansPlusPlusClusterer<Clusterable> clusterer = new KMeansPlusPlusClusterer<>(NUM_TOPICS, 300);
            List<CentroidCluster<Clusterable>> clusters = clusterer.cluster(points);
            Map<Integer, List<Integer>> topicAssignments = new HashMap<>();
            for (int i = 0; i < NUM_TOPICS; i++) topicAssignments.put(i, new ArrayList<>());
            CosineDistance dist = new CosineDistance();
            for (int i = 0; i < embeddings.size(); i++) {
                double minDist = Double.MAX_VALUE;
                int topicId = -1;
                for (int j = 0; j < clusters.size(); j++) {
                    double d = dist.compute(embeddings.get(i), clusters.get(j).getCenter().getPoint());
                    if (d < minDist) {
                        minDist = d;
                        topicId = j;
                    }
                }
                topicAssignments.get(topicId).add(i);
            }
            String outputPath = "C:\\Users\\YUVARAAJ\\OneDrive\\Documents\\sem 5 mini project\\Topics_final.txt";
            PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
            for (int t = 0; t < NUM_TOPICS; t++) {
                List<Integer> docs = topicAssignments.get(t);
                Map<String, Integer> freq = new HashMap<>();
                for (int idx : docs) {
                    String[] words = abstracts.get(idx).toLowerCase().split("\\W+");
                    for (String w : words) if (w.length() > 3) freq.put(w, freq.getOrDefault(w, 0) + 1);
                }
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freq.entrySet());
                sorted.sort((a, b) -> b.getValue() - a.getValue());
                List<String> topWords = new ArrayList<>();
                for (int i = 0; i < Math.min(5, sorted.size()); i++) topWords.add(sorted.get(i).getKey());
                double coherence = computeCoherence(topWords, session, env, vocab, MAX_LEN);
                writer.println("=== Topic " + t + " ===");
                writer.println("Keywords: " + String.join(", ", topWords));
                writer.println("Documents: " + docs.size());
                writer.println("Coherence Score: " + String.format("%.2f", coherence));
                writer.println();
            }
            writer.close();
            System.out.println("Results saved at: " + outputPath);
            session.close();
            env.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static List<String> loadAbstracts(String path) throws Exception {
        List<String> list = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] line;
            reader.readNext(); // skip header
            while ((line = reader.readNext()) != null) {
                if (line.length >= 3 && !line[2].trim().isEmpty()) list.add(line[2]);
            }
        }
        return list;
    }
    private static Map<String, Integer> loadVocab(String path) throws Exception {
        Map<String, Integer> vocab = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int idx = 0;
        while ((line = br.readLine()) != null) {
            vocab.put(line.trim(), idx++);
        }
        br.close();
        return vocab;
    }
    private static double[] generateEmbedding(OrtSession session, OrtEnvironment env, String text,
                                              Map<String, Integer> vocab, int maxLen) throws OrtException {
        long[][] inputIds = new long[1][maxLen];
        long[][] attentionMask = new long[1][maxLen];
        String[] tokens = text.toLowerCase().split("\\s+");
        for (int i = 0; i < Math.min(tokens.length, maxLen); i++) {
            inputIds[0][i] = vocab.getOrDefault(tokens[i], vocab.getOrDefault("[UNK]", 0));
            attentionMask[0][i] = 1;
        }
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input_ids", OnnxTensor.createTensor(env, inputIds));
        inputs.put("attention_mask", OnnxTensor.createTensor(env, attentionMask));
        OrtSession.Result res = session.run(inputs);
        float[][][] allEmb = (float[][][]) res.get(0).getValue();
        float[] clsEmb = allEmb[0][0]; 
        double[] vec = new double[clsEmb.length];
        double norm = 0;
        for (int i = 0; i < vec.length; i++) {
            vec[i] = clsEmb[i];
            norm += vec[i] * vec[i];
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < vec.length; i++) vec[i] /= norm;
        res.close();
        return vec;
    }
    private static double computeCoherence(List<String> words, OrtSession session, OrtEnvironment env,
                                           Map<String, Integer> vocab, int maxLen) throws OrtException {
        if (words.size() < 2) return 0.0;
        List<double[]> embeddings = new ArrayList<>();
        for (String w : words) embeddings.add(generateEmbedding(session, env, w, vocab, maxLen));
        CosineDistance dist = new CosineDistance();
        double sum = 0;
        int count = 0;
        for (int i = 0; i < embeddings.size(); i++) {
            for (int j = i + 1; j < embeddings.size(); j++) {
                sum += 1.0 - dist.compute(embeddings.get(i), embeddings.get(j));
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
}
