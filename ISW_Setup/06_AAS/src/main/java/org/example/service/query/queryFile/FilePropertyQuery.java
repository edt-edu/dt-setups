package org.example.service.query.queryFile;

/**
 * Represents a query for a file property
 */
public class FilePropertyQuery {
    /**
     * Supported comparison operators for file property queries
     */
    public enum Operator {
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        GREATER_THAN_EQUALS(">="),
        LESS_THAN("<"),
        LESS_THAN_EQUALS("<="),
        LIKE("LIKE");
        
        private final String symbol;
        
        Operator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        /**
         * Get operator from symbol
         * @param symbol The operator symbol
         * @return The matching operator, or EQUALS if not found
         */
        public static Operator fromSymbol(String symbol) {
            for (Operator op : values()) {
                if (op.getSymbol().equals(symbol)) {
                    return op;
                }
            }
            return EQUALS; // Default to equals
        }
    }
    
    private final String propertyPath;
    private final Object queryValue;
    private final Operator operator;
    
    public FilePropertyQuery(String propertyPath, Object queryValue) {
        this(propertyPath, queryValue, Operator.EQUALS);
    }
    
    public FilePropertyQuery(String propertyPath, Object queryValue, Operator operator) {
        this.propertyPath = propertyPath;
        this.queryValue = queryValue;
        this.operator = operator;
    }
    
    public String getPropertyPath() {
        return propertyPath;
    }
    
    public Object getQueryValue() {
        return queryValue;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    @Override
    public String toString() {
        return propertyPath + " " + operator.getSymbol() + " " + queryValue;
    }
}
