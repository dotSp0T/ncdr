package org.crumbleworks.forge.ncdr.util;

/**
 * @author Michael Stocker
 */
public final class Parameters {

    private Parameters() {}
    
    /**
     * Checks if a value is <code>null</code>
     * 
     * @param value
     * @param errorMessage
     * 
     * @return the value
     * @throws IllegalArgumentException if the value is <code>null</code>
     */
    public static final <T> T notNull(final T value, final String errorMessage) {
        if(null != value) {
            return value;
        }
        
        throw new IllegalArgumentException(errorMessage);
    }
    
    public static final <T> T notNull(final T value) {
        return notNull(value, "Parameter may not be null!");
    }
    
    
    
    /**
     * Checks that a string is not empty (<code>!"".equals(String)</code>)
     * 
     * @param s
     * @param errorMessage
     * 
     * @return the passed in string
     * @throws IllegalArgumentException if the string was empty
     */
    public static final String stringNotEmpty(final String s, final String errorMessage) {
        if(!s.equals("")) { //to force throwing a nullpointer
            return s;
        }
        
        throw new IllegalArgumentException(notNull(errorMessage));
    }
    
    public static final String stringNotEmpty(final String s) {
        return stringNotEmpty(s, "String may not be empty!");
    }
}
