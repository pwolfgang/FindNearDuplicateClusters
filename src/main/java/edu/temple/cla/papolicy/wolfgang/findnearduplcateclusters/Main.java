/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Program to find near duplicates within a set of text records.
 * @author Paul Wolfgang
 */
public class Main {

    static CommandOption.File dataSourceFileName =
            new CommandOption.File(
            Main.class,
            "datasource",
            "FILE",
            true,
            null,
            "File containing the datasource properties",
            null);
    static CommandOption.String tableName =
            new CommandOption.String(
            Main.class,
            "table_name",
            "STRING",
            true,
            null,
            "Table containing the test data",
            null);
    static CommandOption.String idColumn =
            new CommandOption.String(
            Main.class,
            "id_column",
            "STRING",
            true,
            null,
            "Column(s) containing the ID",
            null);
    static CommandOption.String textColumn =
            new CommandOption.String(
            Main.class,
            "text_column",
            "STRING",
            true,
            null,
            "Column(s) containing the text",
            null);
    static CommandOption.String codeColumn =
            new CommandOption.String(
            Main.class,
            "code_column",
            "STRING",
            true,
            null,
            "Columns(s) containing the code",
            null);
    
    static CommandOption.Double threshold =
            new CommandOption.Double(Main.class,
            "threshold",
            "DOUBLE",
            false,
            0.7,
            "The clustering threshold",
            null);

    static CommandOption.Integer maxClusters =
            new CommandOption.Integer(Main.class,
            "max_clusters",
            "INTEGER",
            false,
            0,
            "The maximum number of clusters to be computed",
            null);

    static CommandOption.String clusterColumn =
            new CommandOption.String(Main.class,
            "cluster_column",
            "STRING",
            false,
            "Cluster",
            "The column containing the cluster flag",
            null);

    static CommandOption.String output =
            new CommandOption.String(Main.class,
            "output",
            "STRING",
            true,
            "",
            "The name of the output file",
            null);
    
    static CommandOption.String targetSession =
            new CommandOption.String(Main.class,
                    "target_session",
                    "STRING",
                    false,
                    "",
                    "The start year of the session just coded",
                    null);

