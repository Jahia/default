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
 * Reads the two {@code jnt:manageRoles} flow views and asserts how the revoke button hands the
 * principal and the role to the script.
 *
 * <p>A principal name and a role name may both contain an underscore, so the button carries each
 * value in its own attribute, and the script reads those attributes instead of splitting the id.
 */
public final class ManageRolesViewTest {

    private static final List<String> VIEWS = List.of(
            "/jnt_manageRoles/html/manageRoles.flow/view.jsp",
            "/jnt_manageRoles/html/manageRoles.settingsBootstrap3GoogleMaterialStyle.flow/view.jsp");
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
}
