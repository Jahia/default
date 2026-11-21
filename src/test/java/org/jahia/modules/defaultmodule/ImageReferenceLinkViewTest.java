package org.jahia.modules.defaultmodule;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Reads the {@code jnt:imageReferenceLink} default view and asserts what it leaves in the page, in
 * edit mode, when no image has been picked.
 *
 * <p>jContent covers a component that rendered no text with its own "nothing to display" box, so a
 * placeholder word written here would keep the component out of that box in a language it has not
 * been translated into. The placeholder therefore carries height and no text. A JSP needs a servlet
 * container to render, so the test reads the shipped view rather than its output.
 */
public final class ImageReferenceLinkViewTest {

    private static final String VIEW = "/jnt_imageReferenceLink/html/imageReferenceLink.jsp";
    private static final Pattern EDIT_MODE_BRANCH =
            Pattern.compile("<c:if test=\"\\$\\{renderContext\\.editMode}\">(.*?)</c:if>", Pattern.DOTALL);
    private static final Pattern JSP_COMMENT = Pattern.compile("<%--.*?--%>", Pattern.DOTALL);
    private static final Pattern EMPTY_ELEMENT_WITH_HEIGHT =
            Pattern.compile("<(\\w+)[^>]*min-height[^>]*>\\s*</\\1>");

    private static String view() throws IOException {
        try (InputStream in = ImageReferenceLinkViewTest.class.getResourceAsStream(VIEW)) {
            assertNotNull("view resource " + VIEW + " is on the classpath", in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String editModePlaceholder() throws IOException {
        Matcher matcher = EDIT_MODE_BRANCH.matcher(view());
        assertTrue("the view has an edit-mode branch for a reference with no image", matcher.find());
        return JSP_COMMENT.matcher(matcher.group(1)).replaceAll("").trim();
    }

    @Test
    public void theEditModePlaceholderIsAnEmptyElementWithAHeight() throws IOException {
        String placeholder = editModePlaceholder();
        assertTrue("the placeholder is an element with a minimum height and nothing inside, found: " + placeholder,
                EMPTY_ELEMENT_WITH_HEIGHT.matcher(placeholder).matches());
    }

    @Test
    public void theEditModePlaceholderWritesNoWord() throws IOException {
        String placeholder = editModePlaceholder();
        assertFalse("the placeholder resolves no message from a resource bundle, found: " + placeholder,
                placeholder.contains("fmt:message"));
        assertFalse("the placeholder writes out no expression, found: " + placeholder,
                placeholder.contains("${"));
    }
}
