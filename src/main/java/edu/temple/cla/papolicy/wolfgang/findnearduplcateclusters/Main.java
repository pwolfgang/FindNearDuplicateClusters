/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters;

import edu.temple.cla.papolicy.wolfgang.texttools.util.Preprocessor;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Util;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Vocabulary;
import edu.temple.cla.papolicy.wolfgang.texttools.util.WordCounter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Program to find near duplicates within a set of text records.
 *
 * @author Paul Wolfgang
 */
public class Main implements Callable<Void> {

    private final String[] args;

    @CommandLine.Option(names = "--datasource", required = true,
            description = "File containing the datasource properties")
    private String dataSourceFileName;

    @CommandLine.Option(names = "--table_name", required = true,
            description = "The name of the table containing the data")
    private String tableName;

    @CommandLine.Option(names = "--id_column", required = true,
            description = "Column(s) containing the ID")
    private String idColumn;

    @CommandLine.Option(names = "--text_column", required = true,
            description = "Column(s) containing the text")
    private String textColumn;

    @CommandLine.Option(names = "--code_column", required = true,
            description = "Column(s) containing the code")
    private String codeColumn;

    @CommandLine.Option(names = "--output_table", required = true,
            description = "Table where the cluster id's are written")
    private String outputTable;

    @CommandLine.Option(names = "--cluster_column", required = true,
            description = "Column containing the cluster flag")
    private String clusterColumn;

    @CommandLine.Option(names = "--remove_stopwords", arity = "0..1",
            description = "Remove common \"stop words\" from the text.")
    private String removeStopWords;

    @CommandLine.Option(names = "--do_stemming", arity = "0..1",
            description = "Pass all words through stemming algorithm")
    private String doStemming;

    @CommandLine.Option(names = "--threshold",
            description = "Minimum value of dot product to consider items to be nearly duplicates")
    private double threshold = 0.7;

    @CommandLine.Option(names = "--max_clusters",
            description = "The maximum number of clusters to be computed")
    private int maxClusters = 0;

    @CommandLine.Option(names = "--output", required = true,
            description = "The output file name")
    private String output;

    @CommandLine.Option(names = "--target_session",
            description = "The start year of the session just coded")
    private String targetSession;

    /**
     * Constructor.
     *
     * @param args The command-line arguments.
     */
    public Main(String[] args) {
        this.args = args;
    }

    /**
     * The main entry point.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main(args);
        CommandLine commandLine = new CommandLine(main);
        commandLine.setUnmatchedArgumentsAllowed(true).parse(args);
        try {
            main.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Void call() {
        List<String> ids = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        List<SortedMap<Integer, Double>> attributes = new ArrayList<>();
        List<WordCounter> counts = new ArrayList<>();
        Vocabulary vocabulary = new Vocabulary();
        Preprocessor preprocessor = new Preprocessor("porter", "true");
        Util.readFromDatabase(dataSourceFileName,
                tableName,
                idColumn,
                textColumn,
                codeColumn,
                false,
                false,
                false,
                clusterColumn)
                .forEach(m -> {
                    ids.add((String) m.get("theID"));
                    codes.add(Objects.toString(m.get("theCode")));
                    String line = (String) m.get("theText");
                    WordCounter counter = new WordCounter();
                    preprocessor.preprocess(line)
                            .forEach(word -> {
                                counter.updateCounts(word);
                                vocabulary.updateCounts(word);
                            });
                    counts.add(counter);
                });
        vocabulary.computeProbabilities();
        counts.forEach((counter) -> {
            attributes.add(Util.computeAttributes(counter, vocabulary, 0.0));
        });
        // Build set of near duplicate pairs
        int numRows = attributes.size();
        double[] n = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            n[i] = Math.sqrt(Util.innerProduct(attributes.get(i), attributes.get(i)));
        }
        Integer[] indices = new Integer[numRows];
        for (int i = 0; i < numRows; i++) {
            indices[i] = i;
        }

        DisjointSet<Integer> d = new DisjointSet<>(Arrays.asList(indices));
        System.out.println("Begin Computing Dot Products");
        long totalIterations = ((long) numRows * (long) numRows - (long) numRows) / 2;
        long iterCount = 0;
        long startTime = System.currentTimeMillis();
        double percentComplete;
        double reportThreshold = 1.0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < i; j++) {
                double ip = Util.innerProduct(attributes.get(i), attributes.get(j));
                ip = ip / (n[i] * n[j]);
                if (ip >= threshold || maxClusters > 0) {
                    d.union(i, j);
                }
                ++iterCount;
                percentComplete = (double) iterCount / (double) totalIterations * 100.0;
                if (percentComplete > reportThreshold) {
                    long currentTime = System.currentTimeMillis();
                    long deltaTime = currentTime - startTime;
                    double rate = (double) iterCount / (double) deltaTime;
                    long estRemainingTime = (long) ((totalIterations - iterCount) / rate);
                    long estMinutes = estRemainingTime / 60000;
                    long estSec = (estRemainingTime - estMinutes * 60000) / 1000;
                    System.out.printf("%.0f Percent Complete %f items/sec %d total remaining%n",
                            percentComplete, rate * 100, (totalIterations - iterCount));
                    System.out.printf("%d minutes and %d sec estimated remaining%n", estMinutes, estSec);
                    reportThreshold += 1.0;
                }
            }
        }
        List<List<Integer>> clusters = filterClusters(d, ids, codes);
        int clusterCount = clusters.size();
        Integer[] cluster = new Integer[numRows];
        for (int i = 0; i < clusters.size(); i++) {
            List<Integer> aCluster = clusters.get(i);
            for (int index : aCluster) {
                cluster[index] = i;
            }
        }
        Util.updateClusterInDatabase(dataSourceFileName,
                outputTable, idColumn, clusterColumn, ids, Arrays.asList(cluster));
        System.err.println(clusterCount + " total Clusters");
        return null;
    }

    private List<List<Integer>> filterClusters(DisjointSet<Integer> d, final List<String> ids, List<String> codes) {
        List<List<Integer>> clusters = new ArrayList<>();
        Iterator<Set<Integer>> setItr = d.setIterator();
        while (setItr.hasNext()) {
            Set<Integer> aCluster = setItr.next();
            if (aCluster.size() > 1 || maxClusters > 0) {
                if (filterCluster(aCluster, ids, codes) || maxClusters > 0) {
                    List<Integer> cluster = new ArrayList<>(aCluster);
                    Collections.sort(cluster, (Integer left, Integer right) -> ids.get(right).compareTo(ids.get(left)));
                    clusters.add(cluster);
                }
            }
        }
        return clusters;
    }

    private boolean filterCluster(Set<Integer> aCluster, List<String> ids, List<String> codes) {
        boolean codesAllEqual = false;
        boolean containsTargetSession = false;
        String currentCode = null;
        for (Integer index : aCluster) {
            String id = ids.get(index);
            containsTargetSession |= id.startsWith(targetSession);
            String thisCode = codes.get(index);
            if (currentCode == null) {
                currentCode = thisCode;
                if (currentCode != null) {
                    codesAllEqual = true;
                }
            } else if (!currentCode.equals(thisCode)) {
                codesAllEqual = false;
            }
        }
        return containsTargetSession && !codesAllEqual;
    }
}
