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
 * Reads the {@code jnt:template} default view and asserts how it writes the page metadata into the
 * {@code <meta>} tags of the page head.
 *
 * <p>The view is read from its own resource, so the assertions follow the shipped file rather than a
 * copy of it. A JSP needs a servlet container to render, so the test checks each {@code content}
 * attribute of a {@code <meta name="…">} tag for the encoder that keeps the value inside it.
 */
public final class TemplateViewMetaTagsTest {

    private static final String VIEW = "/jnt_template/html/template.jsp";
    private static final Pattern META_CONTENT =
            Pattern.compile("<meta\\s+name=\"([^\"]+)\"\\s+content=\"([^\"]*)\"");
    private static final Pattern ENCODED = Pattern.compile("\\$\\{fn:escapeXml\\([^}]+\\)}");

    private static String view() throws IOException {
        try (InputStream in = TemplateViewMetaTagsTest.class.getResourceAsStream(VIEW)) {
            assertNotNull("view resource " + VIEW + " is on the classpath", in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String[]> namedMetaTags() throws IOException {
        List<String[]> tags = new ArrayList<>();
        Matcher matcher = META_CONTENT.matcher(view());
        while (matcher.find()) {
            tags.add(new String[]{matcher.group(1), matcher.group(2)});
        }
        return tags;
    }

    @Test
    public void theViewWritesTheDescriptionAuthorAndKeywordsMetaTags() throws IOException {
        List<String> names = new ArrayList<>();
        for (String[] tag : namedMetaTags()) {
            names.add(tag[0]);
        }
        assertEquals(List.of("description", "author", "keywords"), names);
    }

    @Test
    public void everyMetaTagValueIsEncodedForTheAttribute() throws IOException {
        for (String[] tag : namedMetaTags()) {
            assertTrue("meta " + tag[0] + " writes its value through fn:escapeXml, found: " + tag[1],
                    ENCODED.matcher(tag[1]).matches());
        }
    }
}
