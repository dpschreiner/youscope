/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class FileSystem extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 3606837719455361236L;
	
	private final FileSystemModel fileSystemModel = new FileSystemModel();
	private final JTree fileSystemTree = new JTree(fileSystemModel);
	private final JTextField folderField = new JTextField();
	private FolderNode rootNode;
	
	/**
	 * Listeners which should be notified when a measurement should be opened/viewed.
	 */
	private final Vector<ViewMeasurementListener> viewMeasurementListeners = new Vector<ViewMeasurementListener>();
	
	private final YouScopeClient client;
	
	private JButton actualizeButton;
	private JButton folderUpButton;
	
	FileSystem(YouScopeClient client) 
	{
		super(new BorderLayout());
		
		this.client = client;
		
		String measurementFolder = client.getProperties().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER).toString();
		
		// Create rootNode
		try
		{
			rootNode = new FolderNode((new File(measurementFolder)).getCanonicalFile(), null);
		}
		catch(@SuppressWarnings("unused") IOException e2)
		{
			rootNode = new FolderNode((new File(measurementFolder)).getAbsoluteFile(), null);
		}
		
		
		JButton folderChooserButton = new JButton("...");
		folderChooserButton.setMargin(new Insets(1, 1, 1, 1));
		folderChooserButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser fileChooser = new JFileChooser(folderField.getText());
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showDialog(null, "Open");
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					File selectedFile = fileChooser.getSelectedFile();
					folderField.setText(selectedFile.getAbsolutePath());
					rootNode.setFile(selectedFile);
					fileSystemModel.actualizeTree();
				}
			}
		});
		folderField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String path = folderField.getText();
					File selectedFile = new File(path);
					if(!selectedFile.exists())
					{
						JOptionPane.showMessageDialog(null, "Path \""+path+"\" invalid.\nChoose another path.", "Invalid path", JOptionPane.ERROR_MESSAGE);
						return;
					}
					rootNode.setFile(selectedFile);
					fileSystemModel.actualizeTree();
				}
			});
		
		// Initialize Buttons
		ImageIcon folderUpIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "Folder Up");
		ImageIcon actualizeIcon = ImageLoadingTools.getResourceIcon("icons/arrow-circle.png", "Actualize");
		
		if(folderUpIcon != null)
			folderUpButton = new JButton(folderUpIcon);
		else 
			folderUpButton = new JButton("Folder Up");
		folderUpButton.setToolTipText("Navigates one folder up.");
		folderUpButton.setMargin(new Insets(1, 1, 1, 1));
		folderUpButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					File parent = rootNode.getFile().getParentFile();
					if(parent == null)
						return;
					rootNode.setFile(parent);
					folderField.setText(parent.getAbsolutePath());
					fileSystemModel.actualizeTree();
				}
			});
		
		if(actualizeIcon != null)
			actualizeButton = new JButton(actualizeIcon);
		else 
			actualizeButton = new JButton("Actualize.");
		actualizeButton.setToolTipText("Actualizes the current view.");
		actualizeButton.setMargin(new Insets(1, 1, 1, 1));
		actualizeButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					rootNode.actualize();
					fileSystemModel.actualizeTree();
				}
			});
		
		// Initialize top layout
		GridBagLayout topLayout = new GridBagLayout();
		JPanel topPanel = new JPanel(topLayout);
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		JPanel folderPanel = new JPanel(new BorderLayout());
		folderPanel.add(folderField, BorderLayout.CENTER);	
		folderPanel.add(folderChooserButton, BorderLayout.EAST);
		StandardFormats.addGridBagElement(folderPanel, topLayout, newLineConstr, topPanel);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttonPanel.add(folderUpButton);
		buttonPanel.add(actualizeButton);
		StandardFormats.addGridBagElement(buttonPanel, topLayout, newLineConstr, topPanel);
		
		add(new JScrollPane(fileSystemTree), BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);  
		setPreferredSize(new Dimension(200, 300));
		fileSystemTree.setRootVisible(false);
		fileSystemTree.setShowsRootHandles(true);
		fileSystemTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		fileSystemTree.addMouseListener (new MouseAdapter () 
		{
			@Override
			public void mouseReleased( MouseEvent e ) 
			{
				mousePressed(e); 
			}
			@Override
			public void mousePressed( MouseEvent e ) 
			{
				TreePath path = fileSystemTree.getPathForLocation(e.getX(), e.getY());
	            if(path == null)
	              return;
	            Object selectedObject = path.getLastPathComponent();
	            fileSystemTree.setSelectionPath(path);
				
				if(e.isPopupTrigger())
				{
		            if(selectedObject instanceof FileSystemElement)
			        {
			        	JPopupMenu popup = ((FileSystemElement)selectedObject).getPopupMenu();
			        	if(popup == null || !(e.getSource() instanceof Component))
			        		return;
			        	popup.show((Component)e.getSource(), e.getX(), e.getY() );
			        }
				}
				else
				{
					if(selectedObject instanceof MeasurementNode)
					{
						synchronized(viewMeasurementListeners)
	                	{
	                		for(ViewMeasurementListener listener : viewMeasurementListeners)
	                		{
	                			listener.viewMeasurement(((MeasurementNode)selectedObject).getFile().getPath());
	                		}
	                	}
					}
				}
             } 
          }
       );

		// Customize icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		ImageIcon leafIcon = ImageLoadingTools.getResourceIcon("icons/receipt.png", "document");
		if(leafIcon != null)
			renderer.setLeafIcon(leafIcon);
		ImageIcon openIcon = ImageLoadingTools.getResourceIcon("icons/folder-horizontal-open.png", "opened folder");
		if(openIcon != null)
			renderer.setOpenIcon(openIcon);
		ImageIcon closedIcon = ImageLoadingTools.getResourceIcon("icons/folder-horizontal.png", "closed folder");
		if(closedIcon != null)
			renderer.setClosedIcon(closedIcon);
		fileSystemTree.setCellRenderer(renderer);
		
		actualize();
		
		setBorder(new TitledBorder("Select Measurement Folder"));
	}
	
	public void setSelectedMeasurement(File measurementFolder)
	{
		// Try to open two folders above the measurement.
		int numFoldersAbove = 2;
		Vector<File> folders = new Vector<File>();
		folders.addElement(measurementFolder);
		File lastFolder = measurementFolder;
		for(int i=0; i< numFoldersAbove; i++)
		{
			File parentFolder = lastFolder.getParentFile();
			if(parentFolder == null)
				break;
			folders.insertElementAt(parentFolder, 0);
			lastFolder = parentFolder;
		}
		folders.removeElementAt(0);
		
		rootNode.setFile(lastFolder);
		folderField.setText(lastFolder.getAbsolutePath());
		fileSystemModel.actualizeTree();
		
		// Now open the path to the measurement and select it.
		Vector<FileSystemElement> pathNodes = new Vector<FileSystemElement>();
		pathNodes.addElement(rootNode);
		FileSystemElement lastNode = rootNode;
		for(File file : folders)
		{
			Enumeration<FileSystemElement> iterator = lastNode.children(); 
			boolean found = false;
			while(iterator.hasMoreElements())
			{
				FileSystemElement node = iterator.nextElement();
				if(node.getFile().equals(file))
				{
					pathNodes.addElement(node);
					fileSystemTree.expandPath(new TreePath(pathNodes.toArray()));
					lastNode = node;
					found = true;
					break;
				}
			}
			if(!found)
			{
				client.sendError("Could not find element " + file.getName() + " in tree. Not selecting measurement automatically.");
				return;
			}
		}
		fileSystemTree.setSelectionPath(new TreePath(pathNodes.toArray()));
		fileSystemTree.scrollPathToVisible(new TreePath(pathNodes.toArray()));
	}
	public void addViewMeasurementListener(ViewMeasurementListener listener)
	{
		synchronized(viewMeasurementListeners)
		{
			viewMeasurementListeners.add(listener);
		}
	}
	public void removeViewMeasurementListener(ViewMeasurementListener listener)
	{
		synchronized(viewMeasurementListeners)
		{
			viewMeasurementListeners.remove(listener);
		}
	}
	
	public void actualize()
	{
		fileSystemModel.actualizeTree();
		try
		{
			folderField.setText(rootNode.getFile().getCanonicalPath());
		}
		catch(IOException e)
		{
			FileSystem.this.client.sendError("Could not actualize file system.", e);
		}
	}
	private abstract class FileSystemElement implements TreeNode
	{
		protected File file;
		protected FileSystemElement parentNode;
		public FileSystemElement(File file, FileSystemElement parentNode)
		{
			this.file = file;
			this.parentNode = parentNode;
		}
		public File getFile()
		{
			return file;
		}
		public void setFile(File file)
		{
			this.file = file;
			actualize();
		}
		public JPopupMenu getPopupMenu()
		{
			// Default implementation does not have a popup.
			return null;
		}
		public void actualize()
		{
			// Default implementation does nothing.
		}
		@Override
		public abstract Enumeration<FileSystemElement> children();
	}
	private FileSystemElement fileToNode(File file, FileSystemElement parentNode)
	{
		File imagesFile = new File(file, "images.csv");
		if(imagesFile.exists() && imagesFile.isFile())
			return new MeasurementNode(file, parentNode);
		return new FolderNode(file, parentNode);
	}
	private File[] sortFiles(File[] files)
	{
		class SortableFile implements Comparable<SortableFile>
		{
			public File file;
			public String fileName;
			public boolean isDir;
			public SortableFile(File file)
			{
				this.file = file;
				fileName = file.getName();
				isDir = file.isDirectory();
			}
			
			@Override
			public int compareTo(SortableFile o)
			{
				if(o.isDir && !isDir)
					return 1;
				else if(!o.isDir && isDir)
					return -1;
				else
					return fileName.compareTo(o.fileName);
			}
		}
		Vector<SortableFile> sortableFiles = new Vector<SortableFile>();
		for(File file : files)
		{
			sortableFiles.add(new SortableFile(file));
		}
		Collections.sort(sortableFiles);
		for(int i=0; i<files.length; i++)
		{
			files[i] = sortableFiles.elementAt(i).file;
		}
		return files;
	}
	
	private class FolderNode extends FileSystemElement
	{
		private File[] children = null;
		public FolderNode(File file, FileSystemElement parentNode)
		{
			super(file, parentNode);
		}
		public void initialize()
		{
			if(children == null)
				actualize();
		}
		@Override
		public void actualize()
		{
			children = file.listFiles(new FileFilter()
			{

				@Override
				public boolean accept(File pathname)
				{
					if(pathname.isDirectory())
						return true;
					return false;
				}
				
			});
			if(children == null)
				children = new File[0];
			children = sortFiles(children);
		}
		@Override
		public String toString()
		{
			return file.getName();
		}
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			initialize();
			if(childIndex >= 0 && childIndex < children.length)
			{
				return fileToNode(children[childIndex], this);
			}
			return null;
		}
		
		@Override
		public JPopupMenu getPopupMenu()
		{
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem gotoButton = new JMenuItem("Open");
			gotoButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	rootNode.setFile(getFile());
						fileSystemModel.actualizeTree();
						try
						{
							folderField.setText(getFile().getCanonicalPath());
						}
						catch(@SuppressWarnings("unused") IOException e1)
						{
							folderField.setText(getFile().getAbsolutePath());
						}
	                }
	            });
			JMenuItem renameButton = new JMenuItem("Rename");
			renameButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	String oldName = getFile().getName();
	                	String fileName = JOptionPane.showInputDialog(null, "Insert new name of the file.", "New File", JOptionPane.PLAIN_MESSAGE, null, null, oldName).toString();
						if(fileName == null)
							return;
						File newFile = new File(getFile().getParentFile(), fileName);
						boolean result = getFile().renameTo(newFile);
						if(!result)
							client.sendError("Could not rename folder \""+oldName+"\" to \""+fileName +"\".", null);
						else
							fileSystemModel.actualizeTree();
	                }
	            });
			JMenuItem deleteButton = new JMenuItem("Delete");
			deleteButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	String oldName = getFile().getName();
	                	int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the folder \"" + oldName + "\" really be deleted?", "Delete File", JOptionPane.YES_NO_OPTION);
						if(shouldDelete != JOptionPane.YES_OPTION)
							return;
						boolean result = getFile().delete();
						if(!result)
							client.sendError("Could not delete the folder \"" + oldName + "\".", null);
						else
							fileSystemModel.actualizeTree();
	                }
	            });
			popupMenu.add(gotoButton);
			popupMenu.add(renameButton);
			popupMenu.add(deleteButton);
			return popupMenu;
		}

		@Override
		public int getChildCount()
		{
			initialize();
			return children.length;
		}

		@Override
		public TreeNode getParent()
		{
			return parentNode;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			initialize();
			if(!(node instanceof FileSystemElement))
				return -1;
			String nodeFile;
			try
			{
				nodeFile = ((FileSystemElement)node).getFile().getCanonicalPath();
			}
			catch(@SuppressWarnings("unused") IOException e)
			{
				return -1;
			}
			for(int i=0; i< children.length; i++)
			{
				try
				{
					if(children[i].getCanonicalPath().compareTo(nodeFile) == 0)
						return i;
				}
				catch(@SuppressWarnings("unused") IOException e)
				{
					// Do nothing.
				}
			}
			
			return -1;
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		public Enumeration<FileSystemElement> children()
		{
			initialize();
			Vector<FileSystemElement> childrenNodes = new Vector<FileSystemElement>();
			for(File child : children)
			{
				childrenNodes.add(fileToNode(child, this));
			}
			return childrenNodes.elements();
		}
		
	}
	
	private class MeasurementNode extends FileSystemElement
	{
	
		MeasurementNode(File file, FileSystemElement parentNode)
		{
			super(file, parentNode);
		}
		
		
		@Override
		public JPopupMenu getPopupMenu()
		{
			JPopupMenu popupMenu = new JPopupMenu();
			
			JMenuItem openButton = new JMenuItem("View");
			openButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	synchronized(viewMeasurementListeners)
	                	{
	                		for(ViewMeasurementListener listener : viewMeasurementListeners)
	                		{
	                			listener.viewMeasurement(file.getPath());
	                		}
	                	}
	                }
	            });
			popupMenu.add(openButton);
			
			if(Desktop.isDesktopSupported())
			{
				JMenuItem openExternalButton = new JMenuItem("Open folder (external)");
				openExternalButton.addActionListener(new ActionListener()
		            {
		                @Override
		                public void actionPerformed(ActionEvent e)
		                {
		                	try
							{
								Desktop.getDesktop().open(file);
							}
							catch(IOException e1)
							{
								client.sendError("Could not open file manager for measurement " + file.getName() + ".", e1);
							}
		                }
		            });
				popupMenu.add(openExternalButton);
			}
			JMenuItem renameButton = new JMenuItem("Rename");
			renameButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	String oldName = getFile().getName();
	                	String fileName = JOptionPane.showInputDialog(null, "Insert new name of the measurement.", "Rename Measurement", JOptionPane.PLAIN_MESSAGE, null, null, oldName).toString();
						if(fileName == null)
							return;
						File newFile = new File(getFile().getParentFile(), fileName);
						boolean result = getFile().renameTo(newFile);
						if(!result)
							client.sendError("Could not rename measurement \""+oldName+"\" to \""+fileName +"\".", null);
						else
							fileSystemModel.actualizeTree();
	                }
	            });
			JMenuItem deleteButton = new JMenuItem("Delete");
			deleteButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	String oldName = getFile().getName();
	                	int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the measurement \"" + oldName + "\" really be deleted?", "Delete Measurement", JOptionPane.YES_NO_OPTION);
						if(shouldDelete != JOptionPane.YES_OPTION)
							return;
						boolean result = getFile().delete();
						if(!result)
							client.sendError("Could not delete the measurement \"" + oldName + "\".", null);
						else
							fileSystemModel.actualizeTree();
	                }
	            });
			popupMenu.add(deleteButton);
			return popupMenu;
		}
		
		@Override
		public String toString()
		{
			return file.getName();
		}
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return null;
		}
		@Override
		public int getChildCount()
		{
			return 0;
		}
		@Override
		public TreeNode getParent()
		{
			return parentNode;
		}
		@Override
		public int getIndex(TreeNode node)
		{
			return -1;
		}
		@Override
		public boolean getAllowsChildren()
		{
			return false;
		}
		
		@Override
		public boolean isLeaf()
		{
			return true;
		}
		
		@Override
		public Enumeration<FileSystemElement> children()
		{
			return new Vector<FileSystemElement>().elements();
		}
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(200, 300);
	}
	
	private class FileSystemModel implements TreeModel
    {
		private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();

        void actualizeTree()
        {
        	rootNode.actualize();
        	for (TreeModelListener listener : treeModelListeners)
            {
                Object[] path = new Object[1];
                path[0] = rootNode;
                listener.treeStructureChanged(new TreeModelEvent(this, path));
            }
        }
        @Override
        public void addTreeModelListener(TreeModelListener l)
        {
            treeModelListeners.addElement(l);
        }

        @Override
        public Object getChild(Object parent, int index)
        {
            return ((TreeNode) parent).getChildAt(index);
        }
        @Override
        public int getChildCount(Object parent)
        {
            return ((TreeNode) parent).getChildCount();
        }
        @Override
        public int getIndexOfChild(Object parent, Object child)
        {
        	return ((TreeNode) parent).getIndex((TreeNode)child);
        }

        @Override
        public Object getRoot()
        {
            return rootNode;
        }

        @Override
        public boolean isLeaf(Object node)
        {
            if (((TreeNode)node).isLeaf())
                return true;
			return false;
        }

       @Override
        public void removeTreeModelListener(TreeModelListener l)
        {
            treeModelListeners.removeElement(l);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue)
        {
            // Not used by this model.
        }
    }
}