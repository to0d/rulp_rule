package alpha.rulp.ximpl.entry;

public class REntryFactory {

	public static IREntryIteratorBuilder defaultBuilder() {
		return new XREntryIteratorBuilderDefault();
	}
	
	public static IREntryIteratorBuilder reverseBuilder() {
		return new XREntryIteratorBuilderReverse();
	}
}
