package testing;

public class ClassComparrisonTest {
	public static void main(String[] args) {
		Object test = new String("Hello, this is a test");
		if (test.getClass().equals(String.class) ) {
			System.out.println("This is a String");
		}
		if (test.getClass().equals(Integer.class)) {
			System.err.println("Something is dumb, and it's probably Java.");
		}
		int f = 4;
		System.out.println("f: " + (f<<4));
		System.out.println("f: " + f);
	}
}
