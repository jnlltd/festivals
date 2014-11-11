package ie.festivals

class Reminder {

    Integer daysAhead

    static belongsTo = [festival: Festival, user: User]
    static transients = ['range']

    static constraints = {
        // compound unique index
        festival unique: 'user'
        daysAhead min: 0
    }
}
