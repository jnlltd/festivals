package ie.festivals.music

import ie.festivals.Artist
import ie.festivals.Named

class Album implements Named {
    String name
    String url
    String largeImageUrl

    static belongsTo = [artist: Artist]

    static constraints = {
        url nullable: true, url: true
        largeImageUrl nullable: true, url: true

        name unique: 'artist'
    }

    static mapping = {
        cache true
    }

    @Override
    String toString() {
        name
    }
}
