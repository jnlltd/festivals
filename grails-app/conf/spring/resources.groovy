import grails.util.Holders
import ie.festivals.databinding.DomainClassConverter
import ie.festivals.databinding.EnumCollectionConverter
import ie.festivals.notify.EmailSender
import org.springframework.security.authentication.dao.SystemWideSaltSource

beans = {
    saltSource(SystemWideSaltSource) {
        systemWideSalt = Holders.config.systemWidePasswordSalt
    }

    emailSender(EmailSender) {
        mailService = ref('mailService')
        grailsApplication = ref('grailsApplication')
        messageSource = ref('messageSource')
        pageRenderer = ref('groovyPageRenderer')
    }

    // databinding beans: http://grails.org/doc/latest/guide/theWebLayer.html#dataBinding
    enumCollectionConverter(EnumCollectionConverter)

    domainClassConverter(DomainClassConverter) {
        grailsApplication = ref('grailsApplication')
    }
}
