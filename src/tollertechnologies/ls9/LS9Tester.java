package tollertechnologies.ls9;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
public class LS9Tester extends LS9 {
  boolean open = false;
  boolean closed = false;
  byte[] lastMessage = new byte[];
	LS9Tester() throws MidiUnavailableException {
		
	}
	public void open() throws NoLS9Exception {
	  open = true;
	}
	public void close() {
		open = false;
		closed = false; //This tests that the application opened AND closed, rather than just nothing happening.
	}
	public void send(byte[] message) throws InvalidMidiDataException, NoLS9Exception, MidiUnavailableException {
		if(!open) {
		  throw new NoLS9Exception();
		  return;
		}
		lastMessage = message;
	}
	public byte[] lm() {
	  return lastMessage;
	}
	public boolean checkOpened() {
	  return open;
	}
	public boolean checkClosed() {
	  return closed;
	}
}
