package ee.vvk.ivotingverification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Base64;
import org.robolectric.RobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import ee.vvk.ivotingverification.common.crypto.x509.X509PublicKey;
import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;

@RunWith(RobolectricTestRunner.class)
public class ASN1StructureTest {

    private final String correctVoteEncoded =
            """
                    MIIDGjALBgkrBgEEAZdVAgEwggMJAoIBgFsd8EkKe2dSHgjIzoM5a3Wk+XlpwmGygFOD31eVSSg8
                    xMiZ8u4e1RL2pYoF/EmpxiR7d9veMb3cNaMr65WnM6iGIOklPHMvBS2VsepXK90wWUbpNTk1y5bS
                    bnEVOYEc089es4hb6OWjE7SQzgYONXn2c/wKeNuIasrew99m2luCSNF42Bc3AUQNroF5nlEfSjQ6
                    BNojHaeNmcitEFNn2zBMAcnWJ09ZY9+LYfolLw9FXuFG9iXknmVDoA72Hyw5FQqGENmxylDiFZEp
                    2Aa17x7AIt5/RzcJiC5TouzO8leyji811Nk/7levexOCY0iO7MoPESyoUew97CIT9XELuqaa0BWK
                    6OU76TzvLfOw1o5P1WbOImRAqNDA70aXw4syoiKLkL+tITceXOdztGVf9amNYv+7UdI3sLwgvXJu
                    L7H1JhUFtIwFvIfGtjBfkz9j0p7NE6uq+4yr+eHP4VTBBA9HOFq6taWb7BopLsZb9f5oB+4L7uK4
                    EfcKspPhO/Xp2wKCAYEA/xcMToX3/C2JiOiAq2Vh8PMGZ0P6PpMRarDFdwSULncjcfbBUC3eaSfy
                    tVvcceZfI1eSaXl3RtBme47CpSb4cRWdwTG2QyijuutD6R36Q5ulO1sjInukZbopU/4uAfMrSZDH
                    0nqO1QTVjLUfYelft7f3GrI8D0+g9ItikRn+Hlu1XQaJAgB0yC9joYu47x99wD1UamPCJgm9TEgY
                    UFtgi2/fpjc6X1ljq3/2bzj7pqS9WnZ0hQypSdrTG5iq4EaxbI3aMen69EDJb69SNx4dtYleUvSC
                    WuuV+5ZIH38NRKwG3jRz/WURL+W3oIADATBgbhtchtTPkoPYiJm01cZNpMThuOnG3uuQYd6rZCxo
                    3spu4IFoUXUQ6s2iZtfzyJ2Mj8uHNtOgpZseqU5daMi8TbnLoHMRLO7nFXCIpTDvzohqn9k4/kvt
                    DcSeFryXjC1qAXKmxhxeRyIIAPcfVBLiPjjFNYb5zeA1vgwvRACOcfl5Vwtryp1hvPCu3Z3SGqGH""";

    private final String incorrectVoteEncoded =
            """
                    MIIENzCCAicGCSsGAQQBl1UCATCCAhgCggEBAPWeqWgtFnkimfjVE7OMvC5IZbhvPrWSqQEHtA01
                    jCOINS3Xi4yvnNIuKJa4Q+laXWkeySiSWMyDMlRwriDKui4zV8mTx5N534rLb34vz+mxq7laOkoM
                    76YJ6QULaM7ufHrBaRyXod2pi13Sd81Eh5YAouZNQlbQiqEzWqe8UO461K4jJio8JtFA//6DDGfw
                    2v/Z8Mk9742uDBez6h/48AWKtozezA0SufK8RdpZKJx0vxWToTxtiO38PZNR2t9kl+oR5GLw37sH
                    W5DFxV4s189p2GaUffogsl92pz3bMyoPGuQR45dPI0GKYwMBBdgP6omRH5nJkXlp9BwzAVOQifcC
                    ggEBAJ7nY0lp16PGi6Z5cvQpw/LFTbQBU8zIrM/UH0TDfvuyHf0et9LiALh2vXnidRspwmRGma9X
                    tFRQFV+q8z+Z4i45Fmfo/OEXRVd0YA7ZtVIhZF9/cOesn/L0AirTVR3VndnLYNvh7yFnSVrrvvWQ
                    5/9HEIrJBDCiDZ8zVANew5pe027EdHXDRUBWNSO4AYtKNdx0FufS1thoJr7ppVJ/xVKvbexqWgiR
                    Yy07RDwheEfQE7khgA27EbXb1gXeYczKbTkgaohR42YubFAXodRotZPk6rFV7H/Yj+IKWLBRHg7p
                    MsSO0rTcHmbDD1ykMjJQzdy39Fqii3L0K+lSLEpLpLMbDERJUkUtU1RSQUlUUzCCAggCggEAbvx4
                    7ltvLPSeQcZMQN/GwNZaeE2GresfIjd9F6hP2PX5eNjYEmzdzcA5mryd2UMq4DSvQi6e30fWGq8O
                    TeeDgg3RycA5JzR+s+DdvttultpEESY7iIW+TxJ5pKvAxY7lv1KWgTLL7uCRc/nfqjwybKtNGN1q
                    0PZCMz36BcGohG6ibIME6Vwf2t0SVl7x5JZ1jfF3YICrb2nlRwh6qz9nCNc+p1rdfpXCv+NOE9Rw
                    dQG2rO+UEgN+2Qo7b6m8CtHbj5dlKrACAazmuyeEVizTLJ8q37ihmOoUP8eJbKB+qAGgX8rg0z35
                    6LPfh9WFIj7dtae40reMp4zCx+BCcM6KOwKCAQARX2rGzgD9LxzytJEjSMsnvJzhYH/qdZntWJQP
                    8vP9yzykTzyWyhtrjEjTBUJjbameukzvkxT1pfHSzQJdC8SJImP9K/u/6ANQIyWlzEpw399ryOXy
                    n/7sGOsc3j3dmulrwkdQ+1kflSxS4ijdQ1QTMU6kpWDZUTY9sW4ieQLOLrJxZNrWKwW+WigYJsFN
                    wPE6pI+BELVboQAkcHZmCVkOBGwASWAm7Th669xxalX+Z6nhcPjRh4sGvN8lDJ6bsMlkd3bBahUa
                    6CV6bY7EugloOM5ENfNKhxLSYMDRv7meGVBIol+O6df8j3mSw0ixCvE+t6jwDPbuCAk2wzIV740l""";

