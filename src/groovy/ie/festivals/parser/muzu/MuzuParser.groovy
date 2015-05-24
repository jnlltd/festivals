package ie.festivals.parser.muzu

import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import ie.festivals.parser.ApiResponseParser

enum MuzuParser implements ApiResponseParser<String> {

    ARTIST_VIDEO('video'),
    ARTIST_PLAYLIST('channel.videos.video')

    /**
     * Indicates the path from the root element to the target video element
     */
    private final List<String> videoPathElements
    
    MuzuParser(String videoPath) {
        videoPathElements = videoPath.tokenize('.')
    }

    @Override
    String parse(GPathResult xmlResponse) {

        def videoNode = videoPathElements.inject xmlResponse, {currentNode, videoPathElement ->
            currentNode[videoPathElement]
        }

        Iterator videoIterator = videoNode.iterator()

        if (videoIterator.hasNext()) {
            String embedCode = videoIterator.next().embed?.text()
            Elements iFrames = Jsoup.parseBodyFragment(embedCode).getElementsByTag('iframe')

            if (iFrames.size() == 1) {
                Element iFrame = iFrames[0]

                // we need to append '&ps=b' to the src attribute of the <iframe> for the embed code to work, see #583
                String srcAttrValue = iFrame.attr('src')
                srcAttrValue += '&ps=b'
                iFrame.attr('src', srcAttrValue)
                String iFrameHtml = iFrame.toString()

                // The video embed code is parsed out by JSoup which also cleans up the HTML. This cleaning process
                // converts the attribute name "allowfullscreen" (without a value) to allowfullscreen="".
                // Unfortunately, only the former actually works so we need to undo this cleaning
                StringUtils.replaceOnce(iFrameHtml, 'allowfullscreen=""', 'allowfullscreen')
            }
        }
    }
}
