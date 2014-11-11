package ie.festivals.security

import grails.util.Holders
import ie.festivals.enums.ConfirmationCodeType

class ConfirmationCode {

	String username
	String token = UUID.randomUUID().toString().replaceAll('-', '')
	Date dateCreated
    ConfirmationCodeType type

    /**
     * Confirmed registration codes are soft deleted so that we can distinguish between invalid tokens and
     * expired tokens
     */
    boolean expired = false

	static mapping = {
		version false
        type length: Holders.config.festival.utf8mb4MaxLength
	}

    static constraints = {
        token unique: true
    }
}