    private final String publicKey =
            """
                    -----BEGIN PUBLIC KEY-----
                    MIIDOjCCAakGCSsGAQQBl1UCATCCAZoCggGBAP//////////yQ/aoiFowjTExmKLgNwc0Sk\
                    CTgiKZ8x0Agu+pjsTmyJRSgh5jjQE3e+VGbPNOkMbMCsKbfJfFDdP4TVtbVHCReSFtXZiXn7G9Ex\
                    C6aY37WsL/1y29Aa37e44a/taiZ+lrp8kEXxLH+ZJKGZR7ORbPcIAfLihY78FmNpINhxV05ppFj+\
                    o/STPX4NlXSPco62WHGLzViCFUrue1SkHcJaWbWcMNU5KvJgE8XRsCMoYIXwykF5GLjbOO+Oedyw\
                    YDoYDmyeDouwHoo+1xV3wb0xSyd4ry/aVWBcYOZVJfOqVauUV0iYYmPoFEBVyjlqKqsQtrTMXDQR\
                    QejOoVSGr3xy6ZOz7hQRY2+8KiupxV10GDH2zlw+FpuHkx6v1rozbCTPXHoyU4EolYZ3O49ImGtL\
                    ua/Ev+gbZighk2HYCcz7IamRSHysYF3sgDLvhF1d6YV1sdwmIwLrZRuII4k+gdOWrMUPbW/zg/RC\
                    OS4LRIKk60sr//////////wIBAhsQRVBfMjAyNF9FSFNfREVNTwOCAYkAMIIBhAKCAYBuQOrtiv0\
                    uUWsSrWSwe8lY+s1FkjIAJaizYdGE6NxkZWQ55NEd4XL5OJs4/qlf3n+Tr7qXVNZfI74m4LOHCv9\
                    QlK76TlFubePTMokZyjeWRXI04v9k1C20llNizhuk+2XVayZD+Rh5oFnljkjOW7QD6HR0Gy1hSOu\
                    awB4qEGRTCBtYYSkTVzUbJLsos+YKhhyAOku1oaaan4uM/e2oQoXnM0FTBgVtyUP58UgewWGNzLv\
                    xHEXw49UUBTVyq1eXim1dhzcaabJ6HCBirtR/0Xbx9DAPoSLRvb86iKz5bbfA7lQ0cVn4t/LtZEs\
                    uPNlC77rSIgZQeG3q49CU4DGW/Quj9M+Ft2SZ9JYC2fsvrBs2QrANim+P/Pm84g1aj8735k4uR5o\
                    gTuufMOEaIw/hOi02qASvI7LfyakyK0JHHyj3V1iaJKkfu9v8mUXQFJsv5iXrSNd7Qooj37/c/Dp\
                    Qe/4qAFOtjjDoeeaBhRZ9o3MRB7gKXYWeLalGbIfldPTyaac=
                    -----END PUBLIC KEY-----""";

    @Test
    public void testCorrectVote() throws Exception {
        byte[] vote = Base64.decode(correctVoteEncoded, Base64.DEFAULT);
        DecrypterWithRandomness elGamalPub = X509PublicKey.parseX509PEMPublicKey(publicKey);
        boolean isCorrect = elGamalPub.checkAsn1Structure(vote);
        assertTrue(isCorrect);
    }

    @Test
    public void testIncorrectVote() throws Exception {
        byte[] vote = Base64.decode(incorrectVoteEncoded, Base64.DEFAULT);
        DecrypterWithRandomness elGamalPub = X509PublicKey.parseX509PEMPublicKey(publicKey);
        boolean isCorrect = elGamalPub.checkAsn1Structure(vote);
        assertFalse(isCorrect);
    }
}
