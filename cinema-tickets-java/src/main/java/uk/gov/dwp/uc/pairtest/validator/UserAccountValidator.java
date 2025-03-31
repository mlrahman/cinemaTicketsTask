package uk.gov.dwp.uc.pairtest.validator;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class UserAccountValidator {
    public void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID: " + accountId);
        }
    }
}