    public static void main(String[] args) throws Exception {
        CommandOption.setSummary(Main.class,
                "A tool finding near duplicates");
        CommandOption.process(Main.class, args);
        if (args.length == 0) {
            CommandOption.getList(Main.class).printUsage(false);
            System.exit(-1);
        }

        List<String> ids = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        List<Integer> cluster = new ArrayList<>();
        List<SortedMap<Integer, Double>> attributes = new ArrayList<>();
        List<List<String>> text = new ArrayList<>();
        List<WordCounter> counts = new ArrayList<>();
        Vocabulary vocabulary = new Vocabulary();
        Util.readFromDatabase(dataSourceFileName.value().getPath(),
            tableName.value(), idColumn.value(), textColumn.value(), 
            codeColumn.value(), clusterColumn.value(), false, false,  false,
                ids, lines, codes, cluster);
        System.out.println("Done reading data");
        Preprocessor preprocessor = new Preprocessor("porter", "true");
        lines.forEach(line -> {
            List<String> processedLine = preprocessor.preprocess(line);
            text.add(processedLine);
        });
        System.out.println("Done preprocessing");
        text.forEach((words) -> {
            WordCounter counter = new WordCounter();
            counter.updateCounts(words);
            vocabulary.updateCounts(words);
            counts.add(counter);
        });
        vocabulary.computeProbabilities();
        System.out.println("Prepared vocabulary");
        counts.forEach((counter) -> {
            attributes.add(Util.computeAttributes(counter, vocabulary, 0.0));
        });
        System.out.println("Done computing attributes");
        // Build set of near duplicate pairs
        int numRows = attributes.size();
        double[] n = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            n[i] = Math.sqrt(Util.innerProduct(attributes.get(i), attributes.get(i)));
        }
        SortedSet<DoubleIntInt> s = new TreeSet<>(DoubleIntInt.reverseComparator());
        System.out.println("Begin Computing Dot Products");
        long totalIterations = ((long)numRows * (long)numRows - (long)numRows)/2;
        long iterCount = 0;
        long startTime = System.currentTimeMillis();
        double percentComplete;
        double reportThreshold = 1.0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < i; j++) {
                double ip = Util.innerProduct(attributes.get(i), attributes.get(j));
                ip = ip / (n[i] * n[j]);
                if (ip >= threshold.value() || maxClusters.value() > 0)
                    s.add(new DoubleIntInt(ip, i, j));
                ++iterCount;
                percentComplete = (double)iterCount / (double) totalIterations * 100.0;
                if (percentComplete > reportThreshold) {
                    long currentTime = System.currentTimeMillis();
                    long deltaTime = currentTime - startTime;
                    double rate = (double)iterCount/(double)deltaTime;
                    long estRemainingTime = (long)((totalIterations - iterCount) / rate);
                    long estMinutes = estRemainingTime / 60000;
                    long estSec = (estRemainingTime - estMinutes * 60000)/1000;
                    System.out.printf("%.0f Percent Complete %f items/sec %d total remaining%n",
                            percentComplete, rate * 100, (totalIterations - iterCount));
                    System.out.printf("%d minutes and %d sec estimated remaining%n", estMinutes, estSec);
                    reportThreshold += 1.0;
                }
            }
        }
        System.out.println("Done Computing Dot Products");
        Integer[] indices = new Integer[numRows];
        for (int i = 0; i < numRows; i++)
            indices[i] = i;
        DisjointSet<Integer> d = new DisjointSet<>(Arrays.asList(indices));
        int numSets;
        for (DoubleIntInt dii : s) {
            Integer x = dii.getX();
            Integer y = dii.getY();
            d.union(x, y);
            numSets = d.getNumSets();
            if (numSets <= maxClusters.value()) break;
        }
        System.out.println("Done Computing Clusters");
        List<List<Integer>> clusters = filterClusters(d, ids, codes);
        int clusterCount = clusters.size();
        for (int i = 0; i < clusters.size(); i++) {
            List<Integer> aCluster = clusters.get(i);
            for (int index : aCluster) {
                cluster.set(index, i);
            }
        }
        Util.updateClusterInDatabase(dataSourceFileName.value().getPath(),
            tableName.value(), idColumn.value(), clusterColumn.value(), ids, cluster);
        System.err.println(clusterCount + " total Clusters");
    }
    
    private static List<List<Integer>> filterClusters(DisjointSet<Integer> d, final List<String> ids, List<String> codes) {
        List<List<Integer>> clusters = new ArrayList<>();
        Iterator<Set<Integer>> setItr = d.setIterator();
        while (setItr.hasNext()) {
            Set<Integer> aCluster = setItr.next();
            if (aCluster.size() > 1 || maxClusters.value() > 0) {
                if (filterCluster(aCluster, ids, codes) || maxClusters.value() > 0) {
                    List<Integer> cluster = new ArrayList<>(aCluster);
                    Collections.sort(cluster, (Integer left, Integer right) -> ids.get(right).compareTo(ids.get(left)));
                    clusters.add(cluster);
                }
            }
        }
        return clusters;
    }

    private static boolean filterCluster(Set<Integer> aCluster, List<String> ids, List<String> codes) {
        boolean codesAllEqual = false;
        boolean containsTargetSession = false;
        String currentCode = null;
        for (Integer index : aCluster) {
            String id = ids.get(index);
            containsTargetSession |= id.startsWith(targetSession.value());
            String thisCode = codes.get(index);
            if (currentCode == null) {
                currentCode = thisCode;
                if (currentCode != null) codesAllEqual = true;
            } else if (!currentCode.equals(thisCode)) {
                codesAllEqual = false;
            }
        }
        return containsTargetSession && !codesAllEqual;
    }  
}