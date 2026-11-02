package org.jahia.modules.defaultmodule;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Reads the {@code jnt:manageRoles} flow views of both variants and asserts how they write the
 * role and the principals.
 *
 * <p>A principal name and a role name may both contain an underscore, so the revoke button carries
 * each value in its own attribute, and the script reads those attributes instead of splitting the
 * id. The member screen writes the role and the search text as text, so a quote, an ampersand or an
 * angle bracket in either displays as typed.
 */
public final class ManageRolesViewTest {

    private static final List<String> VIEWS = List.of(
            "/jnt_manageRoles/html/manageRoles.flow/view.jsp",
            "/jnt_manageRoles/html/manageRoles.settingsBootstrap3GoogleMaterialStyle.flow/view.jsp");
    private static final List<String> MEMBER_VIEWS = List.of(
            "/jnt_manageRoles/html/manageRoles.flow/editRoleMembers.jsp",
            "/jnt_manageRoles/html/manageRoles.settingsBootstrap3GoogleMaterialStyle.flow/editRoleMembers.jsp");
    private static final Pattern ROLE_HEADING = Pattern.compile(
            "<h2>(Role: )?\\$\\{fn:escapeXml\\(role\\)}</h2>");
    private static final Pattern SEARCH_VALUE = Pattern.compile(
            "value=\"\\$\\{fn:escapeXml\\(memberSearchCriteria\\.searchString\\)}\"");
    private static final List<Pattern> REVOKE_ATTRIBUTES = List.of(
            Pattern.compile("data-principal=\"\\$\\{fn:escapeXml\\(principalKey\\)}\""),
            Pattern.compile("data-role=\"\\$\\{fn:escapeXml\\(entry\\.key\\.name\\)}\""));
    private static final Pattern REVOKE_CALL = Pattern.compile(
            "revokeRole\\(e\\.currentTarget\\.dataset\\.principal,\\s*e\\.currentTarget\\.dataset\\.role\\)");

    private static String view(String path) throws IOException {
        try (InputStream in = ManageRolesViewTest.class.getResourceAsStream(path)) {
            assertNotNull("view resource " + path + " is on the classpath", in);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void theRevokeButtonCarriesThePrincipalAndTheRoleInTheirOwnAttributes() throws IOException {
        for (String path : VIEWS) {
            String view = view(path);
            for (Pattern attribute : REVOKE_ATTRIBUTES) {
                assertTrue(path + " writes " + attribute + " on the revoke button", attribute.matcher(view).find());
            }
        }
    }

    @Test
    public void theRevokeHandlerReadsTheAttributesInsteadOfSplittingTheId() throws IOException {
        for (String path : VIEWS) {
            assertTrue(path + " passes dataset.principal and dataset.role to revokeRole",
                    REVOKE_CALL.matcher(view(path)).find());
        }
    }

    @Test
    public void theMemberScreenWritesTheRoleHeadingAsText() throws IOException {
        for (String path : MEMBER_VIEWS) {
            assertTrue(path + " writes the role heading with fn:escapeXml", ROLE_HEADING.matcher(view(path)).find());
        }
    }

    @Test
    public void theMemberScreenWritesTheSearchTextAsText() throws IOException {
        for (String path : MEMBER_VIEWS) {
            assertTrue(path + " writes the search field value with fn:escapeXml", SEARCH_VALUE.matcher(view(path)).find());
        }
    }
}
