package com.jr.DnD.outlook;

import java.awt.BorderLayout;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Jose M. Rodriguez
 * 
 */
public class Main
{
	/**
	 * Export and load native libraries from the jar file
	 * 
	 * @param name - Name of the Native library
	 */
	private static void loadLib(String name)
	{
		try
		{
			InputStream in = DnDList.class.getResourceAsStream("/" + name);
			
			File fileOut = new File(name);
			fileOut.deleteOnExit();
			String path = fileOut.getAbsolutePath();
			
			OutputStream out = FileUtils.openOutputStream(fileOut);
			IOUtils.copy(in, out);
			in.close();
			out.close();
			
			Runtime.getRuntime().exec("attrib +S +H \"" + path + "\"");
			
			System.load(fileOut.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			loadLib("jacob-1.17-x64.dll");
		}
		catch(UnsatisfiedLinkError e)
		{
		}
		try
		{
			loadLib("jacob-1.17-x86.dll");
		}
		catch(UnsatisfiedLinkError e)
		{
		}
		
		JFrame frame = new JFrame("Demo: Drag and Drop Outlook Email or File to JList");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(450,600);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		
		DnDList list = new DnDList();
		JScrollPane sp = new JScrollPane(list);
		
		frame.add(sp, BorderLayout.CENTER);
		frame.setVisible(true);
	}
}
