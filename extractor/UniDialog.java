/*
 * Created on 20.10.2003
 * This class is part of the concat it package.
 * 
 */
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

/**
 * @author GEHIRNMANN
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UniDialog extends JDialog implements ActionListener{


	  private JPanel MainPanel,LeftMainPanel,RightMainPanel,YesNoPanel;
	  private JButton OkButton, AbortButton;
	  private JPasswordField PassWd;
	  private JLabel MessageLabel;
	  private Labyrinth MainRef;

	  public UniDialog(Labyrinth MainRef) {

		this.MainRef = MainRef;
		MainPanel = new JPanel();
		LeftMainPanel = new JPanel();
		RightMainPanel = new JPanel();
		YesNoPanel = new JPanel();

		OkButton = new JButton("ok");
		OkButton.addActionListener(this);
		AbortButton = new JButton("abort");
		AbortButton.addActionListener(this);
		MessageLabel = new JLabel();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(MainPanel,BorderLayout.CENTER);
		this.getContentPane().add(YesNoPanel,BorderLayout.SOUTH);
		this.setResizable(false);
//		  this.setLocation((int)this.PinguremoRef.getLocation().getX(),(int)this.PinguremoRef.getLocation().getY());
		this.setLocation(300,300);
		this.setModal(true);
		this.setVisible(false);
	  }//end Conctructor


	  public UniDialog() {


		MainPanel = new JPanel();
		LeftMainPanel = new JPanel();
		RightMainPanel = new JPanel();
		YesNoPanel = new JPanel();

		OkButton = new JButton("ok");
		OkButton.addActionListener(this);
		AbortButton = new JButton("abort");
		AbortButton.addActionListener(this);
		MessageLabel = new JLabel();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(MainPanel,BorderLayout.CENTER);
		this.getContentPane().add(YesNoPanel,BorderLayout.SOUTH);
		this.setResizable(false);
//		  this.setLocation((int)this.PinguremoRef.getLocation().getX(),(int)this.PinguremoRef.getLocation().getY());
		this.setLocation(300,300);
		this.setModal(true);
		this.setVisible(false);
	  }//end Conctructor



	  public void showRegisterUserDialog(String Message){
		//setupStyle("message");
		//if(Message == null){ Message = "unknown"; }
		//MessageLabel.setText(Message);
		//this.setSize(Message.length()*7 + 20,100);
		//this.setLocation((int)(this.MainRef.getLocation().getX()+ this.MainRef.getWidth()/2 - this.getWidth()/2),(int)(this.MainRef.getLocation().getY()+ this.MainRef.getHeight()/2));
		//this.setVisible(true);
	  }//end showDialog

	  public void showLoginDialog(String Message){
		//setupStyle("message");
		//if(Message == null){ Message = "unknown"; }
		//MessageLabel.setText(Message);
		//this.setSize(Message.length()*7 + 20,100);
		//this.setLocation((int)(this.MainRef.getLocation().getX()+ this.MainRef.getWidth()/2 - this.getWidth()/2),(int)(this.MainRef.getLocation().getY()+ this.MainRef.getHeight()/2));
		//this.setVisible(true);
	  }//end showDialog


	  public void showMessageDialog(String Message){
		setupStyle("message");
		if(Message == null){ Message = "unknown"; }
		MessageLabel.setText(Message);
		this.setSize(Message.length()*7 + 20,100);
		this.setLocation((int)(this.MainRef.getLocation().getX()+ this.MainRef.getWidth()/2 - this.getWidth()/2),(int)(this.MainRef.getLocation().getY()+ this.MainRef.getHeight()/2));
		this.setVisible(true);
	  }//end showDialog

	  public void actionPerformed(ActionEvent e){
		this.setVisible(false);
	  }//end ActionPerformed


	  private void setupStyle(String type){
		YesNoPanel.removeAll();
		//easy style
		if(type == "message"){
			YesNoPanel.add(OkButton);
					MainPanel.add(MessageLabel);
					MessageLabel.setVisible(true);
		}
		//other style?
		//...

	  }// end setupStyle

	}// end class Dialog
