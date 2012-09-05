package com.notcharlie.itunes.move;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

public class Main {
    private static final Log log = LogFactory.getLog(Main.class);
    
    @Option(name = "-i", usage = "Filename of the existing iTunes XML music library to use")
    private File inputLibrary = new File("Library.xml");

    @Option(name = "-o", usage = "Filename of the new iTunes XML music library to output")
    private File outputLibrary = new File("Library-new.xml");

    @Argument(index = 0, required = true)
    private String newRoot = ".";

    public static void main(String[] args) {
        new Main().doMain(args);
        log.debug("Completed run");
    }

    public void doMain(String[] args) {
        processArguments(args);
        
        NSDictionary library = loadXmlLibrary();
        ITunesLibrary itunesLibrary = new ITunesLibrary(library);
        String[] extensions = {".mp3", ".m4a"};
        IOFileFilter audioFilesFilter = new SuffixFileFilter(extensions);
        IOFileFilter removeDotDirectories = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                return !name.startsWith(".");
            }

            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        };
        for (File file : FileUtils.listFiles(new File(newRoot), audioFilesFilter, removeDotDirectories)) {
            AudioFile audioFile;
            try {
                audioFile = AudioFileIO.read(file);
            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException
                    | InvalidAudioFrameException e) {
                log.error(String.format("Problem decoding audio file <%s>", file));
                throw new RuntimeException(e);
            }
            Tag tag = audioFile.getTag();
            Long trackNumber = null;
            try {
                trackNumber = Long.parseLong(tag.getFirst(FieldKey.TRACK));
            } catch (NumberFormatException e) {
                // do nothing
            }
            String artist = tag.getFirst(FieldKey.ARTIST);
            String album = tag.getFirst(FieldKey.ALBUM);
            String trackName = tag.getFirst(FieldKey.TITLE);
            NSDictionary trackDictionary = itunesLibrary.findTrack(trackNumber, artist, album, trackName);
            if (trackDictionary != null) {
                log.debug(String.format("Changing Location for <%s> to <%s>", trackDictionary, file.toURI()));
            } else {
                log.warn(String.format("No iTunes track found for <%s> <%s> <%s> <%s>", trackNumber, artist, album,
                        trackName));
            }
        }
    }
    
    private URI searchFile(File startingFile, long number, String name, String album, String artist) {
        return null;
    }
    
    private NSDictionary loadXmlLibrary() {
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
