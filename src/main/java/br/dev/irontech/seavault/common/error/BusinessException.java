package br.dev.irontech.seavault.common.error;

public class BusinessException extends AppException {
    public BusinessException(String message) { super(message); }
    @Override public int status() { return 422; }
    @Override public String code() { return "BUSINESS_RULE"; }
}
