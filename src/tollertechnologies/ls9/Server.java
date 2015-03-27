package tollertechnologies.ls9;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class Server {
	static DatagramSocket socket;
	static LS9 ls9;
	static byte[] buffer;
	static DatagramPacket packet;
	public static void main(String[] args) {
		System.out.println("Starting UDP reciever on port 3724...");
		try {
			socket = new DatagramSocket(3724);
		} catch (SocketException e) {
			System.out.println("Error creating socket. Please contact Charles Toller for assistance.");
			System.exit(0);
		}
		System.out.println("MIDI Devices:");
		Info[] info = MidiSystem.getMidiDeviceInfo();
		for(int i = 0;i<info.length;i++) {
			System.out.println(i+info[i].getName()+": "+info[i].getDescription());
		}
		String dev = System.console().readLine("Choose a device:");
		try {
			ls9 = new LS9(info[Integer.parseInt(dev)]);
		} catch (NumberFormatException e) {
			System.out.println("Type a number next time.");
			System.exit(0);
		} catch (MidiUnavailableException e) {
			System.out.println("Somehow, you've managed to screw up the MIDI connection. Well done.");
			System.exit(0);
		}
		try {
			ls9.open();
		} catch (NoLS9Exception e1) {
			System.out.println("Wrong device.");
			System.exit(0);
		}
		System.out.println("Ready for connections.");
		while(true) {
			buffer = new byte[65508];
			packet = new DatagramPacket(buffer,buffer.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("Something's gone wrong. Restart the application.");
			}
			Thread thread = new Thread(new Main(ls9,packet));
			thread.start();
		}
	}
}
