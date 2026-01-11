package gradlereportlink.system;

import java.io.OutputStream;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class Storage {

    public static boolean WriteToSMB(boolean isPDF, String path, String filename, byte[] data) { // Returns successful data 'filename', isPDF ? writeTo(path) : writeTo(otherPath);
        System.setProperty("jcifs.smb.client.responseTimeout", "120000"); // 2 minutes
        System.setProperty("jcifs.smb.client.soTimeout", "180000"); // 3 minutes
        System.setProperty("jcifs.smb.client.connTimeout", "15000"); // 15 seconds
        System.setProperty("jcifs.smb.client.attrExpirationPeriod", "0"); // Disable attribute caching
        System.setProperty("jcifs.smb.client.dfs.disabled", "true"); // Avoid DFS lookups
        String domain = System.getenv("RPT_SMB_DOMAIN");
        String username = System.getenv("RPT_SMB_USERNAME");
        String password = System.getenv("RPT_SMB_PASSWORD");
        String smbRoot = System.getenv("RPT_REPORTS_ROOT");
        smbRoot = smbRoot.endsWith("/") ? smbRoot : smbRoot + "/"; // Appends trailing slash if doesn't exist.
        String type = isPDF ? "/Reports/" : "/Pictures/"; // Determines filetype for folder placement/creation
        String smbUrl = System.getenv("RPT_SMB_URL") + smbRoot + path + type + filename; // Creates the full filename path
        CIFSContext authedContext = SingletonContext.getInstance() // Authentication context
            .withCredentials(new NtlmPasswordAuthenticator(domain, username, password)); // Required credentials
            
        int lastSlash = smbUrl.lastIndexOf('/'); // Check index of path having a trailing slash 
        try {
            if (lastSlash > "smb://".length()) { // If last slash exists,
                String dirUrl = smbUrl.substring(0, lastSlash + 1); // Begin index to the location of past last slash
                SmbFile smbFile = new SmbFile(dirUrl, authedContext); // Full file path and authorization details
                if (!smbFile.exists()) { // If it doesn't exist
                    try (OutputStream out = new SmbFileOutputStream(smbFile)) { // Try writing file to file path
                        smbFile.mkdirs(); // Make the folders
                        out.write(data); // Write the data
                        out.close(); // Close the thread
                        return true;
                    } catch (Exception e) {
                        if (!smbFile.exists()) { // If mkdirs fails, check again if it now exists (race condition safe)
                            e.printStackTrace(); // Error Log
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Error log in case-of
            return false;
        } return false;
    }

}