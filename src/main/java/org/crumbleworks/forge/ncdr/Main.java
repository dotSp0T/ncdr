package org.crumbleworks.forge.ncdr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Creates an interactive console session 
 * 
 * <p>Commands:
 * <dl>
 *     <dt>E something something phrase</dt>
 *     <dl>encodes the supplied phrase</dl>
 *     <dt>D smthng smthng phrs</dt>
 *     <dl>decodes the supplied phrase</dl>
 *     <dt>LOAD path/to/file.txt</dt>
 *     <dl>loads the contents of the supplied file into the dictionary</dl>
 *     <dt>STORE path/to/file.txt</dt>
 *     <dl>stores the dictionary into the supplied file</dl>
 *     <dt>BYE</dt>
 *     <dl>exits the application</dl>
 * </dl>
 * 
 * @author Michael Stocker
 * @since CURRENT_VERSION
 */
public class Main {
    private static final Ncdr ncdr = new Ncdr("AaEeIiOoUu");
    private static boolean bye = false;
    
    private static final Map<String, Consumer<String>> cmd = new HashMap<>();
    static {
        cmd.put("E", (s) -> {
            System.out.println(ncdr.encode(s, true));
        });
        cmd.put("D", (s) -> {
            System.out.println(ncdr.decode(s));
        });
        cmd.put("LOAD", (s) -> {
            try {
                ncdr.feed(Files.readAllLines(Paths.get(s)));
                System.out.println("Loaded contents from '" + s + "'");
            } catch(IOException e) {
                System.out.println("Failed loading from file '" + s + "', make sure the file exists!");
            }
        });
        cmd.put("STORE", (s) -> {
            try {
                Files.write(Paths.get(s), ncdr.getDictionary());
                System.out.println("Wrote dictionary to '" + s + "'");
            } catch(IOException e) {
                System.out.println("Failed writing to file '" + s + "', make sure the file exists!");
            }
        });
        cmd.put("BYE", (s) -> {
            System.out.println("-Stay classy San Diego!");
            bye = true;
        });
    }
    
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        
        while(!bye) {
            String[] input = scan.nextLine().split("\\s+", 2);
            String command = input[0].toUpperCase();
            
            if("".equals(command)) {
                continue;
            }
            
            if(cmd.containsKey(command)) {
                cmd.get(command).accept(input.length > 1 ? input[1] : "");
            } else {
                System.out.println("Unknown command: " + input[0]);
            }
        }
        
        scan.close();
    }
}
