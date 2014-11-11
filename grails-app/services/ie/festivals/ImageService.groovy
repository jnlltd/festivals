package ie.festivals

import com.sun.media.jai.codec.FileSeekableStream
import com.sun.media.jai.codec.SeekableStream
import grails.plugin.cache.Cacheable
import groovyx.net.http.HTTPBuilder

import javax.annotation.PostConstruct
import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.swing.*
import java.awt.*
import java.awt.color.CMMException
import java.awt.image.BufferedImage
import java.awt.image.renderable.ParameterBlock

import static groovyx.net.http.ContentType.BINARY
import static groovyx.net.http.Method.HEAD
import static org.apache.commons.io.FilenameUtils.getExtension

class ImageService {

    static transactional = false

    private String appVersion
    private String artistImageDir

    def grailsApplication
    private final Toolkit toolkit = Toolkit.defaultToolkit

    @PostConstruct
    private init() {
        appVersion = grailsApplication.metadata.'app.version'
        artistImageDir = grailsApplication.config.festival.images.artistDir
    }

    private boolean isJPEG(String imageExtension) {
        imageExtension?.toLowerCase() in ['jpg', 'jpeg']
    }

    @Cacheable('image')
    BufferedImage read(String remoteImageURL) throws IOException {

        // I was previously reading the image with ImageIO.read(imageLocation) but this didn't work in production (only)
        // with some images http://stackoverflow.com/questions/11742598/reading-image-from-url
        Image image = toolkit.createImage(remoteImageURL.toURL())
        toBufferedImage(image)
    }


    private BufferedImage toBufferedImage(Image image) {

        if (image instanceof BufferedImage) {
            return image
        }
        // ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).image

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB)
        Graphics graphics = bufferedImage.graphics
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()
        bufferedImage
    }

    @Cacheable('image')
    BufferedImage readLocalImage(String relativePath) throws IOException {

        File localImageFile = new File(artistImageDir, relativePath)
        log.debug "Reading image from local file system at $localImageFile"

        try {
            ImageIO.read(localImageFile)

        } catch (CMMException ex) {
            log.warn "Failed to read image file at $localImageFile.absolutePath"

            // if it's a JPEG, try reading it with JAI instead:
            // http://stackoverflow.com/questions/4470958/why-does-loading-this-jpg-using-javaio-give-cmmexception
            String extension = getExtension(localImageFile.name)

            if (isJPEG(extension)) {
                readWithJAI(new FileSeekableStream(localImageFile))

            } else {
                throw ex
            }
        }
    }

    private BufferedImage readWithJAI(SeekableStream stream) {
        ParameterBlock pb = new ParameterBlock()
        pb.add(stream)
        JAI.create("jpeg", pb).asBufferedImage
    }

    boolean isValidImageUrl(String url) {

        if (!url) {
            return false
        }
        def http = new HTTPBuilder(url)
        log.debug "Validating image URL: $url"
        Integer timeout = 2500

        try {
            def result = false

            // use the HEAD method, because we don't want to actually download the image, just know if the URL is valid
            http.request(HEAD, BINARY) { req ->
                headers.'User-Agent' = "festivals.ie/$appVersion"

                req.params.setParameter("http.socket.timeout", timeout);

                response.success = { resp ->
                    result = true
                }
                response.failure = { resp ->
                    log.error "Error '$resp.statusLine' occurred when attempting to retrieve image from $url"
                }
            }
            result

        } catch (IOException ex) {
            log.warn "Failed to validate image from $url due to error", ex
            false
        }
    }
}
