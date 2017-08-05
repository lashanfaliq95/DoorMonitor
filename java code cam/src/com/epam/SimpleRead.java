
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class SimpleRead {
	private static final  char[]COMMAND = {'*', 'R', 'D', 'Y', '*'};
	private static final int WIDTH = 320; //640;
    private static final int HEIGHT = 240; //480;
    	
    private static CommPortIdentifier portId;
    InputStream inputStream;
    SerialPort serialPort;

    public static void main(String[] args) {
    	 Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
        	portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            	System.out.println("Port name: " + portId.getName());
                if (portId.getName().equals("COM8")) {
                	SimpleRead reader = new SimpleRead();
                }
            }
        }
    }

    public SimpleRead() {
       	int[][]rgb = new int[HEIGHT][WIDTH];
       	int[][]rgb2 = new int[WIDTH][HEIGHT];
    	
    	try {
            serialPort = (SerialPort) portId.open("SimpleReadApp", 1000);
            inputStream = serialPort.getInputStream();

            serialPort.setSerialPortParams(1000000,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        	int counter = 0;

        	while(true) {
        		System.out.println("Looking for image");
        	
        		while(!isImageStart(inputStream, 0)){};
        	
	        	System.out.println("Found image: " + counter);
	        	
	        	for (int y = 0; y < HEIGHT; y++) {
	        		for (int x = 0; x < WIDTH; x++) {
		       			int temp = read(inputStream);
		    			rgb[y][x] = ((temp&0xFF) << 16) | ((temp&0xFF) << 8) | (temp&0xFF);
	        		}
	        	}
	        	
	        	for (int y = 0; y < HEIGHT; y++) {
		        	for (int x = 0; x < WIDTH; x++) {
		        		rgb2[x][y]=rgb[y][x];
		        	}	        		
	        	}
	        	
		        BMP bmp = new BMP();
	      		bmp.saveBMP("c:/out" + (counter++) + ".bmp", rgb2);
	      		
	      		System.out.println("Saved image: " + counter);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private int read(InputStream inputStream) throws IOException {
    	int temp = (char) inputStream.read();
		if (temp == -1) {
			throw new  IllegalStateException("Exit");
		}
		return temp;
    }
    	
    private boolean isImageStart(InputStream inputStream, int index) throws IOException {
    	if (index < COMMAND.length) {
    		if (COMMAND[index] == read(inputStream)) {
    			return isImageStart(inputStream, ++index);
    		} else {
    			return false;
    		}
    	}
    	return true;
    }
}

 class BMP{
    byte [] bytes;
    
    public int[][] readBMP(String fileName) {
        byte[]buf = new byte[54];       
        int[][]rgb = null; 
        
        try {
            FileInputStream fos = new FileInputStream(new File(fileName));
            fos.read(buf, 0, buf.length);
            
            int width = ((buf[21]&0xFF) << 24) + ((buf[20]&0xFF) << 16) + ((buf[19]&0xFF) << 8) + (buf[18]&0xFF);
            int height = ((buf[25]&0xFF) << 24) + ((buf[24]&0xFF) << 16) + ((buf[23]&0xFF) << 8) + (buf[22]&0xFF);
            
            rgb = new int[height][width];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    fos.read(buf, 0, 3);
                    rgb[y][x] = ((buf[2]&0xFF) << 16) + ((buf[1]&0xFF) << 8) + (buf[0]&0xFF);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        
        return rgb;
    }
    
    public void saveBMP(String filename, int [][] rgbValues){
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            
            bytes = new byte[54 + 3*rgbValues.length*rgbValues[0].length];

            saveFileHeader();
            saveInfoHeader(rgbValues.length, rgbValues[0].length);
            saveBitmapData(rgbValues);

            fos.write(bytes);
            
            fos.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void saveFileHeader() {
        bytes[0]='B';
        bytes[1]='M';
        
        bytes[5]=(byte) bytes.length;
        bytes[4]=(byte) (bytes.length>>8);
        bytes[3]=(byte) (bytes.length>>16);
        bytes[2]=(byte) (bytes.length>>24);
        
        //data offset
        bytes[10]=54;
    }
    
    private void saveInfoHeader(int height, int width) {
        bytes[14]=40;

        bytes[18]=(byte) width;
        bytes[19]=(byte) (width>>8);
        bytes[20]=(byte) (width>>16);
        bytes[21]=(byte) (width>>24);

        bytes[22]=(byte) height;
        bytes[23]=(byte) (height>>8);
        bytes[24]=(byte) (height>>16);
        bytes[25]=(byte) (height>>24);

        bytes[26]=1;
        
        bytes[28]=24;
    }
    
    private void saveBitmapData(int[][]rgbValues) {
        for(int i=0;i<rgbValues.length;i++){
            writeLine(i, rgbValues);
        }
    }
    
    private void writeLine(int row, int [][] rgbValues) {
        final int offset=54;
        final int rowLength=rgbValues[row].length;
        for(int i=0;i<rowLength;i++){
            int rgb=rgbValues[row][i];
            int temp=offset + 3*(i+rowLength*row);
            
            bytes[temp + 2]    = (byte) (rgb>>16);
            bytes[temp +1] = (byte) (rgb>>8);
            bytes[temp] = (byte) rgb;
        }
    }
}

