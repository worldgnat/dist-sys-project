package exceptions;

public class TransactionAbortedException extends Exception {
	public TransactionAbortedException(int tid) {
		super("Transaction " + tid + " was aborted.");
	}
}
