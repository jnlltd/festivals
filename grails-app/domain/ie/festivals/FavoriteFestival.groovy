package ie.festivals

class FavoriteFestival {
    static belongsTo = [festival: Festival, user: User]

    static constraints = {

        festival unique: 'user'
    }
}
