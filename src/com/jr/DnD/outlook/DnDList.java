package com.jr.DnD.outlook;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.jr.DnD.util.FileHelper;

/**
 * Drag and Drop email items from Outlook into JList or any other file into JList
 * A copy of the email(s) or file(s) will be placed in the provided path or default path of C:/Files
 * 
 * @author Jose M. Rodriguez
 */
public class DnDList extends JList<String>
{
	private static final long serialVersionUID = -2622209540437137353L;
	
	private DefaultListModel<String> model = new DefaultListModel<String>();
	
	private String path = "C:/Files";
	
	/**
	 * 
	 */
	public DnDList()
	{
		if(!new File(path).exists())
		{
			new File(path).mkdirs();
		}
		
		this.setModel(model);
		
		this.refreshList();
		
		DropTarget dropTarget = new DropTarget();
		dropTarget.setComponent(this);
		
		try
		{
			dropTarget.addDropTargetListener(new DropTargetAdapter()
			{
				@Override
				public void dragEnter(DropTargetDragEvent e)
				{
					super.dragEnter(e);
				}
				
				@Override
				public void dragOver(DropTargetDragEvent e)
				{
					e.acceptDrag(DnDConstants.ACTION_COPY);
					
					super.dragOver(e);
				};
				
				public void drop(DropTargetDropEvent e)
				{
					try
					{
						Transferable t = e.getTransferable();
						if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
						{
							DataFlavor[] dataFlavors = t.getTransferDataFlavors();
							e.acceptDrop(DnDConstants.ACTION_COPY);
							for(int i = 0; i < dataFlavors.length; i++)
							{
								if(dataFlavors[i].getRepresentationClass().equals(Class.forName("java.util.List")))
								{
									try
									{
										List<?> list = (List<?>) t.getTransferData(dataFlavors[i]);
										for(Object o : list)
										{
											File f = new File("" + o);
											
											String fName = path + (path.endsWith("/") ? "" : "/") + f.getName();
											
											try
											{
												FileHelper.synchronize(f, new File(fName), true);
											}
											catch(Exception ex)
											{
												ex.printStackTrace();
											}
										}
									}
									catch(IOException ex)
									{
										String line1[] = ((String) t.getTransferData(DataFlavor.stringFlavor)).split("\n")[0].split("\t");
										
										for(int c = 0; c < line1.length; c++)
										{
											if("Subject".equalsIgnoreCase(line1[c].trim()))
											{
												//String name = (lines[1].split("\t")[c] + " " + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).replaceAll("[^a-zA-Z0-9.-]", " ");
												
												ActiveXComponent ol = new ActiveXComponent("Outlook.Application");
												
												Dispatch explorer = Dispatch.get(ol, "ActiveExplorer").toDispatch();
												Dispatch selection = Dispatch.get(explorer, "Selection").toDispatch();
												Variant count = Dispatch.get(selection, "Count");
												for(int mailIndex = 1; mailIndex <= count.getInt(); mailIndex++)
												{
													Dispatch mailItem = Dispatch.call(selection, "Item", new Variant(mailIndex)).toDispatch();
													
													Variant subject = Dispatch.get(mailItem, "Subject");
													Variant received = Dispatch.get(mailItem, "ReceivedTime");
													
													String name = (subject + " - " + received).replaceAll("[^a-zA-Z0-9.-]", " ");
													
													Dispatch.call(mailItem, "SaveAs", path + (path.endsWith("/") ? "" : "/") + name + ".msg");
												}
												
												break;
											}
										}
									}
									
									break;
								}
							}
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					finally
					{
						e.dropComplete(true);
					}
					
					refreshList();
				}
			});
		}
		catch(TooManyListenersException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param path in the format of C:/Files
	 */
	public DnDList(String path)
	{
		this.path = path;
	}
	
	/**
	 * Refresh the JList with the list of files in the path
	 */
	public void refreshList()
	{
		model.removeAllElements();
		
		File[] listOfFiles = new File(path).listFiles();
		for(int i = 0; i < listOfFiles.length; i++)
		{
			model.addElement(listOfFiles[i].getName());
		}
	}
}
