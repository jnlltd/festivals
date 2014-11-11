package ie.festivals.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class HtmlUtilsTests {

    void testScriptTagsRemoved() {
        def html = '''
<div>
    <script type="text/javascript">alert('hello');</script>
</div>
'''
        def cleanedHtml = HtmlUtils.normalize(html, null)
        compareIgnoringWhitepace "<div></div>", cleanedHtml
    }

    void testScriptTagsRemovedAndLinkTargetUpdated() {
        def html = '''
<div>
    <script type="text/javascript">alert('hello');</script>
    <a href="http://example.org">click</a>
</div>
'''
        // by default target should be changed to _blank
        def cleanedHtml = HtmlUtils.normalize(html)

        def expectedCleanedHtml = '''
<div>
    <a href="http://example.org" target="_blank">click</a>
</div>
'''
        compareIgnoringWhitepace expectedCleanedHtml, cleanedHtml

        // a falsey 2nd param should prevent any changes to the link target
        cleanedHtml = HtmlUtils.normalize(html, null)
        expectedCleanedHtml = '''
<div>
    <a href="http://example.org">click</a>
</div>
'''
        compareIgnoringWhitepace expectedCleanedHtml, cleanedHtml
    }

    void testRemoveTags() {

        String html = '<p>Electric Picnic is <strong>one of the biggest</strong> music <em>and</em> arts festivals in Ireland.</p>'
        String text = HtmlUtils.removeTags(html)
        assertEquals 'Electric Picnic is one of the biggest music and arts festivals in Ireland.', text
    }


    private void compareIgnoringWhitepace(String expected, String actual) {
        expected = expected.replaceAll("\\s","")
        actual = actual.replaceAll("\\s","")
        assertEquals expected, actual
    }
}
