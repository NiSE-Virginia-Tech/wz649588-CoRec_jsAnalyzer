package extendstest;

public class Tiger extends Animal{
	public Tiger(int id, int x){
		super(id, x);
	}
	
	public static void main(String[] args) {
		Tiger t = new Tiger(3,4);
		System.out.println(t.y);
	}
}
