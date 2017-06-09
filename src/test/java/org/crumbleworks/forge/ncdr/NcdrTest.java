package org.crumbleworks.forge.ncdr;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for encoding &amp; decoding functionality
 * 
 * @author Michael Stocker
 * @since CURRENT_VERSION
 */
public class NcdrTest {

    private static final String vowels = "AaEeIiOoUu";
    private static final List<String> words = new ArrayList<>();
    static {
        words.add("Hello");
        words.add("World");
        words.add("Peter");
        words.add("Pomegrenate");
        words.add("Bus");
        words.add("Yes");
        words.add("No");
        words.add("an");
        words.add("in");
        words.add("James");
        words.add("Whoosh");
        words.add("Circus");
        words.add("Lambda");
        words.add("Syberia");
    }
    
    @Test
    public void testFeed() {
        Ncdr ncdr = new Ncdr(vowels);
        ncdr.feed(words);
        
        assertThat(ncdr.getDictionary(), containsInAnyOrder(words.toArray()));
    }
    
    @Test
    public void testEncode() {
        Ncdr ncdr = new Ncdr(vowels);
        ncdr.feed(words);
        
        assertEquals("Hll n Sybr!", ncdr.encode("Hello in Syberia!", true));
        assertEquals("Hll frm Sybr!", ncdr.encode("Hello from Syberia!", true));

        assertEquals("Hll", ncdr.encode("Hello", true));
        assertEquals("Jghrt, mn r mchn?!", ncdr.encode("Joghurt, man or machine?!", true));
        assertEquals("Whsh.... spnt wyyyy t mch tm n ths :/", ncdr.encode("Whoosh.... I spent wayyyy too much time on this :/", true));
    }
    
    @Test
    public void textDecode() {
        Ncdr ncdr = new Ncdr(vowels);
        ncdr.feed(words);
        
        assertEquals("Hello an|in Syberia!", ncdr.decode("Hll n Sybr!"));
        assertEquals("Hello ¿frm? Syberia!", ncdr.decode("Hll frm Sybr!"));
        
        assertEquals("¿Pzz?", ncdr.decode("Pzz"));
    }
    
    @Test
    public void textEncodeDecodeWithAdding() {
        Ncdr ncdr = new Ncdr(vowels);
        ncdr.feed(words);
        
        assertEquals("Hll dr frnd!", ncdr.encode("Hello dear friend!", true));
        assertEquals("Hello dear friend!", ncdr.decode("Hll dr frnd!"));
    }
    
    @Test
    public void textEncodeDecodeWithoutAdding() {
        Ncdr ncdr = new Ncdr(vowels);
        ncdr.feed(words);
        
        assertEquals("Hll dr frnd!", ncdr.encode("Hello dear friend!", false));
        assertEquals("Hello ¿dr? ¿frnd?!", ncdr.decode("Hll dr frnd!"));
    }
}
