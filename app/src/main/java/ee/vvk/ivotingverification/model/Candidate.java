package ee.vvk.ivotingverification.model;

import ee.vvk.ivotingverification.util.C;

public class Candidate {
    public final static Candidate NO_CHOICE = new Candidate("", "", C.noChoice);

    public final String number;
    public final String party;
    public final String name;

    public Candidate(String number) {
        String code = "";
        String name = "";
        String party = C.noChoice;

        for (Candidate c : C.candidateList) {
            if (c.number.equals(number)) {
                code = c.number;
                name = c.name;
                party = c.party;
            }
        }

        if (code.isEmpty())
            throw new IllegalArgumentException("Bad candidate format: " + number);

        this.number = code;
        this.name = name;
        this.party = party;
    }

    public Candidate(String number, String party, String name) {
        this.number = number;
        this.party = party;
        this.name = name;
    }
}
