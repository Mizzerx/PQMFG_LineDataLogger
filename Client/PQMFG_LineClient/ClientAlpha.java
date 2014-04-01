import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jws.soap.SOAPBinding.Use;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.xml.ws.Response;

public class ClientAlpha implements ActionListener
{
	//Boolean I use to control some debug print statements
	private boolean Testing = true;
	private boolean IamConnected = false;

	//Some Initial Class Variables
	private Socket ServerSocket = null; //Socket to the server
	private Socket MachineSocket = null; //Socket to a specific machine

	//some new integer
	private InetAddress ServerIpAddress = null;
	private int ServerPortNum = 5006;

	//Inet adresses and port numbers 
	private InetAddress machineIpAdress = null;
	private int machinePortNum = 5005;

	// Window height and width
	private int windowWidth, windowHeight;

	//GUI Elements
	private JFrame guiFrame;
	private JTextArea MainView;
	private JButton[] MachineButtonList;
	private JButton[] ComandButtonList;

	private JLabel LineHeaderLabel = new JLabel("Viewing Line#: ");
	private JLabel LineWOLabel= new JLabel("Running WO#: ");
	private JLabel LineTimeLabel= new JLabel("WO RunTime: ");

	private JLabel LineHeaderField = new JLabel("<Non Selected>");
	private JLabel LineWOField= new JLabel("<N/A>");
	private JLabel LineTimeField= new JLabel("<N/A>");

	//holds all button info
	private static String[][] buttonDict = null;
	private String[] CurrentElement = null;
	private String[] ComandList = {"#COMPLETE","#CHANGE", "#SETPPB","#ADJUST", "#UP","#DOWN","#ADD","#REMOVE","#MSG"};

	//The Main METHOD!!! OMG!!!
	public static void main(String[] args) 
	{
		new ClientAlpha(600,480);
	}

