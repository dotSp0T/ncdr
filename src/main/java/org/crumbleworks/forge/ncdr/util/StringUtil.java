package org.crumbleworks.forge.ncdr.util;

/**
 * @author Patrick Bächli
 */
public class StringUtil {

    private StringUtil() {}
    
    /**
     * Checks that a string is neither <code>null</code> nor empty ("")
     * 
     * @param s
     * 
     * @return <code>true</code> if the string is neither null nor empty; <code>false</code> otherwise
     */
    public static final boolean neitherNullNorEmpty(final String s) {
        if(s != null && !"".equals(s)) {
            return true;
        }
        
        return false;
    }
}
