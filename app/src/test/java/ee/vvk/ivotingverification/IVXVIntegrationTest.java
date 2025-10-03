package ee.vvk.ivotingverification;

import static org.junit.Assert.assertEquals;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.crypto.x509.X509PublicKey;

@RunWith(RobolectricTestRunner.class)
public class IVXVIntegrationTest {

    private final String PEM_BEGIN = "-----BEGIN PUBLIC KEY-----\n";
    private final String PEM_END = "\n-----END PUBLIC KEY-----";

    @Test
    public void testUsingMultipleJsonFixtures() throws Exception {
        // Directory containing JSON test files
        File testCasesDir = new File(Objects.requireNonNull(getClass().getClassLoader()).getResource("test_cases").toURI());

        // Iterate over all JSON files in the directory
        File[] testFiles = testCasesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (testFiles != null) {
            for (File file : testFiles) {

                System.out.println("Processing: " + file.getName());

                // Read JSON content from the file
                String jsonContent = new String(Files.readAllBytes(Paths.get(file.toURI())));
                JSONObject testCase = new JSONObject(jsonContent);

                // Extract test case data
                String electionId = testCase.getString("election_id");
                String publicKeyB64 = testCase.getString("public_key");
                JSONArray encArray = testCase.getJSONArray("encryptions");
                JSONObject encObject = encArray.getJSONObject(0);
                String ciphertextB64 = encObject.getString("ciphertext");
                String randomB64 = encObject.getString("random");
                String plaintextRaw = encObject.getString("plaintext_raw");
                String plaintextEncodedB64 = encObject.getString("plaintext_encoded");

                checkEncryption(publicKeyB64, plaintextRaw, ciphertextB64, randomB64);
                System.out.println("checkEncryption OK");
                checkDecoding(electionId, publicKeyB64, plaintextRaw, plaintextEncodedB64);
                System.out.println("checkDecoding OK");
            }
        } else {
            System.out.println("No files to process");
        }
        System.out.println("Finished test");
    }

    private void checkEncryption(String publicKeyB64, String plaintextRaw, String ciphertextB64, String randomB64) throws IOException {
        DecrypterWithRandomness decrypter;
        decrypter = X509PublicKey.parseX509PEMPublicKey(PEM_BEGIN + publicKeyB64 + PEM_END);
        byte[] ciphertext = Base64.decode(ciphertextB64, Base64.DEFAULT);
        byte[] random = Base64.decode(randomB64, Base64.DEFAULT);
        String result = decrypter.decryptWithRandomness(ciphertext, random);
        assertEquals("Plaintext does not match", result, plaintextRaw);
    }

    private void checkDecoding(String electionId, String publicKeyB64, String plaintextRaw, String plaintextEncodedB64) throws IOException {
        DecrypterWithRandomness decrypter;
        decrypter = X509PublicKey.parseX509PEMPublicKey(PEM_BEGIN + publicKeyB64 + PEM_END);
        assertEquals("Election identifier does not match", decrypter.getElectionID(), electionId);
        byte[] plaintextEncoded = Base64.decode(plaintextEncodedB64, Base64.DEFAULT);
        String plaintextOut = decrypter.decodePlaintext(plaintextEncoded);
        assertEquals("Plaintext encoding does not match", plaintextRaw, plaintextOut);
    }
}