package it.unibo.oop.lab.streams;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return this.songs.stream().map(t -> t.getSongName()).sorted(new Comparator<String>() {

            @Override
            public int compare(final String o1, final String o2) {
                return o1.compareTo(o2);
            }

        });
    }

    @Override
    public Stream<String> albumNames() {
        return this.albums.keySet().stream();
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return this.albumNames().filter(albumName -> this.albums.get(albumName) == year);
    }

    @Override
    public int countSongs(final String albumName) {
        return (int) this.songs.stream().
            filter(song -> albumName.equals(song.getAlbumName().orElse(null))).
            count();
    }

    @Override
    public int countSongsInNoAlbum() {
        return (int) this.songs.stream().
            filter(song -> song.getAlbumName().equals(Optional.empty())).
            count();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        double result = 0.0;
        final Iterator<Double> iterator = this.songs.stream().
            filter(song -> albumName.equals(song.getAlbumName().orElse(null))).
            map(song -> song.getDuration()).
            iterator();
        while (iterator.hasNext()) {
            result = result + iterator.next();
        }
        return OptionalDouble.of(result / (double) this.countSongs(albumName));
    }

    @Override
    public Optional<String> longestSong() {
        return Optional.of(this.songs.stream().
            max((song1, song2) -> Double.compare(song1.getDuration(), song2.getDuration()))
            .get().getSongName());
    }

    /**
     * @param albumName is the name of the album to be computed.
     * @return the length of the specified album.
     */
    private double albumLength(final String albumName) {
        return (double) this.countSongs(albumName) * this.averageDurationOfSongs(albumName).orElse(0.0);
    }

    @Override
    public Optional<String> longestAlbum() {
        return this.albumNames().
            max((album1, album2) -> 
                Double.compare(this.albumLength(album1), this.albumLength(album2)));
    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
