package org.jahia.modules.defaultmodule;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Reads the {@code jnt:post} default view and asserts how it writes the link to the author of a
 * comment.
 *
 * <p>The view is read from its own resource, so the assertions follow the shipped file rather than a
 * copy of it. A JSP needs a servlet container to render, so the test checks the address and the text
 * of the link for the encoder that keeps each value in its place.
 */
public final class PostViewTest {

    private static final String VIEW = "/jnt_post/html/post.jsp";
    private static final Pattern LINK = Pattern.compile("<a\\s+href=\"([^\"]*)\">([^<]*)</a>");
    private static final Pattern ENCODED = Pattern.compile("\\$\\{fn:escapeXml\\([^}]+\\)}");
    private static final Pattern AUTHOR_URL = Pattern.compile("<c:url var=\"authorUrl\" value='([^']*)'/>");
    private static final Pattern PATH_ENCODED =
            Pattern.compile("\\$\\{url\\.base}\\$\\{functions:escapePath\\([^}]+\\)}\\.html");

    private static String view() throws IOException {
        try (InputStream in = PostViewTest.class.getResourceAsStream(VIEW)) {
            assertNotNull("view resource " + VIEW + " is on the classpath", in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String[]> links() throws IOException {
        List<String[]> links = new ArrayList<>();
        Matcher matcher = LINK.matcher(view());
        while (matcher.find()) {
            links.add(new String[]{matcher.group(1), matcher.group(2)});
        }
        return links;
    }

    @Test
    public void theViewWritesOneLinkToTheAuthor() throws IOException {
        assertEquals(1, links().size());
    }

    @Test
    public void theLinkAddressIsEncodedForTheAttribute() throws IOException {
        for (String[] link : links()) {
            assertTrue("href writes its value through fn:escapeXml, found: " + link[0],
                    ENCODED.matcher(link[0]).matches());
        }
    }

    @Test
    public void theAuthorPathIsEncodedForTheUrl() throws IOException {
        Matcher matcher = AUTHOR_URL.matcher(view());
        assertTrue("the view builds the author link address as authorUrl", matcher.find());
        assertTrue("the author path goes through functions:escapePath, found: " + matcher.group(1),
                PATH_ENCODED.matcher(matcher.group(1)).matches());
    }

    @Test
    public void theLinkTextIsEncodedForTheText() throws IOException {
        for (String[] link : links()) {
            assertTrue("link text writes its value through fn:escapeXml, found: " + link[1],
                    ENCODED.matcher(link[1].trim()).matches());
        }
    }
}
