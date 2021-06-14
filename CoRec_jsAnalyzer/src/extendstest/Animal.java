package extendstest;

public class Animal {
	public int id;
	public int x;
	public int y;
	public Animal(int id, int x) {
		this.id = id;
		this.x = x;
		this.y = id + x;
	}
}