	//Default Constructor
	public ClientAlpha(int windowWidth,int windowHeight)
	{

		/**
		 * Ask User To point At Server
		 * */

		//Create a Multi-line input box
		JOptionPaneMultiInput UserPrompt = new JOptionPaneMultiInput();
		NETAddress myAdress = UserPrompt.ask();// *NOTE: NET Adrees simple class i pout together for passing back and forth net addresses

		if (myAdress != null && myAdress.IP != null && myAdress.Port!= -1)
		{
			//Asighned returned shit to class variabls
			this.windowWidth = windowWidth;
			this.windowHeight = windowHeight;

			int globalFont = 15;
			LineHeaderLabel.setFont(new Font("Verdana",1,globalFont));
			LineHeaderField.setFont(new Font("Verdana",1,globalFont));
			LineHeaderField.setForeground(Color.red);

			LineWOLabel.setFont(new Font("Verdana",1,globalFont));
			LineWOField.setFont(new Font("Verdana",1,globalFont));
			LineWOField.setForeground(Color.red);

			LineTimeLabel.setFont(new Font("Verdana",1,globalFont));
			LineTimeField.setFont(new Font("Verdana",1,globalFont));
			LineTimeField.setForeground(Color.red);

			ServerIpAddress = myAdress.IP;
			ServerPortNum = myAdress.Port;

			/**
			 * Heres where we try and open up a connection to the server
			 * */
			try 
			{
				ServerSocket = new Socket(ServerIpAddress, ServerPortNum);
				String[] DoIExist = sendCommand("#GET_ACTIVE", ServerSocket);
				ServerSocket.close();

				if (DoIExist[0].equals("#NONE"))
				{
					JOptionPane.showMessageDialog(null, "Connections Sucsessful: But there are no lines active");
				}
				else
				{
					//Creates the panel holder(JFrame)
					guiFrame = new JFrame();

					//make sure the program exits when the frame closes
					guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					guiFrame.setTitle("PQMFG LINE CONTROLLER GUI");
					guiFrame.setMinimumSize(new Dimension(windowWidth,windowHeight));

					buttonDict = new String[DoIExist.length][3];

					int cnt= 0;
					for (String prs : DoIExist) 
					{

						String[] splited = prs.split("\\s+");

						if (splited.length == 4)
						{
							buttonDict[cnt][0] = splited[1];
							buttonDict[cnt][1] = splited[2].split("'")[1];
							buttonDict[cnt][2] = splited[3].substring(0, splited[3].length() - 1);

						}

						cnt+=1;

					}

					//create pannels and add them to frame
					guiFrame.add(createButtons(),BorderLayout.WEST);
					guiFrame.add(createCenterField(),BorderLayout.CENTER);					
					guiFrame.add(createComandButtons(),BorderLayout.EAST);

					//This will center the JFrame in the middle of the screen
					guiFrame.setLocationRelativeTo(null);

					//make sure the JFrame is visible
					guiFrame.setVisible(true);

				}

			} 
			catch (IOException e) 
			{
				JOptionPane.showMessageDialog(null, "Could Not Open connection to Server At: "+ServerIpAddress+" "+Integer.toString(ServerPortNum));
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Connections Aborted");
		}
	}

	/*
	 * Used to Left side button pannel
	 * */
	protected JComponent createButtons() 
	{
		//Creates pannel that will be returned to constructor for later use
		JPanel panel = new JPanel(new GridLayout(0,1, 10 ,10));

		//Create Buttong
		MachineButtonList = new JButton[buttonDict.length];

		panel.setPreferredSize(new Dimension(windowWidth/4-20,windowHeight));
		panel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Active Lines"),
								BorderFactory.createEmptyBorder(5,5,5,5)), 
								panel.getBorder()));

		int cnt = 0;
		for (String[] MachineLines : buttonDict) 
		{

			MachineButtonList[cnt] = new JButton("Line: "+Integer.parseInt(MachineLines[0]));
			MachineButtonList[cnt].addActionListener(this);
			panel.add(MachineButtonList[cnt]);

			cnt+=1;
		}

		return panel; 

	}

	/*
	 * Used to Right side button pannel
	 * */
	protected JComponent createComandButtons() 
	{
		//Creates pannel that will be returned to constructor for later use
		JPanel panel = new JPanel(new GridLayout(0,1, 10 ,10));

		String[] ComandHeaders = {"Complete WO","Change WO", "Set PPB","Adjust Count", "Bring Machine Up", "Bring Machine Down","Add Employee", "Remove Employee","Send Message"};


		//Create Buttong
		ComandButtonList = new JButton[ComandList.length];

		panel.setPreferredSize(new Dimension(windowWidth/4+20,windowHeight));
		panel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Commands"),
								BorderFactory.createEmptyBorder(5,5,5,5)), 
								panel.getBorder()));

		int cnt = 0;
		for (String Comand : ComandList) 
		{
			ComandButtonList[cnt] = new JButton(ComandHeaders[cnt]);
			ComandButtonList[cnt].setFont(new Font("Arial", Font.PLAIN, 11));
			ComandButtonList[cnt].addActionListener(this);
			panel.add(ComandButtonList[cnt]);

			cnt+=1;
		}

		return panel; 

	}

	/*
	 * Used to create Center Text Area and Banner
	 * */
	protected JComponent createCenterField() 
	{

		int spacerGap = 20;

		//creates & formats panel that will be passed back later 
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension((windowWidth/2)-spacerGap, windowHeight));

		JPanel tempPanel = new JPanel();
		MainView = new JTextArea("-----NO CONNECTION ESTABLISHED-----\n");

		//Configure the first temp pannel
		tempPanel.setLayout(new GridBagLayout());
		tempPanel.setPreferredSize(new Dimension((windowWidth/2)-spacerGap,windowHeight/5));

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		tempPanel.add(LineHeaderLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		tempPanel.add(LineWOLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 2;
		tempPanel.add(LineTimeLabel, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		tempPanel.add(LineHeaderField, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 1;
		tempPanel.add(LineWOField, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		tempPanel.add(LineTimeField, c);

		tempPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Line Info"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
								tempPanel.getBorder()));

		panel.add(tempPanel);

		//creates & Formats object that holds and navigates text area
		JScrollPane areaScrollPane = new JScrollPane(MainView);
		areaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension((windowWidth/2)-spacerGap, windowHeight-(windowHeight/5)-45));

		//More Formatting
		areaScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Line Status"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
								areaScrollPane.getBorder()));


		//Add the new text area to the pannel and return it...
		panel.add(areaScrollPane);
		return panel;
	}

	/*
	 * SIMPLE SEND AND RECIEVE METHOD FOR TCP COMS
	 * */
	public String[] sendCommand(String command, Socket TCPSocket) throws IOException
	{

		//The returned array
		String[] returnString = null;

		//The Arraylist and string that pull in innital data
		ArrayList<String> MessageList = new ArrayList<String>();
		String sentence =null;

		//In and out streams
		DataOutputStream outToServer  = new DataOutputStream(TCPSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));

		//send the comand out
		outToServer.writeBytes(command);
		if (Testing) {System.out.println("Wrote comand: "+command);}

		//used for possible retry
		boolean attempted = true;
		//While data still comes in
		while((sentence= inFromServer.readLine())!= null)
		{

			if (command.equals("#STATUS_C") && sentence.split(",")[0].equals("INVALID COMMAND"))
			{
				if (attempted)
				{

					if (Testing) {System.out.println("attempting response");}

					//makes the program wait 0.1 seconds
					try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

					//then resend the command
					outToServer.writeBytes(command);
					attempted = false;

				}
				else
				{
					MessageList.add(sentence);
					break;
				}
			}

			//add it to the arraylist
			MessageList.add(sentence);
			System.out.println("'"+sentence+"'");
		}

		outToServer.close();
		inFromServer.close();

		System.out.println("Finish capturig");

		//Make sure something actually got sent
		if(!MessageList.isEmpty())
		{
			//Resize the returned the STRING[] 
			returnString = new String[MessageList.size()];

			//Counter Variable
			int count = 0;

			//convert from arraylist... wich are messy... to strings[]... 
			while(!MessageList.isEmpty())
			{
				returnString[count] = MessageList.remove(0);
				//System.out.println(returnString[count]);
				count++;
			}

		}

		//always return something
		return returnString;
	}


	public void refreshView()
	{
		try 
		{
			//debug
			if (Testing) {System.out.println(machineIpAdress.toString()+ " "+ String.valueOf(machinePortNum));}

			MachineSocket = new Socket(machineIpAdress, machinePortNum);

			//debug
			if (Testing) {System.out.println("Sending Comand...");}

			String[] DoIExist = sendCommand("#STATUS_C", MachineSocket);
			MachineSocket.close();

			if (Testing) {System.out.println("Sent Comand and Recieved Response");}


			if (DoIExist != null && !DoIExist[0].equals("No WO Log Avalible, Machine is Idle"))
			{

				String StartTime = null;
				String minew = "";
				int cnt = 0;
				for (String line : DoIExist) 
				{
					if (cnt == 0)
					{
						LineWOField.setText(line);
						LineWOField.setForeground(Color.GREEN);
						LineWOField.setBackground(Color.BLACK);
					}
					else if(cnt==1)
					{
						LineHeaderField.setText(line);
						LineHeaderField.setForeground(Color.GREEN);
						LineHeaderField.setBackground(Color.BLACK);
					}
					else if (cnt ==2)
					{
						minew += ("WO Start: "+line+"\n");
					}
					else if (cnt ==3)
					{
						int seconds = Integer.parseInt(line.split("\\s+")[1]);

						int hr = (int)(seconds/3600);
						int rem = (int)(seconds%3600);
						int mn = rem/60;
						int sec = rem%60;

						String splice1 =":";
						String splice2 = ":";

						if (sec < 10)
						{
							splice2 +="0";
						}
						if(mn <10)
						{
							splice1 += "0";
						}

						LineTimeField.setText(hr+splice1+mn+splice2+sec);
						LineTimeField.setForeground(Color.GREEN);
						LineTimeField.setBackground(Color.BLACK);
					}
					else if (cnt ==4)
					{
						minew += ("Total Peaces Counted: "+line+"\n");
					}
					else if (cnt ==6)
					{
						minew += ("Total Boxs Counted: "+line+"\n");
					}
					else if (cnt ==8)
					{
						minew += ("Peaces Not Boxed(Failed): "+line+"\n");
					}
					else if (cnt ==9)
					{
						minew += ("Peaces Per Box: "+line+"\n");
					}
					else if(cnt!=1 && cnt!=5 && cnt !=7)
					{
						minew += (line+"\n");
					}

					cnt += 1;

				}

				MainView.setText(minew);


			}
			else
			{
				LineHeaderField.setText(CurrentElement[0]);
				LineHeaderField.setForeground(Color.GREEN);
				LineHeaderField.setBackground(Color.BLACK);

				MainView.setText("--Connection Established-- \nNo WO Log Avalible, Machine is Idle");

				LineWOField.setText("None");
				LineWOField.setForeground(Color.RED);
				LineWOField.setBackground(Color.BLACK);

				LineTimeField.setText("None");
				LineTimeField.setForeground(Color.RED);
				LineTimeField.setBackground(Color.BLACK);
			}

		} 
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			System.out.println("----Something Went wroong----");
			System.out.println(CurrentElement[0]+" "+CurrentElement[1]+" "+CurrentElement[2]);
			System.out.println("---- ---- ----- ----");

			System.out.println(e1);

		}
	}

	/*
	 * ActionListener For all 
	 * */
	public void actionPerformed(ActionEvent e) 
	{
		for (String elements[]: buttonDict)
		{
			if (elements[0].equals(e.toString().split(",")[1].split(" ")[1]))	
			{

				CurrentElement = elements;
				try 
				{
					machineIpAdress = InetAddress.getByName(elements[1]);
					this.refreshView();

					for (JButton ButtonEl : MachineButtonList) 
					{
						if (ButtonEl != e.getSource())
						{
							ButtonEl.setBackground((new JButton("")).getBackground());
						}
						else
						{
							ButtonEl.setBackground(Color.green);
							IamConnected = true;
						}
					}


				} catch (UnknownHostException e1) 
				{
					JOptionPane.showMessageDialog(null, "Server report is corrupted improperly formated");
				}
				machinePortNum = 5005;

			}
		}
		int cnter =0 ;
		for (JButton element: ComandButtonList)
		{
			if (element == e.getSource() && IamConnected)	
			{

				if(ComandList[cnter].equals("#COMPLETE"))
				{
					//Custom button text
					Object[] options = {"Yes", "No"};
					int n = JOptionPane.showOptionDialog(guiFrame,
							"Are You Sure You would like to complete Current WO?",
							"Confirmation Complete",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null, options, options[1]);

					//n==0 if the yess option is selected
					if(n==0)
					{
						try 
						{
							//open up new machine socket
							MachineSocket = new Socket(machineIpAdress, machinePortNum);

							String[] response = this.sendCommand("#COMPLETE_C", MachineSocket);
							//checks that command was sent... 
							if(response[0].equals("AKN"))
							{
								JOptionPane.showMessageDialog(guiFrame, "WO has been completed");
							}
							else
							{
								JOptionPane.showMessageDialog(guiFrame, "Machine Didn't Actknowldge, comand may not have been excicuted");
								if(Testing){System.out.println("ACKNOWLEDGE: "+response[0]);}

							}

							//closes the socket and refreshes the view...
							MachineSocket.close();
							this.refreshView();

						} 
						catch (IOException e1) 
						{
							JOptionPane.showMessageDialog(guiFrame, "Something Went Wrong\n"+e1);
						}				
					}
				}
				else if(ComandList[cnter].equals("#CHANGE")) 
				{
					String Save = "False";

					if(!LineWOField.getText().equals("None"))
					{
						//Custom button text
						Object[] options = {"Yes", "No"};
						int n = JOptionPane.showOptionDialog(guiFrame,
								"There Is a WO currently running, \nWould you like to Save Current Proggress?",
								"Ask to Comlpete or Toss Complete",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, options, options[1]);

						if (n==0)
						{
							Save = "True";
						}

					}

					//Some init place holders
					String msg = "Please Enter New WO number";
					boolean keeprunning = true;
					int response = 0;

					//make sure we get a valid integer from user as input
					while(keeprunning)
					{
						try 
						{
							response = Integer.parseInt(JOptionPane.showInputDialog (msg));
							keeprunning = false;
						} 
						catch (Exception e2) 
						{
							msg = "TRY AGAIN: Make sure  your Input is a valid Integer";
						}
					}

					//Try opening the Tcp Connection and send the command
					try 
					{
						MachineSocket = new Socket(machineIpAdress, machinePortNum);
						String[] resp = sendCommand("#CHANGE_C "+String.valueOf(response), MachineSocket);
						MachineSocket.close();
					} 
					catch (IOException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					refreshView();
				}
				else if(ComandList[cnter].equals("#UP"))
				{
					String msg = "Please Enter Employee ID number";
					boolean keeprunning = true;
					String response= null;

					while(keeprunning)
					{
						try 
						{
							response = JOptionPane.showInputDialog (msg);
							Integer.parseInt(response);
							keeprunning = false;
						} 
						catch (Exception e2) 
						{
							msg = "TRY AGAIN: Make sure your Input is a valid Integer";
						}
					}

					try 
					{
						MachineSocket = new Socket(machineIpAdress, machinePortNum);
						String[] resp = sendCommand("#UP_C "+response, MachineSocket);
						MachineSocket.close();
					} 
					catch (IOException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}	
					refreshView();



				}
				else if(ComandList[cnter].equals("#DOWN"))
				{
					if(!LineWOField.getText().equals("None"))
					{

						String Command = "#DOWN_C ";

						//current possible reasons: 1)Maitenance, 2) Inventory, 3) Quality_Control 4)Break
						Object[] options = {"Maitenance", "Inventory", "Quality_Control", "Break", "Cancle"};
						int n = JOptionPane.showOptionDialog(guiFrame,
								"Please Select A reason for bringing the machine down",
								"Bring Machine down", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, options, options[4]);

						if (n!=options.length-1)
						{
							Command += options[n]+" ";

							String msg = "Please Enter Employee ID number";
							boolean keeprunning = true;
							String response= null;

							while(keeprunning)
							{
								try 
								{
									response = JOptionPane.showInputDialog (msg);
									Integer.parseInt(response);
									keeprunning = false;
								} 
								catch (Exception e2) 
								{
									msg = "TRY AGAIN: Make sure your Input is a valid Integer";
								}
								
							}
							
							Command += response;
							
							try 
							{
								MachineSocket = new Socket(machineIpAdress, machinePortNum);
								String[] resp = sendCommand(Command, MachineSocket);
								MachineSocket.close();
							} 
							catch (IOException e1) 
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}	
							refreshView();
							
							
						}

					}
				}
				System.out.println(ComandList[cnter]);
			}
			cnter+=1;
		}
	}
}

