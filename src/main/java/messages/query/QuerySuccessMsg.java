package messages.query;

import model.Transaction;

public class QuerySuccessMsg extends TransactionMsg {

    // Used for serialization
    private QuerySuccessMsg() {}

    public QuerySuccessMsg(Transaction transaction) {
        super(transaction);
    }
}
