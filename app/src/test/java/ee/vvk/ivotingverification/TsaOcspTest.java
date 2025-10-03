package ee.vvk.ivotingverification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ee.vvk.ivotingverification.util.BDocContainer;
import ee.vvk.ivotingverification.util.Ocsp;
import ee.vvk.ivotingverification.util.Pkix;

public class TsaOcspTest {

    private final int MS_IN_SEC = 1000;
    private final Map<String, Boolean> params = new HashMap<>() {{
        put("not-ok", false); // ocsp before tsa
        put("ok-0-diff", true); // tsa in the same sec as ocsp
        put("ok-2-diff", true); // tsa 2 sec before ocsp
        put("ok-10-diff", false); // tsa 10 sec before ocsp
    }};

    @Test
    public void testUsingMultipleTsaOcspScenarios() throws Exception {
        File tsaOcspDir = new File(Objects.requireNonNull(getClass().getClassLoader()).getResource("tsaocsp").toURI());
        assertTrue(tsaOcspDir.exists() && tsaOcspDir.isDirectory());

        File[] subDirs = tsaOcspDir.listFiles(File::isDirectory);
        assertNotNull(subDirs);

        testPKIXOCSP(subDirs);
    }

    private void testPKIXOCSP(File[] subDirs) {

        for (File subDir : subDirs) {
            String ocspFilePath = null;
            String tsaFilePath = null;

            File[] files = subDir.listFiles(File::isFile);
            assertNotNull(files);
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                if (filePath.endsWith(".ocsp")) {
                    ocspFilePath = filePath;
                } else if (filePath.endsWith(".tspreg")) {
                    tsaFilePath = filePath;
                }
            }
            if (ocspFilePath != null && tsaFilePath != null) {
                processOcspTsa(subDir.getName(), ocspFilePath, tsaFilePath);
            } else {
                System.out.printf("Skipping %s (missing OCSP/TSA file)%n", subDir);
            }
        }
    }

    private void processOcspTsa(String subDirName, String ocspFilePath, String tsaFilePath) {
        try {
            File ocspFile = new File(ocspFilePath);
            File tsaFile = new File(tsaFilePath);

            Ocsp ocsp = new Ocsp(new ByteArrayInputStream(Files.readAllBytes(ocspFile.toPath())));
            Pkix pkix = new Pkix(Files.readAllBytes(tsaFile.toPath()));

            boolean pkixPredatesOcsp = false;
            int maxTimeInSec = 8;
            try {
                pkixPredatesOcsp = BDocContainer.pkixPredatesOcsp(pkix, ocsp, maxTimeInSec * MS_IN_SEC);
            } catch (Exception ex) {
                // no-op
            }

            assertEquals(
                    String.format("Failure: %s. PKIX does not predate OCSP", subDirName),
                    params.get(subDirName),
                    pkixPredatesOcsp
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
