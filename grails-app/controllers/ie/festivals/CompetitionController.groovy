package ie.festivals

import de.andreasschmitt.export.ExportService
import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import ie.festivals.competition.Answer
import ie.festivals.competition.Competition
import ie.festivals.competition.Entry

class CompetitionController extends AbstractController {

    SpringSecurityService springSecurityService
    ExportService exportService

    private static final EXPORT_FORMAT = 'excel'

    @Secured(['ROLE_ADMIN'])
    def create() {
        [competition: new Competition()]
    }

    @Secured(['ROLE_ADMIN'])
    def edit() {
        [competition: getById(Competition)]
    }

    @Secured(['ROLE_ADMIN'])
    def update() {
        Competition competition = getById(Competition, [uri: "/"])

        if (isStale(competition)) {
            render(view: "edit", model: [competition: competition])
            return
        }

        // if no image is provided, use the previous image if available
        byte[] previousImage = competition.image
        competition.properties = params
        competition.image = competition.image ?: previousImage

        bindCorrectAnswer(competition)

        // changes weren't persisted before flush: true was added, go figure
        if (competition.save(flush: true)) {
            flashHelper.info 'default.updated': 'Competition'
            redirect action: 'show', params: [code: competition.code]
        } else {
            flashHelper.warn 'competition.invalid'
            render(view: "edit", model: [competition: competition])
        }
    }

    private void bindCorrectAnswer(Competition competition) {
        // Can't figure out how to automatically bind competition.answers[i].correct
        Integer answerIndex = params.int('correctAnswer')
        boolean answerSelected = answerIndex != null && competition.answers?.size() > answerIndex

        if (answerSelected) {
            competition.answers.eachWithIndex {Answer answer, index ->
                answer.correct = index == answerIndex
            }
        }
    }

    @Secured(['ROLE_ADMIN'])
    def save(Competition competition) {
        bindCorrectAnswer(competition)

        if (!competition.save()) {
            flashHelper.warn 'competition.invalid'
            render(view: "create", model: [competition: competition])
            return
        }

        flashHelper.info 'default.created': 'Competition'
        redirect action: 'show', params: [code: competition.code]
    }

    def show(String code) {
        Competition competition = Competition.findByCode(code)

        // Only admins can view competitions that have ended (so they can pick a winner)
        if (!competition || (competition.over && !isAdmin())) {
            log.warn "Active competition with code $code not found"
            flashHelper.warn 'competition.not.found': code
            redirect uri: "/"
            return
        }

        def winners = Entry.findAllByWinnerAndCompetition(true, competition)
        def entryCount = Entry.countByCompetition(competition)
        [entry: new CompetitionEntryCommand(competition: competition), winners: winners, entryCount: entryCount]
    }

    @Secured(['ROLE_ADMIN'])
    def chooseWinner() {

        Competition competition = getSafelyById(Competition)
        Answer correctAnswer = Answer.findCorrectByCompetition(competition)
        List<Entry> correctEntries = Entry.findAllByCompetitionAndAnswerAndWinner(competition, correctAnswer, false)

        if (correctEntries) {
            Collections.shuffle(correctEntries)
            Entry winningEntry = correctEntries[0]
            assert winningEntry.answer == correctAnswer
            winningEntry.winner = true
            flashHelper.info 'competition.winner.chosen'

        } else {
            flashHelper.warn 'competition.winner.not.found'
        }
        redirect  action: 'show', params: [code: competition.code]
    }


    @Secured(['ROLE_ADMIN'])
    def exportCorrectEntrants(Competition competition) {

        Answer correctAnswer = Answer.findCorrectByCompetition(competition)
        List<Entry> correctEntries = Entry.findAllByCompetitionAndAnswer(competition, correctAnswer)

        // the first param should match a key in ExportConfig.exporters with 'Exporter' removed
        response.contentType = grailsApplication.config.grails.mime.types[EXPORT_FORMAT]
        response.setHeader("Content-disposition", "attachment; filename=${competition.code}-correct-entrants.xls")

        List fields = ["user.name", "phoneNumber", "user.username"]
        Map labels = ["user.name": "Name", "phoneNumber": "Phone", "user.username": "Email"]

        // Formatter closure
        Map parameters = [title: competition.code, "column.widths": [0.2, 0.2, 0.3]]

        exportService.export(EXPORT_FORMAT, response.outputStream, correctEntries, fields, labels, [:], parameters)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def enter(CompetitionEntryCommand command) {
        Competition competition = command.competition

        if (!competition) {
            flashHelper.warn 'competition.not.specified'
            redirect uri: "/"
            return
        }

        User user = springSecurityService.currentUser
        Answer answer = Answer.findByCompetitionAndId(competition, command.answer)

        // If this user has already entered, update their answer
        Entry entry = Entry.findOrCreateWhere(user: user, competition: competition)
        entry.answer = answer
        entry.phoneNumber = command.phoneNumber

        if (command.terms && entry.save()) {

            // TODO: check that this flash message is displayed in production. If it is not, replace redirect below with forward
            flashHelper.info 'competition.entry.submitted': g.formatDate(date: competition.end)
            redirect controller: 'home'

        } else {
            flashHelper.warn 'competition.entry.error'
            render view: 'show', model: [entry: command]
        }
    }
}


class CompetitionEntryCommand {

    String phoneNumber
    Competition competition
    Long answer
    Boolean terms

    static constraints = {
        phoneNumber shared: 'phoneNumber'
    }
}