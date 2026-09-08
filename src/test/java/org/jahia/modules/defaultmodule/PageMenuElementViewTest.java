package org.jahia.modules.defaultmodule;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Renders the {@code jnt:page} menu-element view and asserts what it writes into the link it
 * generates — the link text and the {@code title} attribute.
 *
 * <p>The view is evaluated from its own resource, so the assertions follow the shipped file rather
 * than a copy of it. {@code currentNode} is a stand-in exposing the three accessors the view reads;
 * the view resolves them by name, the way the render engine's binding does.
 */
public final class PageMenuElementViewTest {

    private static final String VIEW = "/jnt_page/html/page.menuElement.groovy";
    private static final String URL_VALUE = "/sites/mysite/home/page.html";

    /** Stands in for the bound {@code currentNode}: the view reads these three accessors only. */
    public static final class Page {
        private final String displayableName;
        private final Map<String, Object> properties;

        Page(String displayableName, String description) {
            this.displayableName = displayableName;
            this.properties = description == null
                    ? Collections.emptyMap()
                    : Collections.singletonMap("jcr:description", (Object) new Text(description));
        }

        public String getDisplayableName() {
            return displayableName;
        }

        public String getUrl() {
            return URL_VALUE;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    /** Stands in for a single-valued property: the view reads {@code description.string}. */
    public static final class Text {
        private final String value;

        Text(String value) {
            this.value = value;
        }

        public String getString() {
            return value;
        }
    }

    private static String render(String displayableName, String description) {
        URL view = PageMenuElementViewTest.class.getResource(VIEW);
        assertNotNull("view resource " + VIEW + " is on the classpath", view);

        StringWriter rendered = new StringWriter();
        Binding binding = new Binding();
        binding.setVariable("currentNode", new Page(displayableName, description));
        binding.setVariable("out", new PrintWriter(rendered));

        GroovyShell shell = new GroovyShell(PageMenuElementViewTest.class.getClassLoader(), binding);
        try {
            shell.evaluate(view.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("evaluating " + VIEW, e);
        }
        return rendered.toString();
    }

    @Test
    public void plainValuesAreRenderedAsGiven() {
        assertEquals("<a href=\"" + URL_VALUE + "\" title=\"About the team\">About us</a>",
                render("About us", "About the team"));
    }

    @Test
    public void aPageWithoutADescriptionRendersNoTitleAttribute() {
        assertEquals("<a href=\"" + URL_VALUE + "\">About us</a>", render("About us", null));
    }

    @Test
    public void specialCharactersInTheLinkTextAreEncoded() {
        assertEquals("<a href=\"" + URL_VALUE + "\">Sales &amp; Marketing &lt;EMEA&gt;</a>",
                render("Sales & Marketing <EMEA>", null));
    }

    @Test
    public void specialCharactersInTheTitleAttributeAreEncoded() {
        assertEquals("<a href=\"" + URL_VALUE + "\" title=\"Sales &amp; Marketing &lt;EMEA&gt;\">Team</a>",
                render("Team", "Sales & Marketing <EMEA>"));
    }

    @Test
    public void aQuoteInTheTitleAttributeStaysInsideTheAttribute() {
        assertEquals("<a href=\"" + URL_VALUE + "\" title=\"the &#034;core&#034; team\">Team</a>",
                render("Team", "the \"core\" team"));
    }

    @Test
    public void aQuoteInTheLinkTextIsEncoded() {
        assertEquals("<a href=\"" + URL_VALUE + "\">the &#034;core&#034; team</a>",
                render("the \"core\" team", null));
    }

    @Test
    public void anApostropheIsEncodedInBothPositions() {
        assertEquals("<a href=\"" + URL_VALUE + "\" title=\"Ren&#039;s desk\">Ren&#039;s team</a>",
                render("Ren's team", "Ren's desk"));
    }
}
