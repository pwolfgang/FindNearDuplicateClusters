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
import java.util.StringJoiner;

/**
 * Class to represent a set of disjoint sets. When first constructed, the set
 * consists of singletons. A disjoint set is immutable in that after construction
 * no elements may be added or removed. However, elements may be merged.
 * This data structure is described by
 * <a href="https://mitpress.mit.edu/books/introduction-algorithms-third-edition">
 * Cormen </a>
 *
 * @author Paul Wolfgang
 * @param <E> The element type.
 */
public class DisjointSet<E> extends AbstractSet<Set<E>> {

    /**
     * Class to represent the nodes of the disjoint set trees
     */
    private static class Node<E> {

        private final E item;
        private Node<E> parent;
        private final List<Node<E>> children;
        private int rank;

        public Node(E e) {
            item = e;
            parent = this;
            children = new ArrayList<>();
            rank = 0;
        }
    }

    /**
     * Find the tree that contains this element.
     * @param x The element being sought.
     * @return The root Node of the tree containing this element.
     */
    private Node<E> findSet(Node<E> x) {
        if (x != x.parent) {
            Node<E> p = findSet(x.parent);
            x.parent.children.remove(x);
            p.children.add(x);
            x.parent = p;
        }
        return x.parent;
    }

    /**
     * Merge two sets.
     * @param x The root element of one set.
     * @param y The root element of the other set.
     */
    private void link(Node<E> x, Node<E> y) {
        if (x == y) {
            return;
        }
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

    /**
     * Map between elements of the disjoint set an their nodes within the
     * disjoint tree forest.
     */
    private final Map<E, Node<E>> theMap = new HashMap<>();

    /**
     * The set of roots to the disjoint set trees
     */
    private final Set<Node<E>> theSet = new HashSet<>();

    /**
     * Return the size of the disjoint Set
     *
     * @return The size of the disjoint set
     */
    @Override
    public int size() {
        return theSet.size();
    }

    /**
     * Return the number of disjoint sets
     *
     * @return the number of disjoint sets
     */
    public int getNumElements() {
        return theMap.size();
    }

    /**
     * Create a new DisjointSet from an existing collection
     *
     * @param c The collection to initialize the set.
     */
    public DisjointSet(Collection<E> c) {
        c.forEach(e -> {
            Node<E> n = new Node<>(e);
            theMap.put(e, n);
            theSet.add(n);
        });
    }

    /**
     * Creating an empty DisjointSet is prohibited
     */
    private DisjointSet() {
        throw new UnsupportedOperationException("Creating an empty Disjoint Set");
    }

    /**
     * Convert a DisjointSet Tree into a Set
     */
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
        public boolean hasNext() {
            return internalIterator.hasNext();
        }

        @Override
        public Set<E> next() {
            return toSet(internalIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Return an iterator to the disjoint sets
     *
     * @return an iterator to the disjoint sets
     */
    @Override
    public Iterator<Set<E>> iterator() {
        return new DisjointSetIterator(theSet);
    }

    /**
     * Add is not a supported operation.
     *
     * @param e Element to be added.
     * @return Does not return
     * @throws UnsupportedOperationException is always thrown.
     */
    @Override
    public boolean add(Set<E> e) {
        throw new UnsupportedOperationException("Add not supported");
    }

    /**
     * Remove is not a supported operation.
     *
     * @param o The object to be removed
     * @return Does not return a value
     * @throws UnsupportedOperationException is always thrown.
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Remove not supported");
    }

    /**
     * Returns true of this Set contains a specified element
     *
     * @param o The element to be sought
     * @return true if the set contains o
     */
    @Override
    public boolean contains(Object o) {
        return theMap.keySet().contains(o);
    }

    /**
     * Form the union of two disjoint sets
     *
     * @param e1 An element in the first set
     * @param e2 An element in the second set
     */
    public void union(E e1, E e2) {
        Node<E> n1 = findSet(theMap.get(e1));
        Node<E> n2 = findSet(theMap.get(e2));
        link(n1, n2);
    }

    /**
     * Return a string representation.
     *
     * @return The elements as sets separated by commas, enclosed in { .. }
     */
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        this.forEach(e -> sj.add(e.toString()));
        return sj.toString();
    }

}
