import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;


public class MyClient implements ActionListener,KeyListener
{
	
	JLabel jl;
	Socket s;
	DataInputStream din;
	DataOutputStream dout;
	JFrame f;
	JPanel p,p1,p2,p3,p4,p5;
	JTextArea ta,ta1;
	JScrollPane sp;
	JTextField tf;
	String mes;
	JButton send,leave,clear,userlist;
	//BufferedReader br;
	String name;
	
	
	public MyClient()
	{
		name = JOptionPane.showInputDialog("Enter ur name");
		ta1=new JTextArea();
		ta1.setEditable(false);
		ta1.setFont(new Font("Arial",Font.PLAIN,18));
		ta=new JTextArea("",15,28);
		ta.setEditable(false);
		DefaultCaret caret=(DefaultCaret)ta.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		ta.setFont(new Font("Arial",Font.PLAIN,18));
		sp=new JScrollPane(ta);
		sp.setRowHeaderView(ta1);
		
		
		jl=new JLabel("Active Users",new ImageIcon("image/online.jpeg"),JLabel.LEFT);
		p2=new JPanel();
		p3=new JPanel();
		p4=new JPanel();
		//p5=new JPanel();
		
		leave=new JButton("Leave");
		leave.setPreferredSize(new Dimension(120,25));
		leave.addActionListener(this);
		
		clear=new JButton("Clear");
		clear.setToolTipText("Clear the textarea");
		clear.setPreferredSize(new Dimension(120,25));
		clear.addActionListener(this);
		
		userlist=new JButton("UsersList");
		userlist.setToolTipText("Ask Server for Active Users List");
		userlist.setPreferredSize(new Dimension(120,25));
		userlist.addActionListener(this);
		
		
		tf=new JTextField(25);
		tf.setToolTipText("Type ur mesg here");
		tf.addKeyListener(this);
		tf.setFont(new Font("Arial",Font.PLAIN,16));
		
		send=new JButton("Send");
		send.setToolTipText("Press here to send mesg");
		send.setPreferredSize(new Dimension(150,25));
		p1=new JPanel();
		p=new JPanel();
		p.setLayout(new BorderLayout());
		p.add(sp,BorderLayout.CENTER);
		p2.add(p);
		p2.add(p4);
		p4.setLayout(new BorderLayout(10,10));
		p3.setLayout(new GridLayout(3,1,50,50));
		//p3.setLayout(null);
		p2.add(p3);
		p3.add(leave);
		p3.add(clear);
		p3.add(userlist);
		p1.add(tf);
		p1.add(send);
		//p1.setLayout(new GridLayout(1,2,5,5));
		p.add(p1,BorderLayout.SOUTH);
		
		send.addActionListener(this);
		f=new JFrame("Welcome! "+name);
		f.add(p2);
		
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(0);
		f.setResizable(false);
		Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
		int x=(int)(dim.getWidth())/2-(int)(f.getWidth())/2;
		int y=(int)(dim.getHeight())/2-(int)(f.getHeight())/2;
		f.setLocation(x,y);
		
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent we)
		{ 
        String ObjButtons[] = {"Yes","No"};
        int PromptResult = JOptionPane.showOptionDialog(null,"u want to leave chat?",name,JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
        if(PromptResult==JOptionPane.YES_OPTION)
        {
			try{dout.writeUTF(name+" leaves the chat");
				ta1.append(new Date().toString().substring(11,19)+"  \n");
				dout.flush();
				System.exit(0);
				}catch(Exception e){}
        }}});
		
		try{
			s=new Socket("localhost",10);
			din=new DataInputStream(s.getInputStream());
			dout=new DataOutputStream(s.getOutputStream());
			String aa=name+" join the chat";
			dout.writeUTF(aa);
			dout.flush();
			clientChat();
			
		}
		catch(Exception e){System.out.println(e);}
	
		
		
	}
	
	public void keyPressed(KeyEvent ee)
	{
		if(ee.getKeyCode()==KeyEvent.VK_ENTER)
		{
			mes=tf.getText();
		}
	}
	
	public void keyReleased(KeyEvent ee){}
	public void keyTyped(KeyEvent ee){}
	
	public void clientChat() throws Exception
	{
		My m=new My(din,ta,name,ta1,this);
		Thread t1=new Thread(m);
		t1.start();
		
		String s1,s2;
		do{
			System.out.print("");
			
			if(mes!=null)
			{s1=mes;
			//System.out.println(s1);
			s2=name+": "+s1;
			dout.writeUTF(s2);
			dout.flush();
			mes=null;
			tf.setText(null);
			}
		}while(true);
		
		
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource()==send)
		{
			mes=tf.getText();
		}
		
		if(ae.getSource()==leave)
		{
			int confirm=JOptionPane.showConfirmDialog(null,"u want to leave chat?...",null,JOptionPane.YES_NO_OPTION);
			if(confirm==0)
			{
				try{dout.writeUTF(name+" leaves the chat");
				ta1.append(new Date().toString().substring(11,19)+"  \n");
				dout.flush();
				System.exit(0);
				}catch(Exception e){}
			}
		}
		
		if(ae.getSource()==clear)
		{
			ta.setText(null);
			ta1.setText(null);
		}
		
		if(ae.getSource()==userlist)
		{
			try{
				dout.writeUTF("@userslist@");
				dout.flush();
			}
			catch(Exception e){}
		}
	}
	
	public static void main(String...ss)
	{
		new MyClient();
	}
}

class My implements Runnable
{
	DataInputStream din;
	JTextArea ta,ta1;
	String name;
	MyClient my;
	My(DataInputStream din,JTextArea ta,String name,JTextArea ta1,MyClient my)
	{
		this.din=din;
		this.ta=ta;
		this.name=name;
		this.ta1=ta1;
		this.my=my;
	}
	
	public void run()
	{
		String s2="";
		int i=0;
		boolean join,leave,block;
		do{
			try{
				
				s2=din.readUTF();
				ta.setForeground(Color.blue);
				ta.append(s2+"\n");
				ta1.append(new Date().toString().substring(11,19)+"  \n");
				String qq=s2.substring(0,s2.length()-22);
				if(qq.equals(my.name))
				{
					JOptionPane.showMessageDialog(null,"u have been blocked by server",null,JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			catch(Exception e){}
		}while(true);
	}
}
