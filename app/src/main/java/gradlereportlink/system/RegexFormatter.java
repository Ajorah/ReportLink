/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gradlereportlink.system;

/**
 *
 * @author CoPilot?
 */

public class RegexFormatter {
    public static String sanitize(String input) {
        // Replace all invalid filename/folder characters with underscore
        // Invalid: <>:"/\|?* and ASCII control chars (0-31), plus trailing dot/space
        String sanitized = input.replaceAll("[<>:\"/\\\\|?*\\x00-\\x1F]", "_")
                                .replaceAll("[\\. ]+$", "_");
        // Windows reserved names (CON, PRN, AUX, NUL, COM1-9, LPT1-9) are not handled here
        return sanitized;
    }
}