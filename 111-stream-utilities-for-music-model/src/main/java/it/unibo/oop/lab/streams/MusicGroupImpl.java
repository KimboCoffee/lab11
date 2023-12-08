package it.unibo.oop.lab.streams;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        int counter = 0;
        for (final var song : this.songs) {
            if (song.getAlbumName().equals(Optional.empty())) {
                counter++;
            }
        }
        return counter;
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        double partialResults = 0;
        OptionalDouble average = OptionalDouble.empty();
        final int songNumber = countSongs(albumName);
        if (songNumber != 0) {
            for (final var song : this.songs) {
                if (albumName.equals(song.getAlbumName().orElse(null))) {
                    partialResults = partialResults + song.getDuration();
                }
            }
            partialResults = partialResults / songNumber;
            average = OptionalDouble.of(partialResults);
        }
        return average;
    }

    @Override
    public Optional<String> longestSong() {
        Optional<String> result = Optional.empty();
        double maxDuration = 0.0;
        if (!this.songs.isEmpty()) {
            for (final var song : this.songs) {
                if (song.getDuration() > maxDuration) {
                    maxDuration = song.getDuration();
                    result = Optional.of(song.getSongName());
                }
            }
        }
        return result;
    }

    @Override
    public Optional<String> longestAlbum() {
        Optional<String> result = Optional.empty();
        final List<String> albumNames = this.albumNames().toList();
        double maxDuration = 0.0;
        double partialDuration;
        for (final var name : albumNames) {
            partialDuration = 
                this.countSongs(name) * this.averageDurationOfSongs(name).orElse(0.0);
            if (partialDuration > maxDuration) {
                maxDuration = partialDuration;
                result = Optional.of(name);
            }
        }
        return result;
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
