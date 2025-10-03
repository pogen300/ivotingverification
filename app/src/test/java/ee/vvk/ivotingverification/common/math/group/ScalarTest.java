package ee.vvk.ivotingverification.common.math.group;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class ScalarTest {
    @Test
    public void newScalarTest() {
        BigInteger value = new BigInteger("5");
        BigInteger mod = new BigInteger("24");

        new Scalar(value, mod);
    }

    @Test
    public void newScalarValueNullTest() {
        BigInteger mod = new BigInteger("24");

        //noinspection DataFlowIssue
        Exception exception = Assert.assertThrows(NullPointerException.class, () -> new Scalar(null, mod));
        Assert.assertEquals(exception.getMessage(), "Value cannot be null");
    }

    @Test
    public void newScalarModNullTest() {
        BigInteger value = new BigInteger("5");

        //noinspection DataFlowIssue
        Exception exception = Assert.assertThrows(NullPointerException.class, () -> new Scalar(value, null));
        Assert.assertEquals(exception.getMessage(), "Modulo cannot be null");
    }
}