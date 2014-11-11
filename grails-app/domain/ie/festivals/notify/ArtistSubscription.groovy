package ie.festivals.notify

import ie.festivals.Artist
import ie.festivals.User

class ArtistSubscription {
    static belongsTo = [artist: Artist, user: User]

    static constraints = {
        // compound unique index
        artist unique: 'user'
    }
}
