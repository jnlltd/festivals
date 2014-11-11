package ie.festivals.competition


class Answer {

    String answer
    boolean correct = false

    static belongsTo = [competition: Competition]

    static constraints = {
        answer blank: false
    }

    static mapping = {
        cache true
    }

    @Override
    String toString() {
        answer
    }
}
