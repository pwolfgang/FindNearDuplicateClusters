/* 
 * Copyright (c) 2018, Temple University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * All advertising materials features or use of this software must display 
 *   the following  acknowledgement
 *   This product includes software developed by Temple University
 * * Neither the name of the copyright holder nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters;

import edu.temple.cla.papolicy.wolfgang.texttools.util.CommonFrontEnd;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Preprocessor;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Util;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Vocabulary;
import edu.temple.cla.papolicy.wolfgang.texttools.util.WordCounter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @CommandLine.Option(names = "--target_session", required = true,
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

    @Override
    public Void call() {
        List<Map<String, Object>> cases = new ArrayList<>();
        CommonFrontEnd commonFrontEnd = new CommonFrontEnd();
        CommandLine commandLine = new CommandLine(commonFrontEnd);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.parse(args);
        Vocabulary vocabulary = commonFrontEnd.loadData(cases);
        List<SortedMap<Integer, Double>> attributes = new ArrayList<>(); 
        cases.forEach((trainingCase) -> {
            WordCounter counter = (WordCounter)trainingCase.get("counts");
            SortedMap<Integer, Double> attributeSet = Util.computeAttributes(counter, vocabulary, 0.0);
            attributes.add(attributeSet);
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
                    reportThreshold += 10.0;
                }
            }
        }
        System.out.println("Done Computing dot products");
        System.out.println("Filtering clusters");
        List<List<Integer>> clusters = filterClusters(d, cases);
        System.out.println("Assigning cluster ID");
        int clusterCount = clusters.size();
        for (int i = 0; i < clusters.size(); i++) {
            List<Integer> aCluster = clusters.get(i);
            for (int index : aCluster) {
                cases.get(index).put("ClusterId", i);
            }
        }
        System.out.println("Updating Database");
        commonFrontEnd.outputToDatabase(outputTable, clusterColumn, cases, "ClusterId");
        System.out.println(clusterCount + " total Clusters");
        return null;
    }

    private List<List<Integer>> filterClusters(DisjointSet<Integer> d,
            final List<Map<String, Object>> cases) {
        List<List<Integer>> clusters = new ArrayList<>();
        d.stream().filter(aCluster -> (aCluster.size() > 1 || maxClusters > 0))
                .filter(aCluster -> (filterCluster(aCluster, cases) || maxClusters > 0))
                .forEach(aCluster -> clusters.add(new ArrayList<>(aCluster)));
        return clusters;
    }

    private boolean filterCluster(Set<Integer> aCluster, List<Map<String, Object>> cases) {
        boolean codesAllEqual = false;
        boolean containsTargetSession = false;
        String currentCode = null;
        for (Integer index : aCluster) {
            String id = cases.get(index).get("theID").toString();
            containsTargetSession |= id.startsWith(targetSession);
            String thisCode = Objects.toString(cases.get(index).get("theCode"));
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
