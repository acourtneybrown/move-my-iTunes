package com.notcharlie.itunes.move;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;

public class Main {
    private static final Log log = LogFactory.getLog(Main.class);
    private static final String TRACKS = "Tracks";
    private static final String LOCATION = "Location";
    private static final String TRACK_NUMBER = "Track Number";
    private static final String NAME = "Name";
    private static final String ALBUM = "Album";
    private static final String ARTIST = "Artist";
    
    @Option(name = "-i", usage = "Filename of the existing iTunes XML music library to use")
    private File inputLibrary = new File("Library.xml");

    @Option(name = "-o", usage = "Filename of the new iTunes XML music library to output")
    private File outputLibrary = new File("Library-new.xml");

    @Argument(index = 0, required = true)
    private String oldRoot = File.separator;
//    private File oldRoot = new File(".");
    
    @Argument(index = 1)
    private String newRoot = ".";
//    private File newRoot = new File(".");

    public static void main(String[] args) {
        new Main().doMain(args);
    }

    public void doMain(String[] args) {
        processArguments(args);
        
        NSDictionary library = loadLibrary();
        NSDictionary tracks = (NSDictionary) library.objectForKey(TRACKS);
        for (String trackKey : tracks.allKeys()) {
            NSDictionary trackDictionary = (NSDictionary) tracks.objectForKey(trackKey);
            NSString location = (NSString) trackDictionary.objectForKey(LOCATION);
            log.debug(String.format("#%s -> %s", trackKey, location));
            String newLocation = location.toString().replace(oldRoot, newRoot);
            
            URI uri;
            try {
                uri = new URI(newLocation);
            } catch (URISyntaxException e) {
                log.error(String.format("Invalid URI from <%s>", newLocation));
                throw new RuntimeException(e);
            }
            File newFile = new File(uri.getPath());
            if (!newFile.exists()) {
                NSNumber trackNumber = (NSNumber) trackDictionary.objectForKey(TRACK_NUMBER);
                NSString trackName = (NSString) trackDictionary.objectForKey(NAME);
                NSString trackAlbum = (NSString) trackDictionary.objectForKey(ALBUM);
                NSString trackArtist = (NSString) trackDictionary.objectForKey(ARTIST);
                
                log.warn(String.format("No file at new location <%s>", newLocation));
                File nextSearch = newFile;
                while (!nextSearch.exists()) {
                    nextSearch = nextSearch.getParentFile();
                }
                log.debug(String.format("Found existing directory <%s>", nextSearch.getPath()));
                URI foundFile = searchFile(nextSearch, trackNumber.longValue(), trackName.toString(),
                        trackAlbum.toString(), trackArtist.toString());
            }

            trackDictionary.put(LOCATION, newLocation);
        }
    }
    
    private URI searchFile(File startingFile, long number, String name, String album, String artist) {
        return null;
    }
    
    private NSDictionary loadLibrary() {
        NSObject library;
        try {
            library = PropertyListParser.parse(inputLibrary);
        } catch (Exception e) {
            log.error("Error while reading the input iTunes Library", e);
            throw new RuntimeException(e);
        }
        return (NSDictionary) library;
    }

    private void processArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            // parse the arguments.
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java Main [options...] arguments...");

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println("  Example: java Main" + parser.printExample(ExampleMode.ALL));

            throw new RuntimeException(e);
        }
    }
}
