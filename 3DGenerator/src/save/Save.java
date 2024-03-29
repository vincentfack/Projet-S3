package save;

import graphic.Frame;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mvc.Model;
import mvc.ObjectController;

/**
 * cette classe contient les diff�rents panel permettant la saisie d'information pour les
 * enregistrements d'un objet 3d dans la base de donn�e.
 * @author Alex
 *
 */
public class Save extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * contient le mod�le � enregistrer
	 */
	private Model model;
	/**
	 * panel permettant la saisie d'infos g�n�rales
	 */
	private GeneralInformation info;
	/**
	 * panel permettant la saisie des informations de tri
	 */
	private SortInformation sorted;
	/**
	 * permet de valider la sauvegarde
	 */
	private JButton valider;
	/**
	 * permet d'annuler la sauvegarde
	 */
	private JButton annuler;
	/**
	 * Permet de controller certaines informations du model
	 */
	private ObjectController controller;

	/**
	 * Constructeur de la boite de dialogue
	 * @param parent
	 * @param model
	 * @param controller
	 * @param itemModifier 
	 * @param itemSauver 
	 */
	public Save(Frame parent,Model model,ObjectController controller, JMenuItem itemSauver, JMenuItem itemModifier){
		super(parent,"Enregistrer",true);
		this.model = model;
		this.controller = controller;
		this.initComponents(itemSauver,itemModifier);
		this.pack();
		this.setResizable(false);
		int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2-this.getWidth()/2);
		int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-this.getHeight()/2);
		this.setLocation(x, y);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * permet de placer les composants
	 * @param itemModifier 
	 * @param itemSauver 
	 */
	private void initComponents(final JMenuItem itemSauver,final JMenuItem itemModifier){
		this.info = new GeneralInformation(this.model.getObject().getFichier().getAbsolutePath());
		this.sorted = new SortInformation();

		this.valider = new JButton("Valider");
		final JDialog parent = this;
		this.valider.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0){
				if(info.getInfo() != null){
					int confirm = JOptionPane.showConfirmDialog(parent, "Confirmer l'ajout dans la base","Confirmation",
							JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
					if(confirm == JOptionPane.OK_OPTION){
						String[] information = info.getInfo();
						String[][] triInformation = sorted.getInfo();
						try{
							Class.forName("org.sqlite.JDBC");
							Connection con = null;
							try{
								con = DriverManager.getConnection("jdbc:sqlite:./config/bibliotheque.db");
								PreparedStatement ps = con.prepareStatement("select name from object where name=?");
								ps.setString(1, information[0]);
								ResultSet rs = ps.executeQuery();
								if(rs.next()){
									JOptionPane.showMessageDialog(parent, "Erreur le nom choisi existe d�j�", "Erreur", JOptionPane.ERROR_MESSAGE);
								} else {
									ps = con.prepareStatement("insert into object values(?,?,?,?,?,?,?,?,?,?)");
									int j = 0;
									for(; j < information.length; j++)
										ps.setString(j+1,information[j]);
									for(int i = 1; i < triInformation.length; i++){
										ps.setString(++j, triInformation[i][0]);
									}
									ps.setString(++j, model.getNBPoint()+"");
									ps.setString(++j, model.getNBSeg()+"");
									ps.setString(++j, model.getNBFace()+"");
									ps.executeUpdate();
									controller.setTag(triInformation[0]);
									if(triInformation[0] != null){
										ps = con.prepareStatement("insert into tag values(?,?)");
										for(String tmp : model.getTag()){
											ps.setString(1, tmp);
											ps.setString(2, information[0]);
											ps.executeUpdate();
										}
									}
									controller.setName(information[0]);
									controller.setAuteur(information[3]);
									controller.setUtilisation(triInformation[1][0]);
									controller.setForme(triInformation[2][0]);
									controller.setDescription(triInformation[3][0]);
									info.copyFile();
									itemSauver.setEnabled(false);
									itemModifier.setEnabled(true);
									dispose();
								}
							}catch(Exception e){e.printStackTrace();}
							con.close();
						}catch(Exception e){e.printStackTrace();}
					}
				}
				else{
					String msg = "Impossible d'effectuer l'enregistrement des informations sont manqantes";
					JOptionPane.showMessageDialog(parent,msg,"Erreur",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		this.annuler = new JButton("Annuler");
		this.annuler.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		GridBagLayout bagLayout = new GridBagLayout();
		this.setLayout(bagLayout);
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTH;
		c.insets = new Insets(2,2,2,2);
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.NORTHEAST;
		c.gridwidth = 1;
		c.gridheight = 1;

		c.gridx = 0;
		c.gridy = 0;
		bagLayout.setConstraints(this.info, c);
		this.getContentPane().add(this.info);

		c.gridx = 1;
		bagLayout.setConstraints(this.sorted, c);
		this.getContentPane().add(this.sorted);

		JPanel tmp = new JPanel();
		tmp.add(this.valider);
		tmp.add(this.annuler);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		bagLayout.setConstraints(tmp, c);
		this.getContentPane().add(tmp);
	}
}
