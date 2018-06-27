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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to represent a set of disjoint set.
 * Initially the set consists of singletons.
 * A disjoint set may be found by selecting any member.
 * Two disjoint sets may be merged.
 * @author Paul Wolfgang
 * @param <E> The element type.
 */
public class DisjointSet<E> extends AbstractSet<E> {
    
    /** Class to represent the nodes of the disjoint set trees */
    private static class Node<E> {
        private final E item;
        private Node<E> parent;
        private final List<Node<E>> children;
        private int rank;
        public Node (E e) {
            item = e;
            parent = this;
            children = new ArrayList<>();
            rank = 0;
        }
    }
    
    private Node<E> findSet(Node<E> x) {
        if (x != x.parent) {
            Node<E> p = findSet(x.parent);
            x.parent.children.remove(x);
            p.children.add(x);
            x.parent = p;
        }
        return x.parent;
    }
    
    private void link(Node<E> x, Node<E> y) {
        if (x == y) return;
        if (x.rank > y.rank) {
            x.children.add(y);
            y.parent = x;
            theSet.remove(y);
        } else {
            y.children.add(x);
            x.parent = y;
            if (x.rank == y.rank) {
                y.rank++;
            }
            theSet.remove(x);
        }
    }
    
    /** Map between elements of the disjoint set an their nodes within
     *  the disjoint tree forest.
     */
    private final Map<E, Node<E>> theMap = new HashMap<>();
    
    /** The set of roots to the disjoint set trees */
    private final Set<Node<E>> theSet = new HashSet<>();
    
    /** Return the size of the disjoint Set
     * @return  The size of the disjoint set
     */
    @Override
    public int size() {return theMap.size();}
    
    /** Return the number of disjoint sets
     * @return the number of disjoint sets 
     */
    public int getNumSets() {
        return theSet.size();
    }

    /** Return an iterator
     * @return  an iterator to the contents of the set
     */
    @Override
    public Iterator<E> iterator() {return theMap.keySet().iterator();}
    
    
    /** Create a new DisjointSet from an existing collection
     * @param c The collection to initialize the set.
     */
    public DisjointSet(Collection<E> c) {
        c.forEach(e -> {
            Node<E> n = new Node<>(e);
            theMap.put(e, n);
            theSet.add(n);
        });
    }
    
    /** Creating an empty DisjointSet is prohibited */
    private DisjointSet() {
        throw new UnsupportedOperationException("Creating an empty Disjoint Set");
    }
    
    /** Convert a DisjointSet Tree into a Set */
    private Set<E> toSet(Node<E> n) {
        Set<E> result = new HashSet<>();
        toSet(result, n);
        return result;
    }
    
    private void toSet(Set<E> s, Node<E> n) {
        s.add(n.item);
        n.children.forEach(child -> toSet(s, child));
    }
    
    private class DisjointSetIterator implements Iterator<Set<E>> {
        private final Iterator<Node<E>> internalIterator;
        public DisjointSetIterator(Set<Node<E>> roots) {
            internalIterator = roots.iterator();
        }
             
        @Override
        public boolean hasNext() {return internalIterator.hasNext();}
        
        @Override
        public Set<E> next() {return toSet(internalIterator.next());}
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /** Return an iterator to the disjoint sets
     * @return an iterator to the disjoint sets
     */
    public Iterator<Set<E>> setIterator() {
        return new DisjointSetIterator(theSet);
    }
    
    /** Add is not a supported operation */
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Add not supported");
    }
    
    /** Remove is not a supported operation */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Remove not supported");
    }
    
    /** Returns true of this Set contains a specified element
     * @param o The element to be sought
     * @return  true if the set contains o
     */
    @Override
    public boolean contains (Object o) {
        return theMap.keySet().contains(o);
    }
    
    /** Form the union of two disjoint sets
     * @param e1 An element in the first set
     * @param e2 An element in the second set
     */
    public void union(E e1, E e2) {
        Node<E> n1 = findSet(theMap.get(e1));
        Node<E> n2 = findSet(theMap.get(e2));
        link(n1, n2);
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("{");
        Iterator<Set<E>> itr = setIterator();
        if (itr.hasNext()) {
            stb.append(itr.next().toString());
        }
        while (itr.hasNext()) {
            stb.append(", ");
            stb.append(itr.next().toString());
        }
        stb.append("}");
        return stb.toString();
    }
    
}
