package ee.vvk.ivotingverification.util;

import android.app.Activity;

import java.security.cert.X509Certificate;

import ee.vvk.ivotingverification.R;

public class EstEidLoader {

    public static X509Certificate[] getEsteidCerts(Activity activity) throws Exception {
        byte[] certData = Util.toBytes(activity.getResources().openRawResource(R.raw.esteid_sk_2015));
        X509Certificate sk2015 = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.esteid2018));
        X509Certificate esteid2018 = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.eid2016));
        X509Certificate eid2016 = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.eid_q_2024e));
        X509Certificate eid2024e = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.eid_q_2024r));
        X509Certificate eid2024r = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.eid_q_2021e));
        X509Certificate eid_q_2021e = Util.loadCertificate(certData);

        certData = Util.toBytes(activity.getResources().openRawResource(R.raw.eid_q_2021r));
        X509Certificate eid_q_2021r = Util.loadCertificate(certData);

        return new X509Certificate[]{
                sk2015,
                esteid2018,
                eid2016,
                eid2024e,
                eid2024r,
                eid_q_2021e,
                eid_q_2021r
        };
    }
}
