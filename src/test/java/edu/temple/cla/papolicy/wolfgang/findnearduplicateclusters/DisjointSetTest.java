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
package edu.temple.cla.papolicy.wolfgang.findnearduplicateclusters;
import edu.temple.cla.papolicy.wolfgang.findnearduplcateclusters.DisjointSet;
import java.util.Arrays;
import java.util.Collections;
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
    public void testSizeOfInitialSet() {
        System.out.println("initial size");
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(1, 2, 3, 4, 5));
        int expResult = 5;
        int result = instance.size();
        assertEquals(expResult, result);
    }
    
    /**
     * Test size after union.
     */
    @Test
    public void TestSizeAfterUntion() {
        System.out.println("size after union");
        DisjointSet<Integer> instance = 
            new DisjointSet<>(Arrays.asList(1, 2, 3, 4, 5));
        int expResult = 4;
        instance.union(1, 4);
        int result = instance.size();
        assertEquals(expResult, result);
}

    /**
     * Test of iterator method, of class DisjointSet.
     */
    @Test
    public void testIteratorOfInitialSet() {
        System.out.println("iteratorOfInitialSet");
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(1,2,3,4,5));
        Iterator<Set<Integer>> result = instance.iterator();
        Set<Set<Integer>> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Set<Integer>> expectedContents = expectedResult(new Integer[][] {{1},{2},{3},{4},{5}});
        assertEquals(contents, expectedContents);
    }

    /**
     * Test of iterator method, of class DisjointSet.
     */
    @Test
    public void testIteratorAfterUnion() {
        System.out.println("iteratorAfterUntion");
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(1,2,3,4,5));
        instance.union(3,4);
        Iterator<Set<Integer>> result = instance.iterator();
        Set<Set<Integer>> contents = new HashSet<>();
        while (result.hasNext()) {
            contents.add(result.next());
        }
        Set<Set<Integer>> expectedContents = expectedResult(new Integer[][] {{1},{2},{3,4},{5}});
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
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(1,2,3,4,5));
        instance.union(1, 3);
        Iterator<Set<Integer>> result = instance.iterator();
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
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(1,2,3,4,5));
        instance.union(1, 3);
        instance.union(3, 5);
        Iterator<Set<Integer>> result = instance.iterator();
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
    @Test (expected=UnsupportedOperationException.class)
    public void testAdd() {
        System.out.println("add");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        boolean exceptionThrown = false;
        instance.add(Collections.singleton(0));
    }

    /**
     * Test of remove method, of class DisjointSet.
     */
    @Test (expected=UnsupportedOperationException.class)
    public void testRemove() {
        System.out.println("remove");
        Integer[] theArray = {1,2,3,4,5};
        DisjointSet<Integer> instance = 
                new DisjointSet<>(Arrays.asList(theArray));
        instance.remove(Collections.singleton(2));
    }

}