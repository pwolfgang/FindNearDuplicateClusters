package edu.temple.cla.papolicy.wolfgang.findnearduplicateclusters;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */




import edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters.DisjointSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Paul Wolfgang
 */
public class DisjointSetTest {
    
    /**
     * Test of size method, of class DisjointSet.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<Integer>(Arrays.asList(theArray));
        int expResult = 5;
        int result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of iterator method, of class DisjointSet.
     */
    @Test
    public void testIterator() {
        System.out.println("iterator");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        Iterator<Integer> result = instance.iterator();
        Set<Integer> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Integer> expectedContents = 
                new HashSet<>(Arrays.asList(theArray));
        assertEquals(contents, expectedContents);
    }

    /**
     * Test of setIterator method, of class DisjointSet.
     */
    @Test
    public void testSetIterator() {
        System.out.println("setIterator");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        Iterator<Set<Integer>> result = instance.setIterator();
        Set<Set<Integer>> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Set<Integer>> expectedContents = new HashSet<>();
        for (Integer i: theArray) {
            Set<Integer> s = new HashSet<>();
            s.add(i);
            expectedContents.add(s);
        }
        assertEquals(contents, expectedContents);
    }
    
    /**
     * Internal method to generate expected result
     */
    private Set<Set<Integer>> expectedResult(Integer[][] a) {
        HashSet<Set<Integer>> result = new HashSet<>();
        for (Integer[] a1 : a) {
            Set<Integer> s = new HashSet<>();
            s.addAll(Arrays.asList(a1));
            result.add(s);
        }
        return result;
    }
    
    /**
     * Test 1 of union method
     */
    @Test
    public void testUnion1() {
        System.out.println("union test 1");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        instance.union(1, 3);
        Iterator<Set<Integer>> result = instance.setIterator();
        Set<Set<Integer>> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Set<Integer>> expectedContents = 
                expectedResult(new Integer[][]{{1,3},{2},{4},{5}});
        assertEquals(contents, expectedContents);
    }

    /**
     * Test 2 of union method
     */
    @Test
    public void testUnion2() {
        System.out.println("union test 2");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        instance.union(1, 3);
        instance.union(3, 5);
        Iterator<Set<Integer>> result = instance.setIterator();
        Set<Set<Integer>> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Set<Integer>> expectedContents = 
                expectedResult(new Integer[][]{{1,3,5},{2},{4}});
        assertEquals(contents, expectedContents);
    }
    
    /**
     * Test of add method, of class DisjointSet.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        boolean exceptionThrown = false;
        try {
            instance.add(0);
        } catch (UnsupportedOperationException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * Test of remove method, of class DisjointSet.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        boolean exceptionThrown = false;
        try {
            instance.remove(0);
        } catch (UnsupportedOperationException ex) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

}