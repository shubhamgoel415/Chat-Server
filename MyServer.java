import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class MyServer implements ActionListener
{
	DefaultListModel listModel=new DefaultListModel();
	String str[]=new String[100];
	Socket soc[]=new Socket[100];
	ArrayList al=new ArrayList();
	ServerSocket ss;
	Socket s;
	JFrame jf;
	JTextArea ta,ta1;
	JScrollPane sp;
	JScrollPane sp1;
	JList list;
	JPanel p;
	JButton block;
	JLabel jl;
	
	public MyServer()
	{
		for(int i=0;i<str.length;i++)
		str[i]="@#$%";
		jf=new JFrame("Server!");
		ta1=new JTextArea();
		ta1.setEditable(false);
		ta1.setFont(new Font("Arial",Font.PLAIN,18));
		ta=new JTextArea("Server Started...\n",15,29);
		ta1.append(new Date().toString().substring(11,19)+"  \n");
		ta.setEditable(false);
		ta.setForeground(Color.blue);
		ta.setFont(new Font("Arial",Font.PLAIN,18));
		sp=new JScrollPane(ta);
		sp.setRowHeaderView(ta1);
		
		DefaultCaret caret=(DefaultCaret)ta.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		list=new JList(listModel);
		sp1=new JScrollPane(list);
		block=new JButton("Block");
		block.addActionListener(this);
		jl=new JLabel("Active Users",new ImageIcon("image/online.jpeg"),JLabel.LEFT);
		sp.setBounds(10,10,500,400);
		jl.setBounds(530,10,120,40);
		sp1.setBounds(520,50,130,300);
		block.setBounds(520,360,130,40);
		
		jf.add(jl);
		jf.add(sp1);
		jf.add(block);
		jf.add(sp);
		jf.setLayout(null);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(0);
		jf.setSize(670,450);
		jf.setResizable(false);
		Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
		int x=(int)(dim.getWidth())/2-(int)(jf.getWidth())/2;
		int y=(int)(dim.getHeight())/2-(int)(jf.getHeight())/2;
		jf.setLocation(x,y);
		
		jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jf.addWindowListener(new WindowAdapter() {
		
		public void windowClosing(WindowEvent we)
		{ 
        String ObjButtons[] = {"Yes","No"};
        int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you want to exit?","Server",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
        if(PromptResult==JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }}});
		
		try
		{
			ss=new ServerSocket(10);
			while(true)
			{
				s=ss.accept();
				al.add(s);
				Runnable r=new MyThread(s,al,ta,ta1,this,listModel);
				Thread t=new Thread(r);
				t.start();
			}
		}
		catch(Exception e){}
		
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource()==block)
		{
			
			String asd=(String)list.getSelectedValue();
			int confirm=JOptionPane.showConfirmDialog(null,"u want to block "+asd,null,JOptionPane.YES_NO_OPTION);
			ta.append(asd+" blocked by the server\n");
				ta1.append(new Date().toString().substring(11,19)+"  \n");
				
			
			if(confirm==0)
			{
			Iterator i=al.iterator();
			while(i.hasNext())
			{
				try{
				Socket sc=(Socket)i.next();
				DataOutputStream dout=new DataOutputStream(sc.getOutputStream());
				dout.writeUTF(asd+" blocked by the server");
				dout.flush();
				
					for(int k=0;k<str.length;k++)
					{
						if(asd.equals(str[k]))
							str[k]="@#$%";
					}
					int x=listModel.indexOf(asd);
					listModel.remove(x);
				
				
				}
				catch(Exception e){}
			}
			}
		}
	}
	
	public static void main(String...s)
	{
		new MyServer();
	}
}

class MyThread implements Runnable{
	Socket s;
	ArrayList al;
	JTextArea ta,ta1;
	MyServer my;
	DefaultListModel listModel;
	MyThread(Socket s,ArrayList al,JTextArea ta,JTextArea ta1,MyServer my,DefaultListModel listModel)
	{
		this.s =s;
		this.al=al;
		this.ta=ta;
		this.ta1=ta1;
		this.my=my;
		this.listModel=listModel;
	}
	
	public void run()
	{
		String s1,list=" ";
		int i=0;
		boolean join,leave;
		try{
			DataInputStream din=new DataInputStream(s.getInputStream());
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());
			do{
				s1=din.readUTF();
				join=s1.endsWith("join the chat");
				leave=s1.endsWith("leaves the chat");
				if(join==true)
				{
					i++;
					my.str[i]=s1.substring(0,s1.length()-14);
					my.soc[i]=s;
					//System.out.println(my.str[i]+" "+my.soc[i]);
					listModel.addElement(my.str[i]);
					
				}
				if(leave==true)
				{
					String leav=s1.substring(0,s1.length()-16);
					for(int k=0;k<my.str.length;k++)
					{
						if(leav.equals(my.str[k]))
							my.str[k]="@#$%";
					}
					int x=listModel.indexOf(leav);
					listModel.remove(x);
				}
				if(s1.equals("@userslist@"))
				{
					int x;
					list="Active Users: ";
					try{
						x=listModel.getSize();
					for(int k=0;k<x;k++)
					{
						list=list+(k+1)+") "+listModel.elementAt(k)+"  ";
						
						
					}
					dout.writeUTF(list);
					dout.flush();
					}
					catch(Exception e){e.printStackTrace();}
				}
				else{
				ta.append(s1+"\n");
				ta1.append(new Date().toString().substring(11,19)+"  \n");
					toAll(s1);
				}
			}while(true);
		}
		catch(Exception e){}
	}
	
	public void toAll(String s1)
	{
		Iterator i=al.iterator();
		while(i.hasNext())
		{
			try{
				Socket sc=(Socket)i.next();
				DataOutputStream dout=new DataOutputStream(sc.getOutputStream());
				dout.writeUTF(s1);
				dout.flush();
			}
			catch(Exception e){}
		}
	}
}
