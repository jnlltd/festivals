package ie.festivals.music

import ie.festivals.Artist
import ie.festivals.Named

class Track implements Named {
    String name
    Integer duration
    String url

    static belongsTo = [artist: Artist]

    static constraints = {
        duration nullable: true
        url nullable: true, url: true

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
