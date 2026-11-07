package loan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanAgreementBuilderTest {

    @Test
    void buildsAgreementWithDefaults() {
        LoanAgreement agreement = new LoanAgreementBuilder("gulim", 5000, 12).build();

        assertEquals("gulim", agreement.getBorrower());
        assertEquals(5000.0, agreement.getAmount());
        assertEquals(12, agreement.getMonths());
        assertEquals(12.0, agreement.getInterestRate());
        assertNull(agreement.getGuarantor());
        assertFalse(agreement.isInsured());
    }

    @Test
    void buildsAgreementWithAllOptions() {
        LoanAgreement agreement = new LoanAgreementBuilder("gulim", 5000, 12)
                .interestRate(10.5)
                .guarantor("aiymay")
                .withInsurance()
                .build();

        assertEquals(10.5, agreement.getInterestRate());
        assertEquals("aiymay", agreement.getGuarantor());
        assertTrue(agreement.isInsured());
    }

    @Test
    void rejectsNegativeAmount() {
        LoanAgreementBuilder builder = new LoanAgreementBuilder("gulim", -100, 12);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsZeroMonths() {
        LoanAgreementBuilder builder = new LoanAgreementBuilder("gulim", 5000, 0);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void rejectsGuarantorWithComma() {
        LoanAgreementBuilder builder = new LoanAgreementBuilder("gulim", 5000, 12);

        assertThrows(IllegalArgumentException.class, () -> builder.guarantor("a,b"));
    }

    @Test
    void rejectsBlankGuarantor() {
        LoanAgreementBuilder builder = new LoanAgreementBuilder("gulim", 5000, 12);

        assertThrows(IllegalArgumentException.class, () -> builder.guarantor("  "));
    }

    @Test
    void descriptionMentionsGuarantorAndInsurance() {
        LoanAgreement agreement = new LoanAgreementBuilder("gulim", 5000, 12)
                .guarantor("aiymay")
                .withInsurance()
                .build();

        String text = agreement.toString();

        assertTrue(text.contains("guarantor: aiymay"));
        assertTrue(text.contains("insured"));
    }
}