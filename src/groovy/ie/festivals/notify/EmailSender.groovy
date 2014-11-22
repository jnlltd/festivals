package ie.festivals.notify

import grails.gsp.PageRenderer
import grails.plugin.mail.MailService
import grails.util.ClosureToMapPopulator
import grails.util.Environment
import ie.festivals.MailLog
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable

class EmailSender {

    MessageSource messageSource
    PageRenderer pageRenderer
    LinkGenerator linkGenerator
    MailService mailService
    GrailsApplication grailsApplication

    private String getBaseUrl() {
        String baseUrl = linkGenerator.link(absolute: true, uri: '/')

        // if the last char is '/' remove it or things will go horribly wrong when we use it with <g:link base="${baseUrl}".../>
        baseUrl[-1] == '/' ? baseUrl[0..-2] : baseUrl
    }


    /**
     * Sends an email
     *
     * @param recipient email address of the the recipient
     * @param messageSubject used to resolve the email subject
     * @param mailTemplate path to the template that generates the body of the message
     * @param mailModel model that the template uses to generate the message body
     * @param asynchronous
     */
    void send(String recipient, MessageSourceResolvable messageSubject, String mailTemplate, Map mailModel, boolean asynchronous = false) {

        String mailSubject = messageSource.getMessage(messageSubject, null)
        mailModel.baseUrl = getBaseUrl()
        def mailBody = pageRenderer.render(template: mailTemplate, model: mailModel)

        Closure mailServiceArgs = {
            async asynchronous
            to recipient
            subject mailSubject
            html mailBody
        }

        send(mailServiceArgs)
    }

    /**
     * Send an email
     * @param mailServiceArgs
     */
    void send(Closure mailServiceArgs) {

        if (grailsApplication.config.festival.sendEmail) {

            mailService.sendMail mailServiceArgs

            // Log the recipient and subject of the mail
            Map mailProperties = new ClosureToMapPopulator().populate(mailServiceArgs)

            def mailLog = new MailLog(
                    recipient: mailProperties.to,
                    subject: mailProperties.subject)

            if (!mailLog.save()) {
                log.error "Failed to save email audit info due to errors: $mailLog.errors"
            }
        } else {
            log.warn "Email sending is disabled in ${Environment.current} environment"
        }
    }
}