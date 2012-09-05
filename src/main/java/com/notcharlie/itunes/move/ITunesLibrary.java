package com.notcharlie.itunes.move;

import java.util.HashMap;
import java.util.Map;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;

public class ITunesLibrary {
    private static final String TRACKS = "Tracks";
    private static final String NAME = "Name";
    private static final String ALBUM = "Album";
    private static final String ARTIST = "Artist";
    
    private final Map<String, Map<String, Map<String, NSDictionary>>> artistsAlbums = new HashMap<>();

    public ITunesLibrary(NSDictionary library) {
        NSDictionary libraryTracks = (NSDictionary) library.objectForKey(TRACKS);
        for (String trackKey : libraryTracks.allKeys()) {
            NSDictionary trackDictionary = (NSDictionary) libraryTracks.objectForKey(trackKey);
            NSString nsTrackAlbum = (NSString) trackDictionary.objectForKey(ALBUM);
            NSString nsTrackArtist = (NSString) trackDictionary.objectForKey(ARTIST);
            NSString nsTrackName = (NSString) trackDictionary.objectForKey(NAME);
            
            String trackArtist = nsTrackArtist!= null ? nsTrackArtist.toString() : null;
            if (!artistsAlbums.containsKey(trackArtist)) {
                artistsAlbums.put(trackArtist, new HashMap<String, Map<String, NSDictionary>>());
            }
            Map<String, Map<String, NSDictionary>> albums = artistsAlbums.get(trackArtist);
            String trackAlbum = nsTrackAlbum != null ? nsTrackAlbum.toString() : null;
            if (!albums.containsKey(trackAlbum)) {
                albums.put(trackAlbum, new HashMap<String, NSDictionary>());
            }
            Map<String, NSDictionary> albumTracks = albums.get(trackAlbum);
            String trackName = nsTrackName != null ? nsTrackName.toString() : null;
            albumTracks.put(trackName, trackDictionary);
        }
    }

    public NSDictionary findTrack(Long trackNumber, String artist, String album, String trackName) {
        Map<String, Map<String, NSDictionary>> albums = artistsAlbums.get(artist);
        if (albums != null) {
            Map<String, NSDictionary> tracks = albums.get(album);
            if (tracks != null) {
                NSDictionary track = tracks.get(trackName);
                if (track != null) {
                    return track;
                }
            }
        }
        return null;
    }
}
