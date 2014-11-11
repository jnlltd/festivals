package ie.festivals.competition

import ie.festivals.User

class Entry {

    String phoneNumber
    boolean winner = false

    // these relationships are intentionally unidirectional to prevent N+1 queries
    // http://mrpaulwoods.wordpress.com/2011/02/07/implementing-burt-beckwiths-gorm-performance-no-collections/
    Competition competition
    Answer answer

    static belongsTo = [user: User]

    static mapping = {
        // prevent N+1 queries when exporting entrants
        user fetch: 'join'
    }

    static constraints = {
        // compound unique index
        competition unique: 'user'
        phoneNumber shared: 'phoneNumber'
    }
}
