package storage;

import loan.Loan;
import loan.LoanAgreement;
import loan.LoanAgreementBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoanCsvStore {
    private static final String HEADER =
            "id,accountId,borrower,amount,months,rate,guarantor,insured,monthsPaid";
    private static final String SEPARATOR = ",";
    private static final String NO_GUARANTOR = "-";

    private final Path file;

    public LoanCsvStore(Path file) {
        this.file = file;
    }

    public void saveAll(Collection<Loan> loans) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Loan loan : loans) {
            lines.add(toLine(loan));
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot save loans", e);
        }
    }

    public List<Loan> loadAll() {
        if (Files.notExists(file)) {
            return List.of();
        }
        try (Stream<String> lines = Files.lines(file)) {
            return lines.skip(1)
                    .map(this::parse)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load loans", e);
        }
    }

    private String toLine(Loan loan) {
        LoanAgreement agreement = loan.getAgreement();
        return String.join(SEPARATOR,
                loan.getId(),
                loan.getAccountId(),
                agreement.getBorrower(),
                String.valueOf(agreement.getAmount()),
                String.valueOf(agreement.getMonths()),
                String.valueOf(agreement.getInterestRate()),
                Objects.requireNonNullElse(agreement.getGuarantor(), NO_GUARANTOR),
                String.valueOf(agreement.isInsured()),
                String.valueOf(loan.getMonthsPaid()));
    }

    private Loan parse(String line) {
        String[] parts = line.split(SEPARATOR);
        LoanAgreementBuilder builder = new LoanAgreementBuilder(
                parts[2], Double.parseDouble(parts[3]), Integer.parseInt(parts[4]))
                .interestRate(Double.parseDouble(parts[5]));
        if (!parts[6].equals(NO_GUARANTOR)) {
            builder.guarantor(parts[6]);
        }
        if (Boolean.parseBoolean(parts[7])) {
            builder.withInsurance();
        }
        return new Loan(parts[0], parts[1], builder.build(), Integer.parseInt(parts[8]));
    }
}