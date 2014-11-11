import grails.util.Environment
import ie.festivals.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class BootStrap {

    GrailsApplication grailsApplication

    def init = { servletContext ->

        def festivalConfig = grailsApplication.config.festival

        // create some roles
        def userRole = createRole(festivalConfig.userRoleName)
        def adminRole = createRole(festivalConfig.adminRoleName)

        if (Environment.developmentMode) {
            // create some users
            createUser('Default Admin', 'festival-admin@mailinator.com', adminRole)
            createUser('Default User', 'festival-user@mailinator.com', adminRole)
        }

        // Override the default maxSize of 191 on the body property of BlogEntry and Comment
        // We can't add this constraint directly to these classes because they're in plugins
        grailsApplication.domainClasses.each { GrailsDomainClass grailsDomainClass ->

            Class domainClass = grailsDomainClass.clazz

            if (domainClass.simpleName in ['BlogEntry', 'Comment']) {

                def field = domainClass.declaredFields.find {
                    it.name == 'body'
                }

                if (field) {
                    domainClass.constraints.body.maxSize = Integer.MAX_VALUE
                }
            }
        }
    }

    /**
     * Create a user if they don't already exist.
     * @param name
     * @param username
     * @param role
     * @param resetRoles indicates whether any roles currently assigned to the user will be removed before <tt>role</tt> is assigned
     * @return
     */
    private User createUser(name, username, Role role, boolean resetRoles = false) {

        def defaultPassword = 'password'

        User user = User.findByUsername(username) ?: new User(
                name: name,
                username: username,
                password: defaultPassword,
                passwordConfirm: defaultPassword).save(failOnError: true)

        if (resetRoles) {
            UserRole.removeAll(user)
        }

        if (!user.authorities.contains(role)) {
            UserRole.create user, role
        }
        user
    }

    private createRole(String roleName) {
        Role.findByAuthority(roleName) ?: new Role(authority: roleName).save(failOnError: true)
    }

    def destroy = {
    }
}
