package enumRiddles;

enum TLight {
	   // Each instance provides its implementation to abstract method
	   RED(30,1),
	   AMBER(10,2),
	   GREEN(30,3);
	 
	   
	   private final int seconds;     // Private variable
	   private final int order;
	 
	   TLight(int seconds, int order) {          // Constructor
	      this.seconds = seconds;
	      this.order = order;
	   }
	 
	   int getSeconds() {             // Getter
	      return seconds;
	   }
	   
	   TLight next() {
		   return TLight.values()[(this.order+1)%TLight.values().length];
	   }
	}
	   
	public class TLightTest {
	   public static void main(String[] args) {
	      for (TLight light : TLight.values()) {
	         System.out.printf("%s: %d seconds, next is %s\n", light,
	               light.getSeconds(), light.next());
	      }
	   }
	}