package vt.edu.graph;


public class ReferenceNode {
	/**
	 * Use it when you don't know the type of ReferenceNode
	 */
	public static final int UNKNOWN = 0;
	
	/**
	 * Change a function body
	 */
	public static final int CF = 6;
	
	/**
	 * Add a function
	 */
	public static final int AF = 4;
	
	/**
	 * Delete a function
	 */
	public static final int DF = 5;
	
	/**
	 * Add a variable
	 */
	public static final int AV = 1;
	
	/**
	 * Delete a variable
	 */
	public static final int DV = 2;
	
	/**
	 * Changed variable
	 */
	public static final int CV = 3;
	
	/**
	 * Added Class
	 */
	public static final int AC = 7;
	
	/**
	 * Deleted Class
	 */
	public static final int DC = 8;
	
	/**
	 * Changed Class
	 */
	public static final int CC = 9;
	
	/**
	 * Added Expression
	 */
	public static final int AE = 10;
	
	/**
	 * Deleted Expression
	 */
	public static final int DE = 11;
	
	/**
	 * Changed Expression
	 */
	public static final int CE = 12;
	

	public Object ref;

	public int type;
	
	public ReferenceNode(ClientMember ref, int type) {
		this.ref = ref;
		this.type = type;
	}

//	public ReferenceNode(ClientClass ref, int type) {
//		this.ref = ref;
//		this.type = type;
//	}
//	
//	public ReferenceNode(ClientVar ref, int type) {
//		this.ref = ref;
//		this.type = type;
//	}
//	
//	public ReferenceNode(ClientFunc ref, int type) {
//		this.ref = ref;
//		this.type = type;
//	}
//	
//	public ReferenceNode(ClientExp ref, int type) {
//		this.ref = ref;
//		this.type = type;
//	}
	
	public static String getTypeString(int type) {
		String s;
		switch (type) {
		case 1:
			s = "AV";
			break;
		case 2:
			s = "DV";
			break;
		case 3:
			s = "CV";
			break;
		case 4:
			s = "AF";
			break;
		case 5:
			s = "DF";
			break;
		case 6:
			s = "CF";
			break;
		case 7:
			s = "AC";
			break;
		case 8:
			s = "DC";
			break;
		case 9:
			s = "CC";
			break;
		case 10:
			s = "AE";
			break;
		case 11:
			s = "DE";
			break;
		case 12:
			s = "CE";
			break;
		default:
			s = "UNKNOWN";
			break;
		}
		return s;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReferenceNode other = (ReferenceNode) obj;
		if (ref == null) {
			if (other.ref != null)
				return false;
		} else if (!ref.equals(other.ref))
			return false;
		
		if (type != other.type)
			return false;
		
		return true;
	}




	@Override
	public String toString() {
		
		return ((ClientMember) ref).getSignature();
		
//		if (ref instanceof ClientClass) {
//			return ((ClientClass) ref).getSignature();
//		}
//		
//		else if (ref instanceof ClientVar) {
//			return ((ClientVar) ref).getSignature();
//		}
//		
//		else if (ref instanceof ClientExp) {
//			return ((ClientExp) ref).getSignature();
//		}
//		
//		else 
//			return ((ClientFunc) ref).getSignature();
	}
}
