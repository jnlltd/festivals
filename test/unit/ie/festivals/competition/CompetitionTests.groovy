package ie.festivals.competition

import grails.test.mixin.Mock

@Mock([Competition, Answer])
class CompetitionTests {

    void testCompetitionCodeValidation() {
        Competition competition = new Competition(
                question: 'foo',
                title: 'foo',
                end: new Date() + 10,
                description: 'foo',
                code: null
        )

        [new Answer(answer: 'answer'), new Answer(answer: 'answer', correct: true), new Answer(answer: 'answer')].each {
            competition.addToAnswers(it)
        }

        // code cannot be null
        assertFalse competition.validate()

        ['valid-code', 'valid code', 'valid_code', '999-666'].each {
            competition.code = it
            assertTrue competition.validate()
        }

        // code must begin with digit or number
        [' invalid', '_invalid', '-invalid'].each {
            competition.code = it
            assertFalse competition.validate()
        }
    }
}
