package ie.festivals

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.validation.ObjectError

abstract class AbstractController {

    protected boolean isStale(persistedObj, versionParam = params.version, String errorMsgArg = persistedObj.class.simpleName) {

        if (versionParam) {
            def version = versionParam.toLong()
            if (persistedObj.version > version) {
                flashHelper.warn 'default.optimistic.locking.failure': errorMsgArg
                return true
            }
        } else {
            log.warn "No version param found for ${persistedObj.getClass()}"
        }
        false
    }

    protected storeErrorMessagesInFlash(validateable) {

        List<ObjectError> errors = validateable.errors?.allErrors
        errors.each {
            String errorMessage = g.message(message: it)
            flashHelper.warn errorMessage
        }
    }

    /**
     * Retrieve a domain class instance by ID from <tt>params.id</tt> in a null-safe fashion
     * @param domainClass type of domain class to be retrieved
     * @param readOnly
     * @return domain class instance or null
     */
    protected <T> T getSafelyById(Class<T> domainClass, boolean readOnly = false) {
        def id = params.long('id')
        readOnly ? domainClass.read(id) : domainClass.get(id)
    }

    /**
     * Retrieve a domain class instance by ID from <tt>params.id</tt> in a null-safe fashion.
     * If instance cannot be retrieved add an appropriate message to flash scope and (optionally)
     * perform a redirect
     *
     * @param domainClass type of entity to be loaded
     * @param redirectArgs if truthy, specifies where user should be redirected to. If falsey, no
     * redirect will be performed
     * @return the loaded domain class instance or null if loading or staleness checking failed
     */
    protected <T> T getById(Class<T> domainClass, Map redirectArgs = [uri: "/"]) {
        Long id = params.long('id')
        T domainInstance = domainClass.get(id)

        if (!domainInstance) {
            flashHelper.warn 'default.not.found.message': [domainClass.simpleName, id]

            if (redirectArgs) {
                redirect redirectArgs
            }
        }
        domainInstance
    }

    protected boolean isAdmin() {
        SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')
    }
}
