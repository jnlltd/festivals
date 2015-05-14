import grails.util.Environment
import ie.festivals.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class BootStrap {

    GrailsApplication grailsApplication

    def init = { servletContext ->

        def festivalConfig = grailsApplication.config.festival

        def userRole = createRoleIfAbsent(festivalConfig.userRoleName)
        def adminRole = createRoleIfAbsent(festivalConfig.adminRoleName)

        if (Environment.developmentMode) {
            createUserIfAbsent('Default Admin', 'festival-admin@mailinator.com', adminRole)
            createUserIfAbsent('Default User', 'festival-user@mailinator.com', userRole)
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
     */
    private void createUserIfAbsent(String name, String username, Role role, boolean resetRoles = false) {

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
    }

    /**
     * Creates a role if it doesn't already exist
     * @param name the name of the role
     * @return the role with this name
     */
    private createRoleIfAbsent(String name) {
        Role.findByAuthority(name) ?: new Role(authority: name).save(failOnError: true)
    }

    def destroy = {
    }
}
