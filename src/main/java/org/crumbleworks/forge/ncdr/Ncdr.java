package org.crumbleworks.forge.ncdr;

import static org.crumbleworks.forge.ncdr.util.Parameters.notNull;
import static org.crumbleworks.forge.ncdr.util.Parameters.stringNotEmpty;
import static org.crumbleworks.forge.ncdr.util.StringUtil.neitherNullNorEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.crumbleworks.forge.ncdr.util.LookupNode;
import org.crumbleworks.forge.ncdr.util.LookupNode.LookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes &amp; decodes silly speech.
 * 
 * <p>The ncdr is case-sensitive.
 * 
 * @author Michael Stocker
 * @since CURRENT_VERSION
 */
public class Ncdr {
    private static final Logger logger = LoggerFactory.getLogger(Ncdr.class);
    
    /**
     * Thank you stackoveflow
     * @see https://stackoverflow.com/a/29871638/2889776
     */
    private static final String PUNCTUATION_SPLITTER = " ?(?<!\\G)((?<=[^\\p{Punct}])(?=\\p{Punct})|\\b) ?";
    private static final String PUNCTUATION_REGEX = "^\\p{Punct}+$";
    
    private static final String UNKNOWN_WORD_PREFIX = "¿";
    private static final String UNKNOWN_WORD_SUFFIX = "?";
    private static final String MULTIPLE_RESULTS_DIVIDER = "|";
    
    private final String stripRegex;
    private final String onlyStrippedCharsRegex;
    private final LookupNode<String> dictionary;
    
    /**
     * @param strip the letters to be stripped 
     */
    public Ncdr(final String strip) {
        stripRegex = "[" + stringNotEmpty(notNull(strip)) + "]";
        onlyStrippedCharsRegex = "^" + stripRegex + "+$";
        dictionary = new LookupNode<>();
    }
    
    /**
     * @see #feed(String)
     * 
     * @param words a collection of words
     */
    public void feed(Collection<String> words) {
        for(String word : words) {
            feed(word);
        }
    }
    
    /**
     * Feeds the ncdr a single word to be used for encoding &amp; decoding.
     * 
     * @param word a word
     */
    public void feed(String word) {
        String strippedWord = strip(notNull(word));
        logger.debug("Feeding '{}' > '{}' to the dictionary.", strippedWord, word);
        dictionary.put(strippedWord, word);
    }
    
    /**
     * Retrieves all the words in the dictionary.
     *
     * @return a set with all words
     */
    public Set<String> getDictionary() {
        return dictionary.getPossibleValues();
    }
    
    /**
     * Will encode the given phrase.
     * 
     * <p>This method assumes &amp; treats any whitespace character as word-divider.
     * 
     * <p>This method will add any unknown words to the dictionary if the <code>add</code>-flag is set.
     *
     * @param s the phrase to be encoded
     * @param add tells the method to add any unknown words to the dicitonary
     * 
     * @return the encoded phrase
     */
    public String encode(final String s, boolean add) {
        logger.debug("Encoding: {}", s);

        return process(s, (w) -> {
            if(w.matches(onlyStrippedCharsRegex)) {
                logger.debug(" > Skipping empty");
                return "";
            }
                
            if(add) {
                feed(w);
            }
            
            logger.debug(" > Word");
            return strip(w);
        });
    }
    
    /**
     * Will decode the given phrase.
     * 
     * <p>This method assumes &amp; treats any whitespace character as word-divider.
     * 
     * <p>Any word the ncdr cannot decode will be hugged by question marks: <code>¿wrd?</code>.
     * 
     * <p>If a word returns multiple possible results they will be divided by vertical bars: <code>an|in|no</code>.
     * 
     * @param s the phrase to be decoded
     * 
     * @return the decoded phrase
     */
    public String decode(final String s) {
        logger.debug("Decoding: {}", s);

        return process(s, (w) -> {
            StringBuilder sb = new StringBuilder();
            
            logger.debug(" > Word");
            LookupResult<String> res = dictionary.resolve(w, false);
            
            if(res == null) {
                sb.append(UNKNOWN_WORD_PREFIX)
                  .append(w)
                  .append(UNKNOWN_WORD_SUFFIX);
            } else {
                List<String> values = new ArrayList<>(res.getNode().getValues());
                Collections.sort(values);
                
                if(values.size() > 0) {
                    for(int j = 0 ; j < values.size() ; j++) {
                        
                        sb.append(values.get(j));
                        
                        if(j < values.size() - 1) {
                            sb.append(MULTIPLE_RESULTS_DIVIDER);
                        }
                    }
                } else {
                    sb.append(UNKNOWN_WORD_PREFIX)
                    .append(w)
                    .append(UNKNOWN_WORD_SUFFIX);
                }
            }
            
            return sb.toString();
        });
    }
    
    private final String process(final String s, final Function<String, String> func) {
        if(!neitherNullNorEmpty(s)) {
            logger.debug("Received empty or null string, returning empty string.");
            return "";
        }
        
        final String[] tokens = s.split(PUNCTUATION_SPLITTER);
        final StringBuilder sb = new StringBuilder();
        
        for(int i = 0 ; i < tokens.length ; i++) {
            logger.debug(" processing: {}", tokens[i]);
            
            if(tokens[i].matches("\\p{Punct}") || tokens[i].matches(PUNCTUATION_REGEX)) {
                if(tokens[i].matches("^.*[:;].*$")) {
                    logger.debug(" > Smiley");
                } else {
                    logger.debug(" > Punctuation");
                    sb.setLength(sb.length() - 1); //so hacky..
                }
                
                sb.append(tokens[i]);
            } else {
                String res = func.apply(tokens[i]);
                
                if("".equals(res)) {
                    continue;
                } else {
                    sb.append(res);
                }
            }
            
            if(i < tokens.length - 1) {
                sb.append(" ");
            }
        }
        
        return sb.toString();
    }
    
    private final String strip(final String s) {
        return s.replaceAll(stripRegex, "");
    }
}
