package exceptions;

public class InvalidTransactionException extends Exception {
	public InvalidTransactionException(int tid) {
		super ("Invalid transaction: " + tid + ".");
	}
}
