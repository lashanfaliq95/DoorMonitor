import java.util.*;

public class CamServer {
	static final int port=1300;
	public static void main(String [] args)throws IOException{
	ServerSocket server=new ServerSocket(port);
	while(true){
	Socket s=server.accept();
	/*DataInputStream dIs= new DataInputStream(s.getInputStream());
	dIS.close();*/
	FileInputStream img=new FIleInputStream("C:\\new\\out0.bmp");
	byte [] buffer=new byte[img.available()];
	img.read(buffer);
	ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
	oos.writeObject(buffer);
	oos.close();

	s.close(); }

    }
}