package jkl.iec.net.applications;

import jkl.iec.tc.gui.IECTableModel;
import jkl.iec.tc.type.IECList;
import jkl.iec.tc.type.IECMap;
import jkl.iec.tc.type.IECTCItem;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class IECFile {
	
	IECTableModel tf;
	private String PropFile;
	JFileChooser fc = new JFileChooser();
	BufferedWriter bw ;
	
	public IECFile(IECTableModel t) {
		this.tf =t;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties", 
	            "properties", "prop");   
		fc.addChoosableFileFilter(filter);
	}
	

	public void write(String tx) {
		System.out.println(tx);
        try {
            bw.write(tx);
            bw.newLine();
    } catch (Exception e) {
		System.out.println(e);
    }
	}
	
	public boolean saveV2() {
		int returnVal = fc.showSaveDialog(null);
		if (returnVal==JFileChooser.APPROVE_OPTION) {
			PropFile =fc.getSelectedFile().toString();
			if (!PropFile.contains(".properties")) {
				PropFile =fc.getSelectedFile()+".properties";					
			}

			String tx = "";
			System.out.println(PropFile);
			tx="FILE.VERSION=2";
			try {
	            bw = new BufferedWriter(new FileWriter(new File(PropFile), false));
	            bw.write(tx);
	            bw.newLine();
			} catch (Exception e) {
				System.out.println(e);
			}
			tx="ITEMS.COUNT="+tf.ieclist.size();
			write(tx);
			tx="";
			for (String s:IECList.getPropNames()) {
				if (!tx.isEmpty()) {
					tx=tx+";";
				}
				tx=tx+s;
			}
			tx="ITEM.PROPERTIES="+tx;
			write(tx);
			IECTCItem item =null;
			for (int it=0 ;it < tf.ieclist.size();it++ ) {
				item = tf.ieclist.get(it);
				String pre = "ITEM"+String.valueOf(item.ID);
				tx= pre+"="+item.getPString();
				write(tx);
			}
			try {
	            bw.close();
			} catch (Exception e) {
				System.out.println(e);
			}
			return true;
		}
		return false;
	}
	
	public boolean save() {
		int returnVal = fc.showSaveDialog(null);
		if (returnVal==JFileChooser.APPROVE_OPTION) {
			PropFile =fc.getSelectedFile().toString();
			if (!PropFile.contains(".properties")) {
				PropFile =fc.getSelectedFile()+".properties";					
			}
			System.out.println(PropFile);
			Properties p =tf.ieclist.getItemProperties();
			p.setProperty("FILE.VERSION","1");
			try {
				p.store(new FileOutputStream(PropFile),"Item Proberties for IEC TestServer");
				return true;
			} catch (Exception e) {
				System.out.println(e);
				return false;
			}
		}
		return false;
	}

	public void loadV2(Properties p) {
		System.out.println("LOADV2");

	int c= Integer.parseInt(p.getProperty("ITEMS.COUNT","0"));
	List<String> propertyList = Arrays.asList(p.getProperty("ITEM.PROPERTIES").split(";")); 
	System.out.println("ITEM PROBETIES IN FILE "+propertyList);
	for (int it=1;it<=c;it++) {
		String pre = "ITEM"+it;
		String[] ss= p.getProperty(pre).split(";");
		pre=pre+".";
		int sapos =0;
		String k,v;
		for (String s:propertyList) {
				k=pre+s;
				if (s.equals("TYPE")) {
					v=IECMap.getType(Byte.parseByte(ss[sapos])).toString();
					} else {
						v=ss[sapos];
						}
				System.out.println(k+"="+v);
				p.setProperty(k, v);
				sapos++;

				}
		}		
//	p.list(System.out);
	}
	
	public boolean load(String file) {
		Properties p = new Properties();
		double version =1.0;
		
		PropFile =file;
		System.out.println("LOAD "+ file);
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(PropFile));
			p.load(stream);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	if (p.getProperty("FILE.VERSION").equals("2")) {
//			p.getProperty("SERVER.VERSION").equals("2")) {
		loadV2(p);
		version=2.0;
	}
//	p.list(System.out);
	
	Server.ServerDlg.setProperties(p);
	tf.ieclist.setItemProperties(p,version);
	tf.fireTableDataChanged();
	return true;
	}

	public boolean load() {
		if (fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			return load(fc.getSelectedFile().toString());
		}
		return false;
		} 
	}