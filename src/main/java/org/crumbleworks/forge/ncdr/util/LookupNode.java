package org.crumbleworks.forge.ncdr.util;


import static org.crumbleworks.forge.ncdr.util.Parameters.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Allows creating simple lookup trees.
 * 
 * <p>A lookup tree starts with a single node at it's root. Then add keys as needed with {@link #put(String, Object)}.
 * 
 * <p>To get values call {@link #resolve(String, boolean)}.
 * 
 * @author Michael Stocker
 * 
 * @param <V> the type of the associated values
 */
public final class LookupNode<V> {
    private final Map<Character, LookupNode<V>> childNodes;

    private final Set<V> values;
    private TreeSet<V> possibleValues;
    
    public LookupNode() {
        this.values = new HashSet<>();
        childNodes = new HashMap<>(1, 1.00f);
    }
    
    public LookupNode(final V value) {
        this.values = new HashSet<>();
        this.values.add(notNull(value));
        childNodes = new HashMap<>(0, 1.00f);
    }
    
    /* **********************************************************************
     * OPERATIONS ON NODE
     */
    
    /**
     * Adds a value to this node.
     * 
     * @param value the new value to be added to this node
     */
    public final void addValue(final V value) {
        this.values.add(notNull(value));
    }
    
    /**
     * Gets the values for this node.
     * 
     * @return the values assigned to this node or <code>empty list</code> if no values were assigned
     */
    public final Set<V> getValues() {
        return values;
    }
    
    /**
     * Retrieves a set of all values that are further down the tree.
     * 
     * @return an unmodifiable set with all the values further down the tree.
     */
    public final Set<V> getPossibleValues() {
        synchronized(childNodes) {
            if(possibleValues == null) {
                TreeSet<V> values = new TreeSet<>();
                
                traverseNodesToCollectValues(values, this);
                
                possibleValues = values;
            }
            
            return Collections.unmodifiableSortedSet(possibleValues);
        }
    }
    
    /**
     * Traverses nodes and their children and adds any values found to the given set.
     * 
     * @param values the set to add every found values to
     * @param node the next node to traverse
     */
    private final void traverseNodesToCollectValues(final Set<V> values, final LookupNode<V> node) {
        synchronized(node.childNodes) {
            values.addAll(node.values);
            
            for(LookupNode<V> childNode : node.childNodes.values()) {
                traverseNodesToCollectValues(values, childNode);
            }
        }
    }
    
    /* **********************************************************************
     * OPERATIONS ON TREE
     */
    
    /**
     * Adds a new values.
     * 
     * <p>This method will chip off the first <code>char</code> of the given key and then pass it further down the path.
     * 
     * <p>This operation will force this node and any child-node further down the path of the given key to recalculate their sets of possible values.
     * 
     * @param key the key to be added
     * @param v the values to be added
     */
    public final void put(final String key, final V v) {
        if(key.length() == 0) {
            return;
        }
        
        synchronized(childNodes) {
            char c = notNull(key).charAt(0);
            
            if(key.length() == 1) {
                //reached last element of key
                if(childNodes.containsKey(c)) {
                    childNodes.get(c).addValue(v);
                } else {
                    childNodes.put(c, new LookupNode<>(v));
                }
            } else {
                //key has still more elements
                if(!childNodes.containsKey(c)) {
                    childNodes.put(c, new LookupNode<>());
                }
                
                childNodes.get(c).put(key.substring(1), v);
            }
            
            possibleValues = null;
        }
    }
    
    /**
     * Resolves the supplied key against this tree.
     * 
     * <p>If the <code>partial</code> flag is set, this method will treat the supplied key as a partial-key and thus try looking further along a straight path as described in {@link LookupNode#explore(char)}.
     * 
     * @param key the key or partial-key to be resolved
     * @param partial tells the method to explore further if the supplied key has no associated values
     * 
     * @return a {@link LookupResult} or <code>null</code> if the supplied key cannot be fully resolved
     */
    public final LookupResult<V> resolve(final String key, boolean partial) {
        synchronized(childNodes) {
            //we're technically abusing the lookup result here
            LookupResult<V> lookupResult = findNode(key, this);
            
            //if we found our node the key in the result should be 1 char long
            if(lookupResult != null && lookupResult.getKey().length() == 1) {
                if(partial) {
                    if(lookupResult.getNode().getPossibleValues().size() > 1
                    || (lookupResult.getNode().getPossibleValues().size() == 1 && !lookupResult.getNode().getValues().isEmpty())) {
                        return new LookupResult<>(key, lookupResult.getNode());
                    }
                    
                    return traverseNodesStraight(
                            new StringBuilder(lookupResult.getNode().childNodes.keySet().iterator().next()),
                            lookupResult.getNode().childNodes.values().iterator().next());
                }
                
                if(!lookupResult.getNode().getValues().isEmpty()) {
                    //got a values, no more exploring
                    return new LookupResult<>(key, lookupResult.getNode());
                }
            }
            
            //could not finish looking up key
            return null;
        }
    }
    
    /**
     * Traverses nodes until it either finds the node associated with the key or hits a dead-end.
     * 
     * @param key the key representing the path to the node
     * @param node the node on which to start looking
     * 
     * @return a {@link LookupResult} or <code>null</code> if the key cannot be found
     */
    private final LookupResult<V> findNode(final String key, final LookupNode<V> node) {
        synchronized(childNodes) {
            char c = notNull(key).charAt(0);
            
            if(node.childNodes.containsKey(c)) {
                if(key.length() == 1) {
                    //reached last element of key
                    return new LookupResult<>(key, node.childNodes.get(c));
                }
                
                //key has still more elements
                return findNode(
                        key.substring(1),
                        node.childNodes.get(c));
            }
            
            return null;
        }
    }
    
    /**
     * Traverses child-nodes starting with the given <code>char</code> until either:<BR>
     * - a child-node has a values assigned<BR>
     * - a child-node has multiple child-nodes
     * 
     * @param c the child-node from which to start searching
     * 
     * @return a {@link LookupResult} or <code>null</code> if there's no match along the branch
     */
    public final LookupResult<V> explore(char c) {
        synchronized(childNodes) {
            if(!childNodes.containsKey(c)) {
                //has no matching child-node > null
                return null;
            }
            
            return traverseNodesStraight(new StringBuilder(c), childNodes.get(c));
        }
    }
    
    /**
     * Traverses a straight line of nodes until either a dead-end or a fork.
     * 
     * @param s a {@link StringBuilder} containing the traversed key-segment
     * @param node the next node to check
     */
    private final LookupResult<V> traverseNodesStraight(final StringBuilder s, final LookupNode<V> node) {
        synchronized(childNodes) {
            if(!node.getValues().isEmpty()) {
                //has values
                return new LookupResult<>(s.toString(), node);
            }
            
            if(node.getPossibleValues().size() > 1) {
                //multiple branches
                return new LookupResult<>(s.toString(), node);
            }
            
            return traverseNodesStraight(
                    s.append(node.childNodes.keySet().iterator().next()),
                    node.childNodes.values().iterator().next());
        }
    }
    
    /* **********************************************************************
     * HELPERS
     */
    
    /**
     * Represents the result of a lookup, consisting of a {@link LookupNode} and a corresponding {@link String}.
     * 
     * @author Michael Stocker
     * 
     * @param <V> the type of the values of the lookup-node
     */
    public static final class LookupResult<V> {
        private final String key;
        private final LookupNode<V> node;
        
        private LookupResult(final String key, final LookupNode<V> node) {
            this.key = notNull(key);
            this.node = notNull(node);
        }
        
        public final String getKey() {
            return key;
        }
        
        public final LookupNode<V> getNode() {
            return node;
        }
    }
}
