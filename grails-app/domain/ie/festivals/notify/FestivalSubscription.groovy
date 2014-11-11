package ie.festivals.notify

import ie.festivals.Festival
import ie.festivals.User

class FestivalSubscription {
    static belongsTo = [festival: Festival, user: User]

    static constraints = {

        festival unique: 'user', validator: {Festival festival ->
            // Can't subscribe to a festival that doesn't have a lineup
            festival.hasLineup
        }
    }
}
