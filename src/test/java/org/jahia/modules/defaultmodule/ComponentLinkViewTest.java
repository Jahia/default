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
 * Reads the {@code jnt:componentLink} default view and asserts how it writes the attributes of the
 * wrapper and of the link it generates.
 *
 * <p>The view is read from its own resource, so the assertions follow the shipped file rather than a
 * copy of it. A JSP needs a servlet container to render, so the test checks each attribute value for
 * the encoder that keeps the value inside it.
 */
public final class ComponentLinkViewTest {

    private static final String VIEW = "/jnt_componentLink/html/componentLink.jsp";
    private static final Pattern ATTRIBUTE = Pattern.compile("\\s(class|title|href)=\"([^\"]*)\"");
    private static final Pattern ENCODED = Pattern.compile("\\$\\{fn:escapeXml\\([^}]+\\)}");

    private static String view() throws IOException {
        try (InputStream in = ComponentLinkViewTest.class.getResourceAsStream(VIEW)) {
            assertNotNull("view resource " + VIEW + " is on the classpath", in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String[]> attributes() throws IOException {
        List<String[]> attributes = new ArrayList<>();
        Matcher matcher = ATTRIBUTE.matcher(view());
        while (matcher.find()) {
            attributes.add(new String[]{matcher.group(1), matcher.group(2)});
        }
        return attributes;
    }

    @Test
    public void theViewWritesTheWrapperClassAndTheLinkTitleClassAndHref() throws IOException {
        List<String> names = new ArrayList<>();
        for (String[] attribute : attributes()) {
            names.add(attribute[0]);
        }
        assertEquals(List.of("class", "title", "class", "href"), names);
    }

    @Test
    public void everyAttributeValueIsEncodedForTheAttribute() throws IOException {
        for (String[] attribute : attributes()) {
            assertTrue(attribute[0] + " writes its value through fn:escapeXml, found: " + attribute[1],
                    ENCODED.matcher(attribute[1]).matches());
        }
    }
}
