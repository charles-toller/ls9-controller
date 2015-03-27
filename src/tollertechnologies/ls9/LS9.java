package tollertechnologies.ls9;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Receiver;
public class LS9 {
	MidiDevice realLS9;
	LS9(Info midiInfo) throws MidiUnavailableException {
		realLS9 = MidiSystem.getMidiDevice(midiInfo);
	}
	public void open() throws NoLS9Exception {
		try {
			realLS9.open();
		} catch (MidiUnavailableException e) {
			throw new NoLS9Exception();
		}
	}
	public void close() {
		realLS9.close();
	}
	public void send(byte[] message) throws InvalidMidiDataException, NoLS9Exception, MidiUnavailableException {
		SysexMessage myMsg = new SysexMessage(message,message.length);
		if(!realLS9.isOpen()) {
			throw new NoLS9Exception();
		}
		else {
			Receiver r = realLS9.getReceiver();
			r.send(myMsg, -1);
			r.close();
		}
	}
}
