


public class App {
	
	public static void main(String[] args) throws InterruptedException  {
		
		System.out.print("### CANADA KENO LOTTERY GAME ###\n\n");
		CanadaKeno Ckeno = new CanadaKeno();
		Thread CanadaTask = new Thread(Ckeno);
		CanadaTask.start();
		
	}

}
