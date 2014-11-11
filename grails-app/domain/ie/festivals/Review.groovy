package ie.festivals

import ie.festivals.util.HtmlUtils

class Review {

    String title
    String body
    User author
    Date dateCreated
    boolean approved

    void setBody(String reviewBody) {
        this.body = HtmlUtils.normalize(reviewBody, null)
    }

    static belongsTo = [festival: Festival]

    static constraints = {
        // if a user is deleted we don't want to also have to delete their reviews
        author nullable: true
        body shared: 'unlimitedSize'
    }

    static mapping = {
        body type: 'text'

        cache true
    }
}
