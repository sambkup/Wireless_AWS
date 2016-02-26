package mutex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import communication.Message;
import communication.MessagePasser;
import communication.TimeStampedMessage;
import utils.Configuration;
import utils.Group;

enum StateTypes {
	RELEASED,
	WANTED,
	HELD
}

public class CriticalManager {
    static BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

	private boolean voted;
	private StateTypes state;
	private Group myGroup;
	private boolean[] permissionVector;
	MessagePasser messagePasser;
	private ArrayList<Message> msgList;
	String myName;
	
	public CriticalManager(Configuration config, MessagePasser messagePasser){
		voted = false;
		state = StateTypes.RELEASED;
		this.myName = config.getProcessName();
		this.myGroup = config.getGroup(this.myName);
		this.messagePasser = messagePasser;
		this.msgList = new ArrayList<Message>();
		permissionVector = new boolean[myGroup.getNodes().size()];
	}
		
	public void requestCS(Message msg){
		// someone else wants to enter
		if (state == StateTypes.HELD || voted == true){
			msgList.add(msg);
		}else{
			send(msg,"permissionCS");
			voted = true;
		}
	}
	
	public void releaseCS(Message msg){
		//someone is releasing
		if (!msgList.isEmpty()){
			send(msgList.remove(0),"permissionCS");
			voted = true;
		}else{
			voted = false;
		}
	}
	
	public void permissionCS(Message msg){
		int index = myGroup.getProcessGroupIndex(msg.getSource());
		permissionVector[index] = true;
		System.out.println("permission vector: " + Arrays.toString(permissionVector));
		if (permissionVectorIsReady()) {
			doSomething();
		}
	}
	
	private boolean permissionVectorIsReady(){
		for (boolean i : permissionVector){
			if(i == false){
				return false;
			}
		}
		return true;
	}
	
	public void doSomething(){
		// I want to enter Critical Section
		if (!permissionVectorIsReady()){
			this.state = StateTypes.WANTED;
			multicast("requestCS");
		}else{
			state = StateTypes.HELD;
			resetPermissionVector();
			try {
				criticalCode();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// let everyone know that I am done
			exitCS();
		}
	}	
	
	private void exitCS(){
		// multicast to tell everyone that I'm done
		state = StateTypes.RELEASED;
		multicast("releaseCS");
		
		// reset permission Vector
		resetPermissionVector();
	}
	
	private void resetPermissionVector() {
		for (int i = 0; i < permissionVector.length; i++) {
			permissionVector[i] = false;
		}
	}
	
	private void multicast(String msgtype){
		// requestCS, permissionCS, releaseCS
		TimeStampedMessage msg = new TimeStampedMessage("blah",msgtype,"mutex op", null);
		messagePasser.multicast(myGroup.getName(), msg);		
	}
	
	private void send(Message msg, String msgtype){
		TimeStampedMessage newmsg = new TimeStampedMessage(msg.getSource(),msgtype,"mutex op", null);
		messagePasser.send(newmsg);
	}
	
	private void criticalCode() throws IOException{
		System.out.println("*****This is the critical Section*****");
		
		Scanner scanner = new Scanner(new File("mutex"));
		int i = 0;
		while(scanner.hasNextInt()) {
			i = scanner.nextInt();
		}
		scanner.close();
		
		System.out.println("About to increment. Press any key to continue...");
		readUserInput();
		
		FileWriter writer = new FileWriter("mutex");
		writer.write(String.valueOf(++i));
		writer.flush();
		writer.close();
		System.out.println("Incremented to " + String.valueOf(i));
	}
	
	private String readUserInput(){
		String input = null;
		try {
			input = stdIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}
}
