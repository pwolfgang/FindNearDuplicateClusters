package edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters;

import edu.temple.cis.wolfgang.util.Util;
import java.io.PrintWriter;
import java.util.List;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;

public class DisplayClustersInTable {

    public static void printHTMLHeader(PrintWriter out, String title) {
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
        out.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
    }

    public static void processCluster(List<String> cluster, PrintWriter out) {
        if (cluster.isEmpty()) {
            return;
        }
        String[] firstRow = Util.convertToXML(cluster.get(0)).split("\\|");
        out.println("<tr>");
        for (String col : firstRow) {
            out.println("<td>" + col + "</td>");
        }
        out.println("</tr>");
        String[] list1 = firstRow[1].split("\\s+");
        for (int i = 1; i < cluster.size(); i++) {
            String[] currentRow = cluster.get(i).split("\\|");
            String[] list2 = Util.convertToXML(currentRow[1]).split("\\s+");
            String diffString = genDiffString(list1, list2);
            out.println("<tr>");
            for (int j = 0; j < currentRow.length; j++) {
                if (j != 1) {
                    out.println("<td>" + currentRow[j] + "</td>");
                } else {
                    out.println("<td>" + diffString + "</td>");
                }
            }
            out.println("</tr>");
        }
        out.println("<tr><td colspan=\"4\" bgcolor=\"#ffff00\">&nbsp</td></tr>");
    }

    public static void printTableFooter(PrintWriter out) {
        out.print("</table>\r\n</body>\r\n</html>\r\n");
    }

    public static void printTableHeader(PrintWriter out) {
        out.println("<body>");
        out.println("<table border=\"1\">");
        out.println("<tr>");
        out.println("<th>Bill</th>");
        out.println("<th>Title</th>");
        out.println("<th>Code</th>");
        out.println("<th>Cluster</th>");
        out.println("</tr>");
    }

    public static String genDiffString(String[] list1, String[] list2) {
        List<Difference> diffs = new Diff<>(list1, list2).diff();
        int index = 0;
        StringBuilder stb = new StringBuilder();
        for (Difference diff : diffs) {
            int deletedStart = diff.getDeletedStart();
            int deletedEnd = diff.getDeletedEnd();
            int addedStart = diff.getAddedStart();
            int addedEnd = diff.getAddedEnd();
            while (index < deletedStart) {
                stb.append(list1[index]);
                stb.append(" ");
                index++;
            }
            if (deletedEnd != Difference.NONE) {
                stb.append("<strike>");
                while (index <= deletedEnd) {
                    stb.append(list1[index]);
                    stb.append(" ");
                    index++;
                }
                stb.delete(stb.length() - 1, stb.length());
                stb.append("</strike>");
                stb.append(" ");
            }
            if (addedEnd != Difference.NONE) {
                stb.append("<b>");
                for (int k = addedStart; k <= addedEnd; k++) {
                    stb.append(list2[k]);
                    stb.append(" ");
                }
                stb.delete(stb.length() - 1, stb.length());
                stb.append("</b>");
                stb.append(" ");
            }
        }
        while (index < list1.length) {
            stb.append(list1[index++]);
            stb.append(" ");
        }
        stb.delete(stb.length() - 1, stb.length());
        return stb.toString();
    }

}
