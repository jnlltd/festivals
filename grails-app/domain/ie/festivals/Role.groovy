package ie.festivals

import org.apache.commons.lang.WordUtils

class Role {

	String authority

    String toString() {
        WordUtils.capitalizeFully(authority - "ROLE_")
    }

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
