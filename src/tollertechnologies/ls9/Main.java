package tollertechnologies.ls9;

import java.net.DatagramPacket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class Main implements Runnable {
	LS9 ls9;
	double[] dbl = new double[1024];
	String recieved;
	byte[] header = new byte[] {
			(byte) 0xF0,0x43,0x10,0x3E,0x12,0x01
	};
	byte[] footer = new byte[] {
			(byte) 0xF7
	};
	void initializedB() {
		dbl[0] = -14000;
		double dB = -13800;
		for(int i = 1;i<1024;i++) {
			dbl[i] = dB;
			if(i<15) {
				dB += 300;
			}
			else if(i<33) {
				dB += 100;
			}
			else if(i<223) {
				dB += 20;
			}
			else if(i<423) {
				dB += 10;
			}
			else {
				dB += 5;
			}
		}
	}
	byte[] byteConversion(int startNumber) {
		byte[] newByte = new byte[2];
		newByte[1] = (byte)(startNumber % 128);
		startNumber = (int) Math.floor(startNumber/128);
		newByte[0] = (byte)(startNumber % 128);
		return newByte;
	}
	byte[] byte2Conversion(int startNumber) {
		byte[] newByte = new byte[5];
		newByte[4] = (byte)(startNumber % 128);
		startNumber = (int) Math.floor(startNumber/128);
		newByte[3] = (byte)(startNumber % 127);
		startNumber = (int) Math.floor(startNumber/128);
		newByte[2] = (byte)(startNumber % 128);
		startNumber = (int) Math.floor(startNumber/128);
		newByte[1] = (byte)(startNumber % 128);
		startNumber = (int) Math.floor(startNumber/128);
		newByte[0] = (byte)(startNumber % 128);
		return newByte;
	}
	byte[] byte3Conversion(int startNumber) {
		byte[] newByte = new byte[] {
			15,127,127,127,127
		};
		newByte[4] = (byte)(127-startNumber);
		return newByte;
	}
	Main(LS9 ls92,DatagramPacket packet) {
		ls9 = ls92;
		recieved = new String(packet.getData(),0,packet.getLength());
	}
	public void run() {
		initializedB();
		String s = recieved;
		String[] s2 = s.split(" ");
		if(s2[0].matches("Mix[0-9]+")) {
			if(s2[1].matches("Att")) {
				Matcher number = Pattern.compile("[0-9]+").matcher(s2[0]);
				number.find()
				int mix = Integer.parseInt(number.group());
				mixChangeAtt(mix,Integer.parseInt(s2[2]));
			}
		}
		else if(s2[0].matches("Stereo")) {
			if(s2[1].matches("Off")){
				stereoOnOff(0);
			}
			else {
				stereoOnOff(1);
			}
		}
		else if(s2[0].matches("Input[0-9]+Thru[0-9]+")) {
			if(s2[1].equals("@")) {
				double dB = Double.parseDouble(s2[2]) * 100;
				int i = 0;
				boolean found = false;
				while(!found && i<dbl.length) {
					if(dbl[i] == dB || dbl[i+1] > dB) {
						found = true;
					}
					else {
						i++;
					}
				}
				if(i<dbl.length) {
					Matcher number = Pattern.compile("[0-9]+").matcher(s2[0]);
					number.find();
					int bottom = Integer.parseInt(number.group());
					number.find();
					int top = Integer.parseInt(number.group());
					for(int j = bottom;j<=top;j++) {
						changeLevel(i,j);
					}
				}
				else {
					Matcher number = Pattern.compile("[0-9]+").matcher(s2[0]);
					number.find();
					int bottom = Integer.parseInt(number.group());
					number.find();
					int top = Integer.parseInt(number.group());
					for(int j = bottom;j<=top;j++) {
						changeLevel(i-1,j);
					}
				}
			}
			else if(s2[1].equals("On") || s2[1].equals("Off")) {
				Matcher number = Pattern.compile("[0-9]+").matcher(s2[0]);
				number.find();
				int bottom = Integer.parseInt(number.group());
				number.find();
				int top = Integer.parseInt(number.group());
				double of = s2[1].equals("On") ? 1:0;
				for(int j = bottom;j<=top;j++) {
					onOrOff(j,of);
				}
			}
		}
		else if(s2[0].matches("Input[0-9]+")) {
			if(s2[1].equals("@")) {
				double dB = Double.parseDouble(s2[2]) * 100;
				int i = 0;
				boolean found = false;
				while(!found && i<dbl.length) {
					if(dbl[i] == dB || dbl[i+1] > dB) {
						found = true;
					}
					else {
						i++;
					}
				}
				if(i<dbl.length) {
					changeLevel(i,Double.parseDouble(s2[0].replaceAll("[^0-9]", "")));
				}
				else {
					changeLevel(i-1,Double.parseDouble(s2[0].replaceAll("[^0-9]", "")));
				}
			}
			else if(s2[1].equals("Pan")) {
				String direction = s2[2].replaceAll("[0-9]","");
				double amount;
				if(!direction.equals("C")) {
					amount = Double.parseDouble(s2[2].replaceAll("[^0-9]",""));
				}
				else {
					amount = 0;
				}
				double input = Double.parseDouble(s2[0].replaceAll("[^0-9]",""));
				changePan(direction,amount,input);
			}
			else if(s2[1].equals("On") || s2[1].equals("Off")) {
				double input = Double.parseDouble(s2[0].replaceAll("[^0-9]",""));
				double of = s2[1].equals("On") ? 1:0;
				onOrOff(input,of);
			}
			else if(s2[1].equals("Phase")) {
				double input = Double.parseDouble(s2[0].replaceAll("[^0-9]",""));
				changePhase(s2[2],input);
			}
			else if(s2[1].matches("Mix[0-9]+")) {
				if(s2[2].equals("@")) {
					double dB = Double.parseDouble(s2[3]) * 100;
					int i = 0;
					boolean found = false;
					while(!found && i<dbl.length) {
						if(dbl[i] == dB || dbl[i+1] > dB) {
							found = true;
						}
						else {
							i++;
						}
					}
					if(i<dbl.length) {
						changeMixLevel(i,Double.parseDouble(s2[0].replaceAll("[^0-9]", "")),Integer.parseInt(s2[1].replaceAll("[^0-9]","")));
					}
					else {
						changeMixLevel(i-1,Double.parseDouble(s2[0].replaceAll("[^0-9]", "")),Integer.parseInt(s2[1].replaceAll("[^0-9]","")));
					}
				}
			}
		}
	}
	void mixChangeAtt(int mix,int level) {
		byte[] full;
		byte[] address = new byte[] {
			0x00,0x4F,0x00,0x01
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion(mix));
		full = concatByte(full,byte2Conversion((int) (level * 10)));
		full = concatByte(full,footer);
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void stereoOnOff(double position) {
		byte[] full;
		byte[] address = new byte[] {
			0x00,0x6B,0x00,0x00
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion(0));
		full = concatByte(full,byte2Conversion((int) (position)));
		full = concatByte(full,footer);
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void onOrOff(double input,double position) {
		byte[] full;
		byte[] address = new byte[] {
			0x00,0x31,0x00,0x00
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion((int) (input-1)));
		full = concatByte(full,byte2Conversion((int) position));
		full = concatByte(full,footer);
		System.out.println(bytesToHex(full));
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void changeLevel(double dB,double input) {
		byte[] full;
		byte[] address = new byte[] {
			0x00,0x33,0x00,0x00
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion((int) (input-1)));
		full = concatByte(full,byte2Conversion((int) dB));
		full = concatByte(full,footer);
		System.out.println(bytesToHex(full));
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void changeMixLevel(double dB,double input,int mix) {
		byte[] full;
		byte[] address;
		switch(mix) {
			case 1:
				address = new byte[] {0x00,0x43,0x00,0x05};
				break;
			case 2:
				address = new byte[] {0x00,0x43,0x00,0x08};
				break;
			case 3:
				address = new byte[] {0x00,0x43,0x00,0x0B};
				break;
			case 4:
				address = new byte[] {0x00,0x43,0x00,0x0E};
				break;
			case 5:
				address = new byte[] {0x00,0x43,0x00,0x11};
				break;
			case 6:
				address = new byte[] {0x00,0x43,0x00,0x14};
				break;
			case 7:
				address = new byte[] {0x00,0x43,0x00,0x17};
				break;
			case 8: 
				address = new byte[] {0x00,0x43,0x00,0x1A};
				break;
			default:
				return;
		}
		full = concatByte(header,address);
		full = concatByte(full,byteConversion((int) (input-1)));
		full = concatByte(full,byte2Conversion((int) dB));
		full = concatByte(full,footer);
		System.out.println(bytesToHex(full));
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void changePan(String direction,double amount,double input) {
		byte[] full;
		byte[] address = new byte[] {
				0x00,0x32,0x00,0x01
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion((int) (input-1)));
		if(direction.equals("R")) {
			full = concatByte(full,byte2Conversion((int) amount));
		}
		else if(direction.equals("L")) {
			full = concatByte(full,byte3Conversion((int) ((amount-1))));
		}
		else {
			full = concatByte(full,byte2Conversion(0));
		}
		full = concatByte(full,footer);
		System.out.println(bytesToHex(full));
		try {
			try {
				ls9.send(full);
			} catch (NoLS9Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException e) {
			checkCon();
		}
	}
	void changePhase(String phase,double input) {
		byte[] full;
		byte[] address = new byte[] {
				0x00,0x30,0x00,0x00
		};
		full = concatByte(header,address);
		full = concatByte(full,byteConversion((int) (input-1)));
		full = concatByte(full,byte2Conversion((phase.equalsIgnoreCase("inverted")) ? 1:0));
		full = concatByte(full,footer);
		try {
			ls9.send(full);
		} catch (InvalidMidiDataException e) {
			checkCon();
		} catch (NoLS9Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void checkCon() {
		System.out.println("Check ALL connections(MIDI software,ethernet). Something is wrong.");
	}
	byte[] concatByte(byte[] a, byte[] b) {
	   int aLen = a.length;
	   int bLen = b.length;
	   byte[] c= new byte[aLen+bLen];
	   System.arraycopy(a, 0, c, 0, aLen);
	   System.arraycopy(b, 0, c, aLen, bLen);
	   return c;
	}
	char[] hexArray = "0123456789ABCDEF".toCharArray();
	String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
