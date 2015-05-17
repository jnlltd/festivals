package ie.festivals

import ie.festivals.competition.Entry
import ie.festivals.notify.ArtistSubscription
import ie.festivals.notify.FestivalSubscription

class User {
    def saltSource
    def springSecurityService
    static transients = ['passwordConfirm', 'twitter', 'enabled', 'accountExpired', 'passwordExpired']

    String name
    String username
    String password
    String passwordConfirm
    String apiKey
    boolean accountLocked

    // these fields are required by GormUserDetailsService, but we don't use them, so they can be transient
    boolean enabled = true
    boolean accountExpired = false
    boolean passwordExpired = false

    String socialLoginProvider
    Date dateCreated

    /**
     * This field is populated from the data returned by Janrain login. It is only used by twitter-registered users
     */
    String socialId

    @Override
    String toString() {
        name
    }

    boolean isTwitter() {
        'Twitter' == socialLoginProvider
    }

    static hasMany = [
            artistSubscriptions: ArtistSubscription,
            festivalSubscriptions: FestivalSubscription,
            ratings: Rating,
            entries: Entry]

    static constraints = {

        // need to prevent the default maxSize constraint of 191 from applying to these collections
        artistSubscriptions shared: 'unlimitedSize'
        festivalSubscriptions shared: 'unlimitedSize'
        ratings shared: 'unlimitedSize'
        entries shared: 'unlimitedSize'

        // users that registered before this field was added won't have a creation date
        dateCreated nullable: true
        name blank: false
        username blank: false, unique: true, email: true
        socialLoginProvider nullable: true

        // Compound unique index because usernames should be unique within a particular provider
        socialId nullable: true, unique: 'socialLoginProvider'

        passwordConfirm bindable: true
        apiKey nullable: true
        password blank: false, validator: {val, self ->

            // We only need to check the password confirmation the first time a user is saved
            if (!self.id) {
                val == self.passwordConfirm
            }
        }
    }

    static mapping = {
        password column: '`password`'
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        def salt = saltSource.systemWideSalt
        password = springSecurityService.encodePassword(password, salt)
        passwordConfirm = springSecurityService.encodePassword(passwordConfirm, salt)
    }
}
